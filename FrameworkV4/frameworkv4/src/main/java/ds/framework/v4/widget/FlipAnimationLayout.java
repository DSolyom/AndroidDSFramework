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
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

public class FlipAnimationLayout extends RelativeLayout {

	public final static int STATE_INIT = 1;
	public final static int STATE_CALM = 2;
	public final static int STATE_ANIMATING = 3;
	public final static int STATE_PENDING = 4;
	
	public final static int ANIMATION_NO = 0;
	public final static int ANIMATION_SHOWING_FIRST = 1;
	public final static int ANIMATION_SHOWING_SECOND = 2;
	
	public final static int LEFT_TO_RIGHT = 0;
	public final static int RIGHT_TO_LEFT = 1;
	public final static int TOP_TO_BOTTOM = 2;
	public final static int BOTTOM_TO_TOP = 3;

	public final static float[][] DIRECTIONS = new float[][] {
		new float[] { 0, -90.0f, 90.0f, 0, 1 },
		new float[] { 0, 90.0f, -90.0f, 0, 1 },
		new float[] { 0, -90.0f, 90.0f, 0, 0 },
		new float[] { 0, 90.0f, -90.0f, 0, 0 }
	};
	
	private FlipAnimation mStartAnimation;
	private FlipAnimation mEndAnimation;
	private FlipAnimation mCurrentAnimation;
	
	private int mState;
	private int mCurrentChildPosition = 0;
	private PointF mCenter;
	private int mDuration = 500;
	
	private OnAnimationStateChangeListener mListener;
	private int mDirection;
	private int mNextChildPosition;
	
	public FlipAnimationLayout(Context context) {
		super(context);
		
		init();
	}
	
	public FlipAnimationLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init();
	}
	
	public FlipAnimationLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init();
	}
	
	private  void init() {
		setState(STATE_INIT);
	}

	@Override
	public void addView(View child) {
		super.addView(child);
		
		if (getChildCount() > 1) {
			child.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void addView(View child, int index) {
		super.addView(child, index);
		
		if (getChildCount() > 1) {
			child.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void addView(View child, ViewGroup.LayoutParams params) {
		super.addView(child, params);
		
		if (getChildCount() > 1) {
			child.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		super.addView(child, index, params);
		
		if (getChildCount() > 1) {
			child.setVisibility(View.INVISIBLE);
		}
	}
	
	public void setOnAnimationStateChangeListener(OnAnimationStateChangeListener listener) {
		mListener = listener;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getState() {
		return mState;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getCurrentChildPosition() {
		return mCurrentChildPosition;
	}
	
	/**
	 * get the current child (the current child is the next visible one if there is an ongoing animation)
	 * 
	 * @return
	 */
	public View getCurrentChild() {
		return getChildAt(mCurrentChildPosition);
	}
	
	/**
	 * 
	 * @param position
	 */
	public void setCurrentChild(int position) {
		cancel();
		
		if (mCurrentChildPosition == position) {
			return;
		}

		getChildAt(mCurrentChildPosition).setVisibility(View.INVISIBLE);
		mCurrentChildPosition = position;
		getChildAt(mCurrentChildPosition).setVisibility(View.VISIBLE);
	}
	
	/**
	 * 
	 * @param direction
	 */
	public void start(int direction, int nextChildPosition) {
		mDirection = direction;
		mNextChildPosition = nextChildPosition;
		
		if (mCenter == null) {
		
			// (probably) no layout yet - set to start right after layout
			setState(STATE_PENDING);
			requestLayout();
			return;
		}

		cancel();
		createAnimations(DIRECTIONS[mDirection]);

		mCurrentAnimation = mStartAnimation;
		
		setState(STATE_ANIMATING);

		getCurrentChild().startAnimation(mCurrentAnimation);
	}
	
	/**
	 * stop fliping - put it in end position if in the middle
	 */
	public void cancel() {
		if (mCurrentAnimation != null) {
			mCurrentAnimation.setAnimationListener(null);
			mCurrentAnimation.cancel();
			
			setState(STATE_CALM);
			mCurrentAnimation = null;
			
			getCurrentChild().setAnimation(null);
		} else if (mState == STATE_PENDING) {
			setState(STATE_INIT);
		}
	}
	
	@Override
	public void onLayout(boolean changed, int l, int t, int r, int b) {
		if (mState == STATE_INIT) {
			setState(STATE_CALM);
		}
		
		super.onLayout(changed, l, t, r, b);

		if (mCenter == null) {
			mCenter = new PointF();
		}
		
		if (changed) {
			mCenter.x = (float) (r - l) / 2;
			mCenter.y = (float) (b - t) / 2;
		}
		
		if (mCenter.x != 0 && mState == STATE_PENDING) {
			setState(STATE_CALM);
			start(mDirection, mNextChildPosition);
		}
	}
	
	void setState(int state) {
		if (mState == state) {
			return;
		}
		mState = state;

		if (mListener != null) {
			mListener.onAnimationStateChanged(mState);
		}
	}
	
	/**
	 * returns animation state
	 * 
	 * @return
	 */
	int getAnimationState() {
		if (mState == STATE_ANIMATING) {
			return mCurrentAnimation == mStartAnimation ? ANIMATION_SHOWING_FIRST : ANIMATION_SHOWING_SECOND;
		} else {
			return ANIMATION_NO;
		}
	}
	
	/**
	 * 
	 */
	private void createAnimations(float[] directions) {
		mStartAnimation = new FlipAnimation();
		mStartAnimation.setFillBefore(true);
		mStartAnimation.setFillAfter(true);
		mStartAnimation.setDuration(mDuration  / 2);
		mEndAnimation = new FlipAnimation();
		mEndAnimation.setFillBefore(true);
		mEndAnimation.setFillAfter(true);
		mEndAnimation.setDuration(mDuration  / 2);
	
		mStartAnimation.set(mCenter, directions[0], directions[1], directions[4] == 0);
		mEndAnimation.set(mCenter, directions[2], directions[3], directions[4] == 0);

		mStartAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				final View lastChild = getCurrentChild();
				lastChild.setAnimation(null);
				lastChild.setVisibility(View.INVISIBLE);
				mCurrentAnimation = mEndAnimation;
				mCurrentAnimation.reset();
				mCurrentChildPosition = mNextChildPosition;
				getCurrentChild().startAnimation(mCurrentAnimation);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				;
			}

			@Override
			public void onAnimationStart(Animation animation) {
				;
			}
		
		});
		mEndAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				mCurrentAnimation = null;
				getCurrentChild().setAnimation(null);
				setState(STATE_CALM);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				;
			}

			@Override
			public void onAnimationStart(Animation animation) {
				getCurrentChild().setVisibility(View.VISIBLE);
			}
		
		});
	}

	/**
	 * @class FlipAnimation
	 */
	public static class FlipAnimation extends Animation {
		
		private boolean mUseAxisX = true;
		private float mStart;
	    private float mEnd;
	    private float mSize;
	    private PointF mCenter;

		private Camera mCamera;

		public FlipAnimation() {
			;
		}
		
		public FlipAnimation(PointF center, float start, float end, boolean useAxisX) {
			set(center, start, end, useAxisX);
		}
		
		/**
		 * 
		 * @param center
		 * @param start
		 * @param end
		 */
		public void set(PointF center, float start, float end, boolean useAxisX) {
			if (hasStarted() && !hasEnded()) {
				return;
			}
			reset();
			
			mCenter = center;
			mStart = start;
			mEnd = end;
			mUseAxisX = useAxisX;
		}

		@Override
	    public void initialize(final int width, final int height, final int parentWidth, final int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
			
			mSize = (float) Math.sqrt(width * height);
			mCamera = new Camera();
		}
		
		@Override
	    protected void applyTransformation(final float interpolatedTime, final Transformation t) {
			final Matrix matrix = t.getMatrix();
			final float at = mStart + (mEnd - mStart) * interpolatedTime;

	        mCamera.save();
	        mCamera.translate(0, 0, Math.abs(at) * mSize / 200);
	        
	        if (mUseAxisX) {
	        	mCamera.rotateX(at);
	        } else {
	        	mCamera.rotateY(at);
	        }
	        mCamera.getMatrix(matrix);
	        mCamera.restore();

	        matrix.preTranslate(-mCenter.x, -mCenter.y);
	        matrix.postTranslate(mCenter.x, mCenter.y);
		}
	}
	
	public interface OnAnimationStateChangeListener {
		public void onAnimationStateChanged(int state);
	}
}
