/*
	Copyright 2013 Dániel Sólyom

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

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPagerModByDS;
import android.support.v4.view.ViewPagerModByDS.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import ds.framework.v4.R;

public class DSViewPagerTitlebar extends View implements OnPageChangeListener {

	private final ArrayList<Rect> mTitleBounds = new ArrayList<Rect>();
	
	private int mFirstPageOnScreen;
	private float mPositionOffset;
	private int mScrollState;
	
	private final Paint mPaint = new Paint();
	private int mTextColor;
	
	private OnPageChangeListener mListener;
	private int mTextSize;
	
	private ViewPagerModByDS mViewPager;
	private int mActivePointerId = -1;
	private float mLastMotionX = -1;
	private boolean mIsDragging;
	
	public DSViewPagerTitlebar(Context context) {
		this(context, null);
	}
	
	public DSViewPagerTitlebar(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.style.TextAppearance);
	}
	
	public DSViewPagerTitlebar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DsView, defStyle, 0);
		
		mTextColor = a.getColor(R.styleable.DsView_textColor, Color.BLACK);
		mTextSize = a.getDimensionPixelSize(R.styleable.DsView_textSize, 28);
		mPaint.setTextSize(mTextSize);

		a.recycle();
	}
	
	/**
	 * set the view pager - required to work
	 * 
	 * @param pager
	 */
	public void setViewPager(ViewPagerModByDS pager) {
		mViewPager = pager;
	}
	
	/**
	 * set on page change listener
	 * 
	 * @param listener
	 */
	public void setOnPageChangeListener(OnPageChangeListener listener) {
		mListener = listener;
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		mScrollState = state;
		
		if (mListener != null) {
			mListener.onPageScrollStateChanged(state);
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		mFirstPageOnScreen = position;
		mPositionOffset = positionOffset;
		invalidate();
		
		if (mListener != null) {
			mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}
	}

	@Override
	public void onPageSelected(int position) {
		if (mScrollState == ViewPagerModByDS.SCROLL_STATE_IDLE) {
			mFirstPageOnScreen = position;
			mPositionOffset = 0;
            invalidate();
        }
		
		if (mListener != null) {
			mListener.onPageSelected(position);
		}
	}

	@Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mViewPager == null) {
            return;
        }

		final PagerAdapter adapter = mViewPager.getAdapter();
		if (adapter == null) {
			return;
		}
		final int count = adapter.getCount();
        final int width = getWidth();
	
        // calculate new title bounds
        calculateTitlesBounds();

        // draw titles
        int colorAlpha = mTextColor >>> 24;
        for (int i = 0; i < count; i++) {

        	final Rect bound = mTitleBounds.get(i);
        	if (bound.right < 0 || bound.left > width) {
        		
        		// not visible
        		continue;
        	}
        	
        	mPaint.setColor(mTextColor);
        	
        	if (bound.left <= 0 || bound.right >= width) {
        		mPaint.setAlpha(colorAlpha * 2 / 3);
        	} else {
        		mPaint.setAlpha(colorAlpha);
        	}
        	
            final CharSequence title = adapter.getPageTitle(i);
            canvas.drawText(title, 0, title.length(), bound.left, bound.bottom, mPaint);
        }
    }

	private void calculateTitlesBounds() {
        if (mViewPager == null) {
            return;
        }

        final PagerAdapter adapter = mViewPager.getAdapter();
        if (adapter == null) {
            return;
        }

	    mTitleBounds.clear();
		 
        final int width = getWidth();
        final int hw = width / 2;
        final int topPadding = getPaddingTop();

        for (int i = 0; i < adapter.getCount(); i++) {

            final CharSequence title = adapter.getPageTitle(i);
            Rect bounds = new Rect();
            bounds.right = (int) mPaint.measureText(title, 0, title.length());
            bounds.bottom = (int) (-mPaint.ascent()) + topPadding;
            final int w = bounds.right - bounds.left;
            final int ww = width - w;

            bounds.left = (int) ((i - mFirstPageOnScreen - mPositionOffset + 0.5f) * ww);
            final int bonus = (int) (w - 1.5f * mTextSize);
            if (bounds.left < 0) {
                bounds.left += hw - bonus;
                if (bounds.left > 0) {
                    bounds.left = 0;
                }
            }
            if (bounds.left > ww) {
                bounds.left -= hw - bonus;
                if (bounds.left < ww) {
                    bounds.left = ww;
                }
            }
            bounds.right = bounds.left + w;
            mTitleBounds.add(bounds);
        }
	}
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		// any width
        final int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);

        // height
        float height = 0;
        final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightSpecMode == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            height = (int) (mPaint.descent() - mPaint.ascent());
            height += getPaddingTop() + getPaddingBottom();
        }
        final int measuredHeight = (int) height;

        setMeasuredDimension(measuredWidth, measuredHeight);
    }
	
	@Override
	public boolean onTouchEvent(android.view.MotionEvent ev) {
        if (super.onTouchEvent(ev)) {
            return true;
        }
        if ((mViewPager == null) || (mViewPager.getAdapter().getCount() == 0) || !isEnabled()) {
            return false;
        }

        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
        	case MotionEvent.ACTION_DOWN:
        		mActivePointerId = ev.getPointerId(0);
        		mLastMotionX = ev.getX();
        		break;
        		
        	case MotionEvent.ACTION_MOVE:
        		if (!mIsDragging && Math.abs(ev.getX() - mLastMotionX) > 20) {
        			mViewPager.beginFakeDrag();
        			mIsDragging = true;
        		}
        		if (mIsDragging) {
        			mViewPager.fakeDragBy(ev.getX() - mLastMotionX);
        			mLastMotionX = ev.getX();
        		}
        		break;

	        case MotionEvent.ACTION_CANCEL:
	        case MotionEvent.ACTION_UP:
	            if (!mIsDragging) {
	            	int touched = getTouchedTitleByX(ev.getX());
	            	if (touched != -1 && touched == getTouchedTitleByX(mLastMotionX)) {
	            		mViewPager.setCurrentItem(touched, true);
	            	}
	            }
	
	            mIsDragging = false;
	            mActivePointerId = -1;
	            if (mViewPager.isFakeDragging()) {
	            	mViewPager.endFakeDrag();
	            }
	            break;
	
	        case MotionEvent.ACTION_POINTER_DOWN:
	            final int index = ev.getActionIndex();
	            mLastMotionX = ev.getX(index);
	            mActivePointerId = ev.getPointerId(index);
	            break;
	
	        case MotionEvent.ACTION_POINTER_UP:
	            final int pointerIndex = ev.getActionIndex();
	            final int pointerId = ev.getPointerId(pointerIndex);
	            if (pointerId == mActivePointerId) {
	                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
	                mActivePointerId = ev.getPointerId(newPointerIndex);
	            }
	            mLastMotionX = ev.getX(ev.findPointerIndex(mActivePointerId));
	            break;
		}
		return true;
	}

	private int getTouchedTitleByX(float x) {
    	final int boundSize = mTitleBounds.size();
    	for(int i = 0; i < boundSize; ++i) {
    		final Rect bound = mTitleBounds.get(i);
    		if (bound.left <= x && bound.right >= x) {
    			return i;
    		}
    	}
    	return -1;
	}
}
