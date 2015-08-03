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
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import ds.framework.v4.R;
import ds.framework.v4.datatypes.Interval;

public class Slider extends ViewGroup {

	protected Interval mInterval;
	protected int mPosition;
	private boolean mEnabled;
	final protected Handle mHandle = new Handle();
	private OnPositionChangedListener mOnPositionChangedListener;
	protected int mBarSize;
	protected boolean mHorizontal;
	
	public Slider(Context context) {
		super(context);
		
		mEnabled = true;
		mInterval = new Interval(0, 100);
	}

	public Slider(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DsView, 0, 0);

		View handleView = new View(context);
		handleView.setBackgroundDrawable(a.getDrawable(R.styleable.DsView_sliderDrawable));		
		setHandle(handleView);
		
		mEnabled = a.getBoolean(R.styleable.DsView_enabled, true);
		
		setInterval(a.getInteger(R.styleable.DsView_interval_start, 0),
					a.getInteger(R.styleable.DsView_interval_end, 100));
		
		a.recycle();
	}
	
	@Override
	public void addView(View child) {
		throw(new RuntimeException("No outside call of this function expected!"));
	}
	
	public void setOnPositionChangedListener(OnPositionChangedListener listener) {
		mOnPositionChangedListener = listener;
	}
	
	public void setHandle(View handle) {
		mHandle.view = handle;
		mHandle.lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mHandle.view.setLayoutParams(mHandle.lp);
		
		addView(mHandle.view, 0);
	}
	
	public void setInterval(int start, int end) {
		mInterval = new Interval(start, end);
		mPosition = start;
	}
	
	public int getPosition() {
		return mPosition;
	}
	
	public void setPosition(int position) {
		mPosition = position;
		
		if (mBarSize > 0) {

			// already measured
			int handleCoord = (mPosition - mInterval.start) * mBarSize 
					/ mInterval.length();
			
			if (mHorizontal) {
				mHandle.x = handleCoord;
			} else {
				mHandle.y = handleCoord;
			}
			
			requestLayout();
			invalidate();
		}
	}
	
	protected void countNewPosition(float x, float y) {
		float handleCoord = Math.max(0, Math.min(mHorizontal ? x : y, mBarSize));
		
		if (mHorizontal) {
			mHandle.x = (int) handleCoord;
		} else {
			mHandle.y = (int) handleCoord;
		}
		
		requestLayout();
		invalidate();
		
		int oldPos = mPosition;
		mPosition = (int) (handleCoord / mBarSize * mInterval.length()) + mInterval.start;
		
		if (mOnPositionChangedListener != null && oldPos != mPosition) {
			mOnPositionChangedListener.positionChanged(mPosition);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (!mEnabled) {
			return false;
		}
		switch(ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				countNewPosition(ev.getX(), ev.getY());
				return true;
		}
		return false;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		if (mHandle.view != null) {
			int mesWidth = getMeasuredWidth();
			int mesHeight = getMeasuredHeight();
			
			mHorizontal = mesWidth >= mesHeight;
						
			measureChild(mHandle.view, mesWidth, mesHeight);
			
			if (mHorizontal) {
				mHandle.size = mHandle.view.getMeasuredWidth();
			} else {
				mHandle.size = mHandle.view.getMeasuredHeight();
			}
			
			mBarSize = (mHorizontal ? mesWidth : mesHeight) - mHandle.size;
			
			if (mHandle.x == -1) {

				// first here - make sure the handle is at the required position
				setPosition(mPosition);
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (mHandle.view != null) {
			mHandle.view.layout(mHandle.x, 
					mHandle.y,
					mHandle.x + mHandle.view.getMeasuredWidth(), 
					mHandle.y + mHandle.view.getMeasuredHeight()
			);
		}
	}
	
	class Handle {
		int size;
		View view;
		LayoutParams lp;
		int x = -1;
		int y;
	}
	
	public static interface OnPositionChangedListener {
		public void positionChanged(int position);
	}
}
