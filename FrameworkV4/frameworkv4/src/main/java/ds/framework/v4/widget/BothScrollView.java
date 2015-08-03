/*
	Copyright 2014 Dániel Sólyom

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
	
	Based very very much on Android's ScrollView under the same licence
*/
package ds.framework.v4.widget;

import java.util.List;

import ds.framework.v4.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

public class BothScrollView extends FrameLayout {
	static final String TAG = "BothScrollView";
	static final int ANIMATED_SCROLL_GAP = 250;
	static final float MAX_SCROLL_FACTOR = 0.5f;
	static final int ACTION_MASK = MotionEvent.ACTION_CANCEL | 
			MotionEvent.ACTION_DOWN | 
			MotionEvent.ACTION_MOVE | 
			MotionEvent.ACTION_OUTSIDE | 
			MotionEvent.ACTION_UP;
	static final int SCROLLBARS_HORIZONTAL = 0x00000100; // from View - hope this doesn't change there
    static final int SCROLLBARS_VERTICAL = 0x00000200; // from View - hope this doesn't change there

    private boolean mHorizontalOnly;
    private boolean mVerticalOnly;
	private boolean mVerticalScrollEnabled;
	private boolean mHorizontalScrollEnabled;
    
	private boolean mFillViewport;
	int mMinimumVelocity;
	int mMaximumVelocity;
	private int mTouchSlop;
	boolean mSmoothScrollingEnabled = true;
	
	private final Rect mTempRect = new Rect();
	long mLastScroll;
	
	Scroller mScroller;
	boolean mScrollViewMovedFocus;

	boolean mIsBeingDragged = false;

	float mLastMotionY;
	float mLastMotionX;

	VelocityTracker mVelocityTracker;
    
	View mChildToScrollTo = null;
	
	boolean mIsLayoutDirty = true;
	
	boolean mJustScrolling = false;
	private OnScrollListener mOnScrollListener;
	
	public BothScrollView(Context context) {
		this(context, null);
	}

	public BothScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.scrollViewStyle);
    }

	public BothScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initScrollView();

		setFillViewport(attrs.getAttributeBooleanValue("http://schemas.android.com/apk/res/android", "fillViewport", false));
		
		int scrollbars = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android", "scrollbars", -1);
		if (scrollbars == -1) {
			setHorizontalScrollBarEnabled(true);
		}
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DsView, defStyle, 0);
		
		mHorizontalOnly = a.getBoolean(R.styleable.DsView_horizontalScrollOnly, false);
		mVerticalOnly = a.getBoolean(R.styleable.DsView_verticalScrollOnly, false);
		mHorizontalScrollEnabled = 
				mVerticalOnly ? false : a.getBoolean(R.styleable.DsView_horizontalScrollEnabled, true);
		mVerticalScrollEnabled = 
				mHorizontalOnly ? false : a.getBoolean(R.styleable.DsView_verticalScrollEnabled, true);

		a.recycle();
	}
	
	@Override
	protected float getLeftFadingEdgeStrength() {
		if (getChildCount() == 0) {
			return 0.0f;
		}

		final int length = getHorizontalFadingEdgeLength();
		if (getScrollX() < length) {
			return getScrollX() / (float) length;
		}
		return 1.0f;
	}

	@Override
	protected float getRightFadingEdgeStrength() {
		if (getChildCount() == 0) {
			return 0.0f;
		}

		final int length = getHorizontalFadingEdgeLength();
		final int rightEdge = getWidth() - getPaddingRight();
		final int span = getChildAt(0).getRight() - getScrollX() - rightEdge;

		if (span < length) {
			return span / (float) length;
		}
		return 1.0f;
	}
	
	@Override
	protected float getTopFadingEdgeStrength() {
		if (getChildCount() == 0) {
			return 0.0f;
		}

		final int length = getVerticalFadingEdgeLength();
		if (getScrollY() < length) {
			return getScrollY() / (float) length;
		}
		return 1.0f;
	}

	@Override
	protected float getBottomFadingEdgeStrength() {
		if (getChildCount() == 0) {
			return 0.0f;
		}

		final int length = getVerticalFadingEdgeLength();
		final int bottomEdge = getHeight() - getPaddingBottom();
		final int span = getChildAt(0).getBottom() - getScrollY() - bottomEdge;

		if (span < length) {
			return span / (float) length;
		}
		return 1.0f;
	}
	
	public int getMaxVerticalScrollAmount() {
		return (int) (MAX_SCROLL_FACTOR * (getBottom() - getTop()));
	}
	
	public int getMaxHorizontalScrollAmount() {
		return (int) (MAX_SCROLL_FACTOR * (getRight() - getLeft()));
	}
	
	private void initScrollView() {
		mScroller = new Scroller(getContext());
		setFocusable(true);
		setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
		setWillNotDraw(false);

		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
	}
	
	@Override
	public void addView(View child) {
		if (getChildCount() > 0) {
			throw new IllegalStateException("ScrollView can host only one direct child");
        }
		super.addView(child);
	}

	@Override
	public void addView(View child, int index) {
		if (getChildCount() > 0) {
			throw new IllegalStateException("ScrollView can host only one direct child");
        }
		super.addView(child, index);
	}

	@Override
	public void addView(View child, ViewGroup.LayoutParams params) {
		if (getChildCount() > 0) {
			throw new IllegalStateException("ScrollView can host only one direct child");
        }
		super.addView(child, params);
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if (getChildCount() > 0) {
			throw new IllegalStateException("ScrollView can host only one direct child");
        }
		super.addView(child, index, params);
	}
	
	/**
	 * 
	 * @param enable
	 */
	public void setVerticalScrollEnabled(boolean enable) {
		mVerticalScrollEnabled = enable;
	}
	
	/**
	 * 
	 * @param enable
	 */
	public void setHorizontalScrollEnabled(boolean enable) {
		mHorizontalScrollEnabled = enable;
	}
	
	private boolean canScroll() {
		return canScrollVertical() || canScrollHorizontal();
	}

	private boolean canScrollVertical() {
		if (!mVerticalScrollEnabled || !isEnabled()) {
			return false;
		}
		View child = getChildAt(0);
		if (child != null) {
			return getHeight() < child.getHeight() + getPaddingTop() + getPaddingBottom();
		}
		return false;
	}
	
	private boolean canScrollHorizontal() {
		if (!mHorizontalScrollEnabled || !isEnabled()) {
			return false;
		}
		View child = getChildAt(0);
		if (child != null) {
			return getWidth() < child.getWidth() + getPaddingLeft() + getPaddingRight();
		}
		return false;
	}

	public boolean isFillViewport() {
		return mFillViewport;
	}
	
	public void  setFillViewport(boolean fillViewport) {
		if (fillViewport != mFillViewport) {
			mFillViewport = fillViewport;

			requestLayout();
		}
	}
	
	public boolean isSmoothScrollingEnabled() {
		return mSmoothScrollingEnabled;
	}
	
	public void setSmoothScrollingEnabled(boolean smoothScrollingEnabled) {
		mSmoothScrollingEnabled = smoothScrollingEnabled;
	}
	
	public void setOnScrollListener(OnScrollListener listener) {
		mOnScrollListener = listener;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (!mFillViewport) {
			return;
		}

		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		if (getChildCount() == 0) {
			return;
		}

		final View child = getChildAt(0);
		
		int childWidthMeasureSpec = -1;
		int childHeightMeasureSpec = -1;

		if (heightMode != MeasureSpec.UNSPECIFIED) {
			int height = getMeasuredHeight();
			if (child.getMeasuredHeight() < height) {
				height -= getPaddingTop();
				height -= getPaddingBottom();
				childHeightMeasureSpec =
					MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
			}
		}

		if (widthMode != MeasureSpec.UNSPECIFIED) {
			int width = getMeasuredWidth();
			if (child.getMeasuredWidth() < width) {
				width -= getPaddingLeft();
				width -= getPaddingRight();
				childWidthMeasureSpec =
					MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
			}
		}

		child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// Let the focused view and/or our descendants get the key first
		boolean handled = super.dispatchKeyEvent(event);

		if (handled) {
			return true;
		}

		return executeKeyEvent(event);
	}
	
	public boolean executeKeyEvent(KeyEvent event) {

		mTempRect.setEmpty();
		
		boolean canVertical = canScrollVertical();
		boolean canHorizontal = canScrollHorizontal();
		
		boolean handled = false;

		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_DPAD_UP:
					if (!canVertical) {
						return executeCantScrollKeyEvent(View.FOCUS_UP);
					}
					if (!event.isAltPressed()) {
						handled = arrowVerticalScroll(View.FOCUS_UP);
					} else {
						handled = fullScroll(View.FOCUS_UP);
					}
					break;

				case KeyEvent.KEYCODE_DPAD_DOWN:
					if (!canVertical) {
						return executeCantScrollKeyEvent(View.FOCUS_DOWN);
					}
					if (!event.isAltPressed()) {
						handled = arrowVerticalScroll(View.FOCUS_DOWN);
					} else {
						handled = fullScroll(View.FOCUS_DOWN);
					}
					break;
				
				case KeyEvent.KEYCODE_DPAD_LEFT:
					if (!canHorizontal) {
						return executeCantScrollKeyEvent(View.FOCUS_LEFT);
					}
					if (!event.isAltPressed()) {
						handled = arrowHorizontalScroll(View.FOCUS_LEFT);
					} else {
						handled = fullScroll(View.FOCUS_LEFT);
					}
					break;
				
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					if (!canHorizontal) {
						return executeCantScrollKeyEvent(View.FOCUS_RIGHT);
					}
					if (!event.isAltPressed()) {
						handled = arrowHorizontalScroll(View.FOCUS_RIGHT);
					} else {
						handled = fullScroll(View.FOCUS_RIGHT);
					}
					break;
			}
		}
		return handled;
	}
	
	public boolean executeCantScrollKeyEvent(int direction) {
		if (isFocused()) {

			View currentFocused = findFocus();

			if (currentFocused == this) {
				currentFocused = null;
			}

			View nextFocused = FocusFinder.getInstance().findNextFocus(this,
					currentFocused, direction);
			return nextFocused != null && nextFocused != this
					&& nextFocused.requestFocus(direction);
		}
		return false;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
			return true;
		}

		if (!canScroll()) {
			mIsBeingDragged = false;
			return false;
		}

		final float x = ev.getX();
		final float y = ev.getY();

		switch (action) {
			case MotionEvent.ACTION_MOVE:
				final int yDiff = canScrollVertical() ? (int) Math.abs(y - mLastMotionY) : 0;
				final int xDiff = canScrollHorizontal() ? (int) Math.abs(x - mLastMotionX) : 0;
				if (yDiff > mTouchSlop || xDiff > mTouchSlop) {
					mIsBeingDragged = true;
				}
				break;

			case MotionEvent.ACTION_DOWN:
				mLastMotionY = y;
				mLastMotionX = x;
				mIsBeingDragged = !mScroller.isFinished();
				break;

			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				mIsBeingDragged = false;
				break;
		}

		return mIsBeingDragged;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getEdgeFlags() != 0) {
			return false;
		}
		
		if (getChildCount() == 0) {
			return false;
		}

		if (!canScroll()) {
			return false;
		}

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}

		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction() & ACTION_MASK;
		final float x = ev.getX();
		final float y = ev.getY();

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				if (!mScroller.isFinished()) {
					mScroller.abortAnimation();
				}
				mLastMotionY = y;
				mLastMotionX = x;
				break;

			case MotionEvent.ACTION_MOVE:
				int deltaY = canScrollVertical() ? (int) (mLastMotionY - y) : 0;
				mLastMotionY = y;

				final int scrollY = getScrollY();
				final int scrollX = getScrollX();
				
				if (deltaY < 0) {
					deltaY = Math.max(-scrollY, deltaY);
				} else if (deltaY > 0) {
					final int bottomEdge = getHeight() - getPaddingBottom();
					final int availableToScroll = getChildAt(0).getBottom() - scrollY - bottomEdge;
					if (availableToScroll > 0) {
						deltaY = Math.min(availableToScroll, deltaY);
					} else {
						deltaY = 0;
					}
				}
				
				int deltaX = canScrollHorizontal() ? (int) (mLastMotionX - x) : 0;
				mLastMotionX = x;

				if (deltaX < 0) {
					deltaX = Math.max(-scrollX, deltaX);
				} else if (deltaX > 0) {
					final int rightEdge = getWidth() - getPaddingRight();
					final int availableToScroll = getChildAt(0).getRight() - scrollX - rightEdge;
					if (availableToScroll > 0) {
						deltaX = Math.min(availableToScroll, deltaX);
					} else {
						deltaX = 0;
					}
				}

				scrollBy(deltaX, deltaY);
				
				break;

			case MotionEvent.ACTION_UP:
				final VelocityTracker velocityTracker = mVelocityTracker;

				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int initialXVelocity = canScrollHorizontal() ? (int) velocityTracker.getXVelocity() : 0;
				int initialYVelocity = canScrollVertical() ? (int) velocityTracker.getYVelocity() : 0;

				if ((Math.abs(initialXVelocity) <= mMinimumVelocity)) {
					initialXVelocity = 0;
				}
				if ((Math.abs(initialYVelocity) <= mMinimumVelocity)) {
					initialYVelocity = 0;
				}
				if (initialXVelocity != 0 || initialYVelocity != 0) {
					fling(-initialXVelocity, -initialYVelocity);
				}

				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
				break;
		}
		return true;
	}

	public void fling(int velocityX, int velocityY) {
		if (getChildCount() > 0) {
			int height = getHeight() - getPaddingBottom() - getPaddingTop();
			int bottom = getChildAt(0).getHeight();
			int width = getWidth() - getPaddingRight() - getPaddingLeft();
			int right = getChildAt(0).getWidth();

			mScroller.fling(getScrollX(), getScrollY(), velocityX, velocityY, 0, right - width, 0, bottom - height);

			final int movingHorizontal = velocityX > 0 ? View.FOCUS_RIGHT : View.FOCUS_LEFT;
			final int movingVertical = velocityY > 0 ? View.FOCUS_DOWN : View.FOCUS_UP;

			View newFocused =
				findFocusableViewInMyBounds(movingHorizontal == View.FOCUS_LEFT, 
						movingVertical == View.FOCUS_UP, 
						mScroller.getFinalX(), 
						mScroller.getFinalY(), findFocus());
			if (newFocused == null) {
				newFocused = this;
			}

			if (newFocused != findFocus()
					&& newFocused.requestFocus(movingHorizontal | movingVertical)) {

				mScrollViewMovedFocus = true;
			}

			invalidate();
		}
	}
	
	public boolean arrowHorizontalScroll(int direction) {

		View currentFocused = findFocus();

		if (currentFocused == this) {
			currentFocused = null;
		}

		View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, direction);

		final int maxJump = getMaxHorizontalScrollAmount();

		if (nextFocused != null && isWithinHorizontalDeltaOfScreen(nextFocused, maxJump, getWidth())) {
			nextFocused.getDrawingRect(mTempRect);
			offsetDescendantRectToMyCoords(nextFocused, mTempRect);

			int scrollDelta = computeHorizontalScrollDeltaToGetChildRectOnScreen(mTempRect);
			doScrollX(scrollDelta);
			nextFocused.requestFocus(direction);
		} else {

			// no new focus
			int scrollDelta = maxJump;

			if (direction == View.FOCUS_LEFT && getScrollX() < scrollDelta) {
				scrollDelta = getScrollX();
			} else if (direction == View.FOCUS_RIGHT) {
				if (getChildCount() > 0) {
					int daRight = getChildAt(0).getRight();
					int screenRight = getScrollX() + getWidth();

					if (daRight - screenRight < maxJump) {
						scrollDelta = daRight - screenRight;
					}
				}
			}

			if (scrollDelta == 0) {
				return false;
			}
			doScrollX(direction == View.FOCUS_RIGHT ? scrollDelta : -scrollDelta);
		}

		if (currentFocused != null && currentFocused.isFocused()
				&& isHorizontalOffScreen(currentFocused)) {

			final int descendantFocusability = getDescendantFocusability();  // save
			setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
			requestFocus();
			setDescendantFocusability(descendantFocusability);  // restore
		}
		return true;
	}

	public boolean arrowVerticalScroll(int direction) {

		View currentFocused = findFocus();

		if (currentFocused == this) {
			currentFocused = null;
		}

		View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, direction);

		final int maxJump = getMaxVerticalScrollAmount();

		if (nextFocused != null && isWithinVerticalDeltaOfScreen(nextFocused, maxJump, getHeight())) {
			nextFocused.getDrawingRect(mTempRect);
			offsetDescendantRectToMyCoords(nextFocused, mTempRect);

			int scrollDelta = computeVerticalScrollDeltaToGetChildRectOnScreen(mTempRect);
			doScrollY(scrollDelta);
			nextFocused.requestFocus(direction);
		} else {

			// no new focus
			int scrollDelta = maxJump;

			if (direction == View.FOCUS_UP && getScrollY() < scrollDelta) {
				scrollDelta = getScrollY();
			} else if (direction == View.FOCUS_DOWN) {
				if (getChildCount() > 0) {
					int daBottom = getChildAt(0).getBottom();
					int screenBottom = getScrollY() + getHeight();

					if (daBottom - screenBottom < maxJump) {
						scrollDelta = daBottom - screenBottom;
					}
				}
			}

			if (scrollDelta == 0) {
				return false;
			}
			doScrollY(direction == View.FOCUS_DOWN ? scrollDelta : -scrollDelta);
		}

		if (currentFocused != null && currentFocused.isFocused()
				&& isVerticalOffScreen(currentFocused)) {

			final int descendantFocusability = getDescendantFocusability();  // save
			setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
			requestFocus();
			setDescendantFocusability(descendantFocusability);  // restore
		}
		return true;
	}
	
	public boolean fullScroll(int direction) {
		boolean down = direction == View.FOCUS_DOWN;
		boolean right = direction == View.FOCUS_RIGHT;
		int width = getWidth();
		int height = getHeight();

		mTempRect.left = 0;
		mTempRect.top = 0;
		mTempRect.right = width;
		mTempRect.bottom = height;

		if (down) {
			int count = getChildCount();

			if (count > 0) {
				View view = getChildAt(count - 1);
				mTempRect.bottom = view.getBottom();
				mTempRect.top = mTempRect.bottom - height;
			}
		} else if (right) {
			int count = getChildCount();

			if (count > 0) {
				View view = getChildAt(count - 1);
				mTempRect.right = view.getRight();
				mTempRect.left = mTempRect.right - width;
			}
		}

		return scrollAndFocus(direction, mTempRect.left, mTempRect.top, mTempRect.right, mTempRect.bottom);
	}
	
	private boolean scrollAndFocus(int direction, int left, int top, int right, int bottom) {
		boolean handled = true;
		int width = getWidth();
		int height = getHeight();
		int containerLeft = getScrollX();
		int containerRight = containerLeft + width;
		int containerTop = getScrollY();
		int containerBottom = containerTop + height;
		boolean toDown = direction == View.FOCUS_DOWN;
		boolean toRight = direction == View.FOCUS_RIGHT;

		View newFocused = findFocusableViewInBounds(!toDown, !toRight, left, top, right, bottom);
		if (newFocused == null) {
			newFocused = this;
		}

		if (toDown || direction == View.FOCUS_UP) {
			if (top >= containerTop && bottom <= containerBottom) {
				handled = false;
			} else {
				int delta = toDown ? (bottom - containerBottom) : (top - containerTop);
				doScrollY(delta);
			}
		} else {
			if (left >= containerLeft && right <= containerRight) {
				handled = false;
			} else {
				int delta = toRight ? (right - containerRight) : (left - containerLeft);
				doScrollX(delta);
			}
		}

		if (newFocused != findFocus() && newFocused.requestFocus(direction)) {
			mScrollViewMovedFocus = true; //FIXME
		}
		return handled;
	}
	
	private View findFocusableViewInMyBounds(boolean leftFocus, boolean topFocus, 
			int left, int top, View preferredFocusable) {

		final int horizontalFadingEdgeLength = getHorizontalFadingEdgeLength() / 2;
		final int leftWithoutFadingEdge = left + horizontalFadingEdgeLength;
		final int rightWithoutFadingEdge = left + getWidth() - horizontalFadingEdgeLength;
		final int verticalFadingEdgeLength = getVerticalFadingEdgeLength() / 2;
		final int topWithoutFadingEdge = top + verticalFadingEdgeLength;
		final int bottomWithoutFadingEdge = top + getHeight() - verticalFadingEdgeLength;

		if ((preferredFocusable != null)
				&& (preferredFocusable.getLeft() < rightWithoutFadingEdge)
				&& (preferredFocusable.getRight() > leftWithoutFadingEdge)
				&& (preferredFocusable.getTop() < bottomWithoutFadingEdge)
				&& (preferredFocusable.getBottom() > topWithoutFadingEdge)) {
			return preferredFocusable;
		}

		return findFocusableViewInBounds(topFocus, leftFocus, leftWithoutFadingEdge,
				topWithoutFadingEdge, rightWithoutFadingEdge, bottomWithoutFadingEdge);
	}
	
	private View findFocusableViewInBounds(boolean leftFocus, boolean topFocus , int left, int top, int right, int bottom) {
		List<View> focusables = getFocusables(leftFocus ? View.FOCUS_LEFT : View.FOCUS_RIGHT);
		View focusCandidate = null;
		
		boolean foundFullyContainedFocusable = false;

		int count = focusables.size();
		for (int i = 0; i < count; i++) {
			View view = focusables.get(i);
			int viewTop = view.getTop();
			int viewBottom = view.getBottom();

			if (top < viewBottom && viewTop < bottom) {
				int viewLeft = view.getLeft();
				int viewRight = view.getRight();
				
				if (left < viewLeft && viewRight < right) {
					continue;
				}
				
				final boolean viewIsFullyContained = 
					(top < viewTop) && (viewBottom < bottom) &&
						(left < viewLeft) && (right < viewRight);

				if (focusCandidate == null) {
					/* No candidate, take this one */
					focusCandidate = view;
				} else {
					int horizontalDistance = leftFocus ? 
							focusCandidate.getLeft() - viewLeft : 
								viewRight - focusCandidate.getRight();
					int verticalDistance = topFocus ?
							focusCandidate.getTop() - viewTop : 
								viewBottom - focusCandidate.getBottom();

					final boolean viewIsCloserToBoundary = 
						(horizontalDistance > 0 && horizontalDistance >= verticalDistance ||
								verticalDistance > 0 && verticalDistance > horizontalDistance); 					

					if (foundFullyContainedFocusable) {
						if (viewIsFullyContained && viewIsCloserToBoundary) {
							focusCandidate = view;
						}
					} else {
						if (viewIsFullyContained) {
							/* Any fully contained view beats a partially contained view */
							focusCandidate = view;
							foundFullyContainedFocusable = true;
						} else if (viewIsCloserToBoundary) {
							focusCandidate = view;
						}
					}
				}
			}
		}
		return focusCandidate;
	}
	
	protected int computeHorizontalScrollDeltaToGetChildRectOnScreen(Rect rect) {
		if (getChildCount() == 0) {
			return 0;
		}

		int width = getWidth();
		int screenLeft = getScrollX();
		int screenRight = screenLeft + width;
		int fadingEdge = getHorizontalFadingEdgeLength();

		if (rect.left > 0) {
			screenLeft += fadingEdge;
		}

		if (rect.right < getChildAt(0).getWidth()) {
			screenRight -= fadingEdge;
		}
		int scrollXDelta = 0;

		if (rect.right > screenRight && rect.left > screenLeft) {
			if (rect.width() > width) {
				scrollXDelta += (rect.left - screenLeft);
			} else {
				scrollXDelta += (rect.right - screenRight);
			}
			int right = getChildAt(0).getRight();
			int distanceToRight = right - screenRight;
	
			scrollXDelta = Math.min(scrollXDelta, distanceToRight);
		} else if (rect.left < screenLeft && rect.right < screenRight) {

			if (rect.width() > width) {
				scrollXDelta -= (screenRight - rect.right);
			} else {
				scrollXDelta -= (screenLeft - rect.left);
			}
			scrollXDelta = Math.max(scrollXDelta, -getScrollX());
		}
		return scrollXDelta;
	}
	
	protected int computeVerticalScrollDeltaToGetChildRectOnScreen(Rect rect) {
		if (getChildCount() == 0) {
			return 0;
		}

		int height = getHeight();
		int screenTop = getScrollY();
		int screenBottom = screenTop + height;
		int fadingEdge = getVerticalFadingEdgeLength();

		if (rect.top > 0) {
			screenTop += fadingEdge;
		}

		if (rect.bottom < getChildAt(0).getHeight()) {
			screenBottom -= fadingEdge;
		}
		int scrollYDelta = 0;

		if (rect.bottom > screenBottom && rect.top > screenTop) {
			if (rect.height() > height) {
				scrollYDelta += (rect.top - screenTop);
			} else {
				scrollYDelta += (rect.bottom - screenBottom);
			}
			int bottom = getChildAt(0).getBottom();
			int distanceToBottom = bottom - screenBottom;
	
			scrollYDelta = Math.min(scrollYDelta, distanceToBottom);
		} else if (rect.top < screenTop && rect.bottom < screenBottom) {

			if (rect.height() > height) {
				scrollYDelta -= (screenBottom - rect.bottom);
			} else {
				scrollYDelta -= (screenTop - rect.top);
			}
			scrollYDelta = Math.max(scrollYDelta, -getScrollY());
		}
		return scrollYDelta;
	}
	
	private boolean isHorizontalOffScreen(View descendant) {
		return !isWithinHorizontalDeltaOfScreen(descendant, 0, getWidth());
	}

	private boolean isWithinHorizontalDeltaOfScreen(View descendant, int delta, int width) {
		descendant.getDrawingRect(mTempRect);
		offsetDescendantRectToMyCoords(descendant, mTempRect);

		return (mTempRect.right + delta) >= getScrollX()
				&& (mTempRect.left - delta) <= (getScrollX() + width);
	}
	
	private boolean isVerticalOffScreen(View descendant) {
		return !isWithinVerticalDeltaOfScreen(descendant, 0, getHeight());
	}

	private boolean isWithinVerticalDeltaOfScreen(View descendant, int delta, int height) {
		descendant.getDrawingRect(mTempRect);
		offsetDescendantRectToMyCoords(descendant, mTempRect);

		return (mTempRect.bottom + delta) >= getScrollY()
				&& (mTempRect.top - delta) <= (getScrollY() + height);
	}
	
	private void doScrollXY(int deltaX, int deltaY) {
		if (deltaX != 0 || deltaY != 0) {
			if (mSmoothScrollingEnabled) {
				smoothScrollBy(deltaX, deltaY);
			} else {
				scrollBy(deltaX, deltaY);
			}
		}
	}
	
	private void doScrollX(int delta) {
		if (delta != 0) {
			if (mSmoothScrollingEnabled) {
				smoothScrollBy(delta, 0);
			} else {
				scrollBy(delta, 0);
			}
		}
	}
	
	private void doScrollY(int delta) {
		if (delta != 0) {
			if (mSmoothScrollingEnabled) {
				smoothScrollBy(0, delta);
			} else {
				scrollBy(0, delta);
			}
		}
	}
	
	/**
	 * stop scrolling
	 */
	public void stopScrolling() {
		mScroller.forceFinished(true);
	}
	
	/**
	 * scroll to position
	 * 
	 * @param x
	 * @param y
	 */
	@Override
	public void scrollTo(int x, int y) {
		final int scrollX = getScrollX();
		final int scrollY = getScrollY();
		
		super.scrollTo(x, y);
		
		if (mOnScrollListener != null && (scrollX != x || scrollY != y)) {
			mOnScrollListener.onScroll(this, x, y);
		}
	}
	
	/**
	 * scroll smoothly by
	 * 
	 * @param dx
	 * @param dy
	 */
	public final void smoothScrollBy(int dx, int dy) {
		long duration = AnimationUtils.currentAnimationTimeMillis() - mLastScroll;
		
		if (duration > ANIMATED_SCROLL_GAP) {
			mScroller.startScroll(getScrollX(), getScrollY(), dx, dy);
			invalidate();
		} else {
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			scrollBy(dx, dy);
        }
		mLastScroll = AnimationUtils.currentAnimationTimeMillis();
	}

	/**
	 * scroll to position smoothly
	 * 
	 * @param dx
	 * @param dy
	 */
	public final void smoothScrollTo(int x, int y) {
		smoothScrollBy(x - getScrollX(), y - getScrollY());
	}
	
	@Override
	protected int computeHorizontalScrollRange() {
		int count = getChildCount();

		return count == 0 ? getWidth() : (getChildAt(0)).getRight();
	}

	@Override
	protected int computeVerticalScrollRange() {
		int count = getChildCount();

		return count == 0 ? getHeight() : (getChildAt(0)).getBottom();
	}
	
	@Override
	protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
		child.measure(
				MeasureSpec.makeMeasureSpec(mVerticalOnly ? parentWidthMeasureSpec : 0, MeasureSpec.UNSPECIFIED), 
				MeasureSpec.makeMeasureSpec(mHorizontalOnly ? parentHeightMeasureSpec : 0, MeasureSpec.UNSPECIFIED)
		);
	}

	@Override
	protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed,
		int parentHeightMeasureSpec, int heightUsed) {
		final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

		final int childWidthMeasureSpec;
		final int childHeightMeasureSpec;
		
		if (mVerticalOnly) {
			childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
				getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin + widthUsed, lp.width);
		} else {
			childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
				lp.leftMargin + lp.rightMargin, MeasureSpec.UNSPECIFIED);
		}
		if (mHorizontalOnly) {
			childHeightMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
				getPaddingLeft() + getPaddingRight() + lp.topMargin + lp.bottomMargin + widthUsed, lp.height);
		} else {
			childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
				lp.topMargin + lp.bottomMargin, MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
	}

	@Override
	public void computeScroll() {
		if (mJustScrolling) {
			return;
		}
		if (mScroller.computeScrollOffset()) {
			mJustScrolling = true;
			
			int oldX = getScrollX();
			int oldY = getScrollY();

			int x = mScroller.getCurrX();
			int y = mScroller.getCurrY();

			if (getChildCount() > 0) {
				View child = getChildAt(0);

				scrollTo(x = clamp(x, getWidth() - getPaddingRight() - getPaddingLeft(), child.getWidth()),
							y = clamp(y, getHeight() - getPaddingBottom() - getPaddingTop(), child.getHeight()));
			} else {
				scrollTo(x, y);
			}            

			if (oldX != x || oldY != y) {
				onScrollChanged(x, y, oldX, oldY);
			}
			mJustScrolling = false;
			
			postInvalidate();
		}
	}
	
	@Override
	public void invalidate() {
		if (mJustScrolling) {
			return;
		}
		super.invalidate();
	}
	
	protected int computeScrollXDeltaToGetChildRectOnScreen(Rect rect) {
        if (getChildCount() == 0) return 0;

        int width = getWidth();
        int screenLeft = getScrollX();
        int screenRight = screenLeft + width;

        int fadingEdge = getHorizontalFadingEdgeLength();

        // leave room for top fading edge as long as rect isn't at very top
        if (rect.left > 0) {
            screenLeft += fadingEdge;
        }

        // leave room for bottom fading edge as long as rect isn't at very bottom
        if (rect.right < getChildAt(0).getWidth()) {
            screenRight -= fadingEdge;
        }

        int scrollXDelta = 0;

        if (rect.right > screenRight && rect.left > screenLeft) {
            if (rect.width() > width) {
                scrollXDelta += (rect.left - screenLeft);
            } else {
                scrollXDelta += (rect.right - screenRight);
            }
            int right = getChildAt(0).getRight();
            int distanceToRight = right - screenRight;
            scrollXDelta = Math.min(scrollXDelta, distanceToRight);

        } else if (rect.left < screenLeft && rect.right < screenRight) {
            if (rect.width() > width) {
                scrollXDelta -= (screenRight - rect.right);
            } else {
                scrollXDelta -= (screenLeft - rect.left);
            }
            scrollXDelta = Math.max(scrollXDelta, -getScrollX());
        }
        return scrollXDelta;
    }
	
    protected int computeScrollYDeltaToGetChildRectOnScreen(Rect rect) {
        if (getChildCount() == 0) return 0;

        int height = getHeight();
        int screenTop = getScrollY();
        int screenBottom = screenTop + height;

        int fadingEdge = getVerticalFadingEdgeLength();

        // leave room for top fading edge as long as rect isn't at very top
        if (rect.top > 0) {
            screenTop += fadingEdge;
        }

        // leave room for bottom fading edge as long as rect isn't at very bottom
        if (rect.bottom < getChildAt(0).getHeight()) {
            screenBottom -= fadingEdge;
        }

        int scrollYDelta = 0;

        if (rect.bottom > screenBottom && rect.top > screenTop) {
            if (rect.height() > height) {
                scrollYDelta += (rect.top - screenTop);
            } else {
                scrollYDelta += (rect.bottom - screenBottom);
            }
            int bottom = getChildAt(0).getBottom();
            int distanceToBottom = bottom - screenBottom;
            scrollYDelta = Math.min(scrollYDelta, distanceToBottom);

        } else if (rect.top < screenTop && rect.bottom < screenBottom) {
            if (rect.height() > height) {
                scrollYDelta -= (screenBottom - rect.bottom);
            } else {
                scrollYDelta -= (screenTop - rect.top);
            }
            scrollYDelta = Math.max(scrollYDelta, -getScrollY());
        }
        return scrollYDelta;
    }
	
	@Override
	public void requestChildFocus(View child, View focused) {
		if (!mScrollViewMovedFocus) {
            if (!mIsLayoutDirty) {
                scrollToChild(focused);
            } else {
                mChildToScrollTo  = focused;
            }
        }
        super.requestChildFocus(child, focused);
    }
	
	/**
     * Scrolls the view to the given child.
     *
     * @param child the View to scroll to
     */
    private void scrollToChild(View child) {
        child.getDrawingRect(mTempRect);

        /* Offset from child's local coordinates to ScrollView coordinates */
        offsetDescendantRectToMyCoords(child, mTempRect);

        int deltaX = computeScrollXDeltaToGetChildRectOnScreen(mTempRect);
        int deltaY = computeScrollYDeltaToGetChildRectOnScreen(mTempRect);

        if (deltaX != 0 || deltaY != 0) {
            scrollBy(deltaX, deltaY);
        }
    }
    
    private boolean scrollToChildRect(Rect rect, boolean immediate) {
    	final int deltaX = computeScrollXDeltaToGetChildRectOnScreen(rect);
    	final int deltaY = computeScrollYDeltaToGetChildRectOnScreen(rect);

        final boolean scroll = deltaX != 0 || deltaY != 0;
        if (scroll) {
            if (immediate) {
                scrollBy(deltaX, deltaY);
            } else {
                smoothScrollBy(deltaX, deltaY);
            }
        }
        return scroll;
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction,
            Rect previouslyFocusedRect) {
   	
    	if (direction == View.FOCUS_FORWARD) {
    		direction = View.FOCUS_DOWN;
    	} else if (direction == View.FOCUS_BACKWARD) {
    		direction = View.FOCUS_UP;
    	}

        final View nextFocus = previouslyFocusedRect == null ?
                FocusFinder.getInstance().findNextFocus(this, null, direction) :
                FocusFinder.getInstance().findNextFocusFromRect(this,
                        previouslyFocusedRect, direction);

        if (nextFocus == null) {
            return false;
        }

        if (isOffScreen(nextFocus)) {
            return false;
        }

        return nextFocus.requestFocus(direction, previouslyFocusedRect);
    }
    
    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle,
            boolean immediate) {
        // offset into coordinate space of this scroll view
        rectangle.offset(child.getLeft() - child.getScrollX(),
                child.getTop() - child.getScrollY());

        return scrollToChildRect(rectangle, immediate);
    }

    @Override
    public void requestLayout() {
        mIsLayoutDirty = true;
        super.requestLayout();
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mIsLayoutDirty = false;
        // Give a child focus if it needs it 
        if (mChildToScrollTo != null && isViewDescendantOf(mChildToScrollTo, this)) {
        	scrollToChild(mChildToScrollTo);
        }
        mChildToScrollTo = null;

        // Calling this with the present values causes it to re-clam them
        scrollTo(getScrollX(), getScrollY());
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        View currentFocused = findFocus();
        if (null == currentFocused || this == currentFocused)
            return;

        int deltaX;
        int deltaY;
        
        // If the currently-focused view was visible on the screen when the
        // screen was at the old height, then scroll the screen to make that
        // view visible with the new screen height.
        if (isWithinHorizontalDeltaOfScreen(currentFocused, 0, oldw)) {
            currentFocused.getDrawingRect(mTempRect);
            offsetDescendantRectToMyCoords(currentFocused, mTempRect);
            deltaX = computeScrollXDeltaToGetChildRectOnScreen(mTempRect);
        } else {
        	return;
        }
        if (isWithinVerticalDeltaOfScreen(currentFocused, 0, oldh)) {
            currentFocused.getDrawingRect(mTempRect);
            offsetDescendantRectToMyCoords(currentFocused, mTempRect);
            deltaY = computeScrollYDeltaToGetChildRectOnScreen(mTempRect);
            
            doScrollXY(deltaX, deltaY);
        }
        
    }  

    /**
     * Return true if child is an descendant of parent, (or equal to the parent).
     */
    private boolean isViewDescendantOf(View child, View parent) {
        if (child == parent) {
            return true;
        }

        final ViewParent theParent = child.getParent();
        return (theParent instanceof ViewGroup) && isViewDescendantOf((View) theParent, parent);
    }
    
    private boolean isOffScreen(View descendant) {
        return !isWithinDeltaOfScreen(descendant, 0, getWidth(), 0, getHeight());
    }

    private boolean isWithinDeltaOfScreen(View descendant, int deltaX, int width, 
    		int deltaY, int height) {
        descendant.getDrawingRect(mTempRect);
        offsetDescendantRectToMyCoords(descendant, mTempRect);

        return (mTempRect.right + deltaX) >= getScrollX()
        		&& (mTempRect.left - deltaX) <= (getScrollX() + width) &&
        		(mTempRect.bottom + deltaY) >= getScrollY()
                && (mTempRect.top - deltaY) <= (getScrollY() + height);
    }

	
	private int clamp(int n, int my, int child) {
		if (my >= child || n < 0) {
			return 0;
		}
		if ((my+n) > child) {
			return child - my;
		}
		return n;
	}
	
	public interface OnScrollListener {
		public void onScroll(BothScrollView view, int x, int y);
	}
}
