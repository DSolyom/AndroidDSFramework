/*
	Copyright 2011 Dániel Sólyom

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package ds.framework.v4.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import ds.framework.v4.R;

public class DragAndDropListView extends ListView {
	
	private boolean mEnabled = true;
	
	private int mLastX;
	private int mLastY;
	private int mYOffset;
	private int mYListOffset;
	
	private int mHeight;
	private int mWidth;
	
	private boolean mMoveUp = false;

	private int mHandleId;
	
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mWindowParams;
	
	private int mDraggedPosition = -1;
	private View mDraggedItem;
	private int mDraggedHeight;
	private ImageView mDraggedView;
	private Bitmap mDraggedBitmap;
	
	private int mExpandedPosition = -1;
	private boolean mIsExpandedUp;

	private int mScrollLimit;
	
	private OnDragAndDropListener mOnDragAndDropListener = null;
	
	private int mTouchSlop;
	
	public DragAndDropListView(Context context) {
		super(context);
	}

	public DragAndDropListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public DragAndDropListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		
		TypedArray arr = getContext().obtainStyledAttributes(
				attrs,
				R.styleable.DsView,
				0, 0
		);
		
		mHandleId = arr.getResourceId(R.styleable.DsView_handleId, -1);
		mEnabled = arr.getBoolean(R.styleable.DsView_enabled, true);
	}
	
	public void setOnDragAndDropListener(OnDragAndDropListener listener) {
		mOnDragAndDropListener = listener;
	}
	
	public void setDragEnabled(boolean enabled) {
		mEnabled = enabled;
	}
	
	public boolean isDragEnabled() {
		return mEnabled;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mEnabled && ev.getAction() == MotionEvent.ACTION_DOWN && getCount() > 0) {
			mLastX = (int) ev.getX();
			mLastY = (int) ev.getY();
			
			int draggedPosition = pointToPosition(mLastX, mLastY);
			if (draggedPosition == AdapterView.INVALID_POSITION) {
				return super.onInterceptTouchEvent(ev);
			}
			mDraggedItem = (ViewGroup) getChildAt(draggedPosition - getFirstVisiblePosition());

			if (mHandleId != -1) {
				View handle = mDraggedItem.findViewById(mHandleId);

				if (handle == null) {
					return super.onInterceptTouchEvent(ev);
				}

				/* find the coordinates of the handle */
				View parent = handle;
				int pX;
				int pY;
				pX = handle.getLeft() - handle.getScrollX();
				pY = handle.getTop() - handle.getScrollY();
				while(!mDraggedItem.equals(parent)) {
					parent = (View)parent.getParent();
					pX += parent.getLeft();
					pY += parent.getTop();
				}
				
				if (mLastX < pX || mLastX > pX + handle.getWidth() ||
						mLastY < pY || mLastY > pY + handle.getHeight()) {
					return super.onInterceptTouchEvent(ev);	
				}
			}
			
			mDraggedPosition = draggedPosition;
			
			/* touched the 'handle' or no handle so drag by touching it anywhere */
			mYListOffset = (int) ev.getRawY() - mLastY;
			mYOffset = mLastY - mDraggedItem.getTop();

			mHeight = getHeight();	// no better place
			mWidth = getWidth();
			
			startDragging();
			return false;
		}
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mDraggedView != null) {
			int action = ev.getAction(); 
			switch (action) {
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					stopDraggingAt();
					break;
						
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
					int x = (int) ev.getX();
					int y = (int) ev.getY();

					if (y < mYOffset) {
						y = mYOffset;
					}
					if (y > mHeight - mDraggedHeight + mYOffset) {
						y = mHeight - mDraggedHeight + mYOffset;
					}
					
					if (y == mLastY) {
						break;
					}

					drag(x, y);
					
					if (y < mLastY) {
						mMoveUp = true;
					} else {
						mMoveUp = false;
					}
					
					mLastY = y;
					mLastX = x;

					/* still scroll if needed */
					
					int speed = 0;

					if (y - mYOffset < mScrollLimit) {
						speed = -mTouchSlop;
						if (y - mYOffset > mScrollLimit / 2) {
							speed /= 3;
						}
					} else if (y - mYOffset + mDraggedHeight > mHeight - mScrollLimit) {
						speed = mTouchSlop;
						if (y - mYOffset + mDraggedHeight < mHeight - mScrollLimit / 2) {
							speed /= 3;
						}
					}
					if (speed != 0) {
						int ref = pointToPosition(0, mHeight / 2);
						int tries = 0;
						while(ref == AdapterView.INVALID_POSITION && tries < 3) {
							++tries;
							ref = pointToPosition(0, mHeight / 2 + getDividerHeight() * tries);
						}
						if (ref != AdapterView.INVALID_POSITION) {
							View v = getChildAt(ref - getFirstVisiblePosition());
							setSelectionFromTop(ref, v.getTop() - speed);
						}
					}
					arrangeChildren(mLastX, mLastY);
					break;
			}
			return true;
		}
		runThrough();
		return super.onTouchEvent(ev);
	}
	
	private void startDragging() {
		collectGarbage();

		mWindowParams = new WindowManager.LayoutParams();
		mWindowParams.gravity = Gravity.TOP;
		mWindowParams.x = 0;
		mWindowParams.y = mYOffset;

		mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		mWindowParams.format = PixelFormat.TRANSLUCENT;
		mWindowParams.windowAnimations = 0;
		
		mDraggedHeight = mDraggedItem.getHeight();
		mScrollLimit = mDraggedHeight;
		
		mDraggedView = new ImageView(getContext());

		mDraggedItem.setDrawingCacheEnabled(true);
		mDraggedView.setImageBitmap(mDraggedBitmap = Bitmap.createBitmap(mDraggedItem.getDrawingCache()));

		mWindowManager = (WindowManager)getContext().getSystemService("window");
		mWindowManager.addView(mDraggedView, mWindowParams);
		
		mIsExpandedUp = mDraggedPosition <= getFirstVisiblePosition();
		mExpandedPosition = mIsExpandedUp ? mDraggedPosition + 1 : mDraggedPosition - 1;
		
		drag(mLastX, mLastY);
		runThrough();
	}
	
	private void arrangeChildren(int atX, int atY) {
		int edge;
		int atRow;
		View v;
		
		edge = atY - mYOffset;
		if (!mMoveUp) {
			edge += mDraggedHeight;
		}
		
		atRow = pointToPosition(0, edge);
		if (atRow == mDraggedPosition) {
			runThrough();
			return;
		}
		v = getChildAt(atRow - getFirstVisiblePosition());

		if (v == null) {
			runThrough();
			return;
		}
		
		int padding = 0;
		
		if (atRow == mExpandedPosition) {
			if (mIsExpandedUp && !mMoveUp) {
				padding = v.getPaddingTop();
			} else if (!mIsExpandedUp && mMoveUp) {
				padding = -v.getPaddingBottom();
			} else {
				runThrough();
				return;
			}
		}
		
		int center = v.getTop() + (v.getHeight() + padding) / 2 + (mMoveUp ? 2 : -2);
		if (mMoveUp && center > edge || !mMoveUp && center < edge) {
			mIsExpandedUp = mMoveUp;
			mExpandedPosition = atRow;
		}
		runThrough();
	}
	
	private void runThrough() {
		int sC = getChildCount();
		int expandable = mExpandedPosition - getFirstVisiblePosition();
		int draggedChild = mDraggedPosition - getFirstVisiblePosition();
		
		for (int i = 0; i < sC; ++i) {
			View child = getChildAt(i);
			
			if (i == draggedChild) {
				((ViewGroup) child).getChildAt(0).setVisibility(View.GONE);
			} else {
				((ViewGroup) child).getChildAt(0).setVisibility(View.VISIBLE);
				if (i == expandable) {
					child.setPadding(0, mIsExpandedUp ? mDraggedHeight - 1 : 0, 0, mIsExpandedUp ? 0 : mDraggedHeight - 1);
				} else {
					child.setPadding(0, 0, 0, 0);
				}
			}
		}
	}
	
	private void stopDraggingAt() {
		collectGarbage();
		
		int from = mDraggedPosition;
		int to = mExpandedPosition;
		if (mIsExpandedUp && mExpandedPosition > mDraggedPosition) {
			--to;
		}
		if (!mIsExpandedUp && mExpandedPosition < mDraggedPosition) {
			++to;
		}
		
		mExpandedPosition = -1;
		mDraggedPosition = -1;
		
		if (mOnDragAndDropListener != null) {
			if (mLastX > mWidth / 10 || !mOnDragAndDropListener.delete(from)) {
				mOnDragAndDropListener.drop(from, to);
			}
		}
		runThrough();
	}
	
	private void drag(int x, int y) {
		mWindowParams.y = y - mYOffset + mYListOffset;
		mWindowManager.updateViewLayout(mDraggedView, mWindowParams);
	}
	
	private void collectGarbage() {
		if (mWindowManager != null) {
			mWindowManager.removeView(mDraggedView);
			mWindowManager = null;
			mDraggedView.setImageDrawable(null);
			mDraggedView = null;
			mDraggedBitmap.recycle();
			mDraggedBitmap = null;
		}
	}
	
	public interface OnDragAndDropListener {
		public void drop(int from, int to);
		public boolean delete(int from);
	}
}
