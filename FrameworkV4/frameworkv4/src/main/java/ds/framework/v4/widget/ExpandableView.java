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
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import ds.framework.v4.R;

public class ExpandableView extends FrameLayout implements Runnable {

	public final static int DEFAULT_EXPAND_DURATION = 500;

	private boolean mExpandFirst;
	private boolean mIsSmall;
	private int mHeightAt;
	private int mSmallHeight;
	private int mMaxHeight;
	private int mExpanderResId;
	private View mExpanderView;
	private boolean mExpanderNeeded;
	
	private Expander mExpander;
	private int mDuration;

	private boolean mEnabled = true;

	private String mExpandText;
	private String mTightenText;

	public ExpandableView(Context context) {
		this(context, null);
	}

	public ExpandableView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
    }

	public ExpandableView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DsView, defStyle, 0);
		
		mSmallHeight = a.getDimensionPixelSize(R.styleable.DsView_smallHeight, -1);
		mExpandFirst = mIsSmall = !a.getBoolean(R.styleable.DsView_start_fullMode, false);
		
		mHeightAt = -1;

			// TODO - make a widget for the expander
		mExpanderResId = a.getResourceId(R.styleable.DsView_expander, -1);
		mDuration = a.getInt(R.styleable.DsView_expand_duration, DEFAULT_EXPAND_DURATION);
		
		mExpandText = a.getString(R.styleable.DsView_expandText);
		mTightenText = a.getString(R.styleable.DsView_tightenText);
		
		a.recycle();
		
		mExpander = new Expander();
	}
	
	/**
	 * get the expander view
	 * 
	 * @return
	 */
	public View getExpanderView() {
		return mExpanderView;
	}
	
	/**
	 * 
	 */
	public void setEnabled(boolean enabled) {
		if (mEnabled == enabled) {
            return;
        }

		if (mExpanderView != null) {
			mExpanderView.setVisibility(enabled && mExpanderNeeded ? View.VISIBLE : View.GONE);
			
			if (enabled && mExpanderNeeded) {
				if (!mEnabled) {
					mEnabled = enabled;
					
					if (mIsSmall) {
						mIsSmall = false;
					}
					
					refreshExpanderView();
					
					mHeightAt = mMaxHeight;
					
					start();
					return;
				} else {
					mIsSmall = mHeightAt != mMaxHeight;
					refreshExpanderView();
				}
			}
		}
		
		mEnabled = enabled;	
		
		super.setEnabled(enabled);
		
		requestLayout();
	}
	
	public void setExpanderView(int resId) {
		mExpanderView = ((View) getParent()).findViewById(resId);
		mExpanderView.setVisibility(mExpanderNeeded ? VISIBLE : GONE);

		mExpanderView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				start();
			}
			
		});
		setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				start();
			}
			
		});
	}
	
	protected void refreshExpanderView() {
		if (mExpanderView instanceof TextView) {
   			((TextView) mExpanderView).setText(mIsSmall ? mExpandText : mTightenText);
		}
   		mExpanderView.setSelected(!mIsSmall);
	}
	
	public boolean getExpandState() {
		return mIsSmall;
	}
	
	@Override
	public void addView(View child) {
		if (getChildCount() > 0) {
			throw new IllegalStateException("ExpandableView can host only one direct child");
        }
		super.addView(child);
	}

	@Override
	public void addView(View child, int index) {
		if (getChildCount() > 0) {
			throw new IllegalStateException("ExpandableView can host only one direct child");
        }
		super.addView(child, index);
	}

	@Override
	public void addView(View child, ViewGroup.LayoutParams params) {
		if (getChildCount() > 0) {
			throw new IllegalStateException("ExpandableView can host only one direct child");
        }
		super.addView(child, params);
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if (getChildCount() > 0) {
			throw new IllegalStateException("ExpandableView can host only one direct child");
        }
		super.addView(child, index, params);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.UNSPECIFIED));

        int height = getMeasuredHeight();
        if (mMaxHeight != height) {
        	mExpander.abort();
        	
        	mMaxHeight = height;
        	
        	if (mHeightAt <= mSmallHeight) {
        		mHeightAt = -1;
        	} else if (!mIsSmall && mHeightAt < mMaxHeight) {
        		mHeightAt = mMaxHeight;
        	}
        }

        if (mHeightAt == -1) {
            
        	if (mExpanderView == null) {
           		setExpanderView(mExpanderResId);
           		mExpanderView.measure(mExpanderView.getLayoutParams().height, mExpanderView.getLayoutParams().width);
        	}
        	
        	// first time or after a reset
            height = Math.min(mMaxHeight, mSmallHeight);
        	mExpanderNeeded = mMaxHeight > height;

        	refreshExpanderView();
        	
        	mExpanderView.setVisibility(mEnabled && mExpanderNeeded ? VISIBLE : GONE);

        	if (!mIsSmall || !mExpanderNeeded || !mEnabled) {
        		mHeightAt = mMaxHeight;
        	} else {
        		mHeightAt = height;
        	}
        }
        
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(mHeightAt, MeasureSpec.AT_MOST));
	}
	
	public void reset() {
		mIsSmall = mExpandFirst;
   		mHeightAt = -1;
   		mExpander.abort();
   		requestLayout();
	}
	
	/**
	 * 
	 * @param max
	 */
	public void startFrom(final boolean max) {
		mExpander.abort();

		if (mMaxHeight <= mSmallHeight) {
			return;
		}
		
		mIsSmall = !max;
		
		refreshExpanderView();
		
		mHeightAt = max ? mMaxHeight : mSmallHeight;
		
		start();
	}
	
    private void start() {
    	if (!mEnabled ) {
    		return;
    	}
    	if (!mExpander.isFinished()) {
    		mExpander.abort();
    		mExpander.restart(mIsSmall ? mMaxHeight : mSmallHeight);
    		post(this);
    	} else {
    		mExpander.start(mHeightAt, mIsSmall ? mMaxHeight : mSmallHeight, mDuration);
    		post(this);
    	}

    	mIsSmall = !mIsSmall;

        refreshExpanderView();
    }
    
    public void run() {
    	if (!mExpander.compute()) {
    		refreshExpanderView();
    		return;
    	}

    	mHeightAt = mExpander.getPos();

    	requestLayout();
    	invalidate();
    	
    	post(this);
    }

}