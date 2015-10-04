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
*/
package ds.framework.v4.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import ds.framework.v4.R;
import ds.framework.v4.widget.LaizyImageView.LaizyImageViewInfo;
import ds.framework.v4.widget.LaizyImageView.OnImageSetListener;

public class LaizyImageFlipAnimationLayout extends FlipAnimationLayout {
	
	private LaizyImageViewInfo mImageInfo;

	private int mDirection = TOP_TO_BOTTOM;
	private int mNextImagePosition = 0;
	private int mNextLoadingPosition = 0;
	
	private LaizyImageView mNextImageView;
	
	private boolean mFirstImage = true;
	
	private boolean mShowingLoading = false;
	private boolean mNeedToShowLoading = false;
	
	private boolean mFlipFirst;
	
	private OnImageSetListener mOnImageSetListener = new OnImageSetListener() {

		@Override
		public void onDefaultSet(LaizyImageView view) {	
			if (!view.getInfo().info.equals(mImageInfo.info)) {
				return;
			}

			onFinishedLoading();
		}

		@Override
		public void onLoadingSet(LaizyImageView view) {
			if (!view.getInfo().info.equals(mImageInfo.info)) {
				return;
			}
			
			mShowingLoading = true;
			
			if (mNextImagePosition == mNextLoadingPosition) {
				
				// only happens when we are loading the first image and no need to flip
				((LaizyImageView) getChildAt(0)).showLoading(mImageInfo);
				return;
			}
			
			// just animate in the loading image
			mNeedToShowLoading = true;

			showLoading();
		}

		@Override
		public void onErrorSet(LaizyImageView view) {
			if (!view.getInfo().info.equals(mImageInfo.info)) {
				return;
			}

			onFinishedLoading();
		}

		@Override
		public void onImageSet(LaizyImageView view) {
			if (!view.getInfo().info.equals(mImageInfo.info)) {
				return;
			}
			
			onFinishedLoading();
		}
	
	};
	
	public LaizyImageFlipAnimationLayout(Context context) {
		this(context, null);
	}
	
	public LaizyImageFlipAnimationLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public LaizyImageFlipAnimationLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DsView, defStyle, 0);
		
		mFlipFirst = a.getBoolean(R.styleable.DsView_flip_first, true);
		
		a.recycle();
	}
	
	/**
	 * create third image view which will be used to lazy-load the images 
	 * 
	 * @param context
	 * @return
	 */
	protected LaizyImageView createThirdImageView(Context context) {
		return new LaizyImageView(context);
	}
	
	public void reset() {
		stop();
		
		setCurrentChild(0);
		mFirstImage = true;
	}
	
	/**
	 * 
	 * @param always
	 */
	public void flipAlways(boolean always) {
		mFlipFirst = always;
	}
	
	/**
	 * 
	 * @param info
	 */
	public void loadImage(LaizyImageViewInfo info) {
		if (mImageInfo != null && (info == null || info.info.equals(mImageInfo.info))) {
			
			// already loading / loaded this image
			return;
		}
		
		stop();
		
		if (getChildCount() < 3) {
			for(int i = getChildCount(); i < 3; ++i) {
				final LaizyImageView thirdView = createThirdImageView(getContext());
				addView(thirdView);
			}
		}
		
		mNeedToShowLoading = false;
		
		mImageInfo = info;
		info.needFading = false;
		
		// load the image
		if (mFirstImage) {
			
			// first image to load
			mNextLoadingPosition = mNextImagePosition = 1;
			
			// act like we are showing loading so we could do the flip even for the first image
			mShowingLoading = mFlipFirst;
			mFirstImage = false;
		} else {
			
			// load to the next empty position
			mNextImagePosition = advancePosition(getCurrentChildPosition());
			
			// the current third position which is not showing and not used to 
			// load into it will be the only empty position we can have
			mNextLoadingPosition = advancePosition(mNextImagePosition);
		}
		
		mNextImageView = (LaizyImageView) getChildAt(mNextImagePosition);
		mNextImageView.setOnImageSetListener(mOnImageSetListener);
		mNextImageView.reset();
		mNextImageView.load(mImageInfo);
	}

	/**
	 * 
	 */
	public void stop() {
		mNeedToShowLoading = false;
		mShowingLoading = false;
		if (mNextImageView != null) {
			mNextImageView.stopLoading();
			mNextImageView.setOnImageSetListener(null);
		}
		mNextImageView = null;
		mImageInfo = null;
		
		super.cancel();
	}
	
	/**
	 * 
	 * @param direction
	 */
	public void setDirection(int direction) {
		assert(direction >= 0 && direction < DIRECTIONS.length);
		
		mDirection = direction;
	}
		
	@Override
	void setState(int state) {
		super.setState(state);

		if (mNeedToShowLoading && state == STATE_CALM) {
			
			// still loading the image and the previous flip is finished
			// show loading
			showLoading();
		}
	}
	
	/**
	 * 
	 */
	private void showLoading() {
		if (!mNeedToShowLoading) {
			return;
		}
		if (getState() == STATE_ANIMATING) {
			
			// wait for the previous animation to finish when loading
			return;
		}

		mNeedToShowLoading = false;
		
		((LaizyImageView) getChildAt(mNextLoadingPosition)).showLoading(mImageInfo);

		start(mDirection, mNextLoadingPosition);
	}
		
	/**
	 * 
	 */
	private void onFinishedLoading() {
		if (getCurrentChildPosition() == mNextImagePosition) {
	
			// we are showing the image that just finished loading so nothing to do
			// except we cancel the animation if there was any 
			// this would look messy if animating but mostly it is not the case
			cancel();
			return;
		}
		
		if (!mShowingLoading) {
		
			// there was no need to show loading - image was right there
			// just switch without animation
			setCurrentChild(mNextImagePosition);
		} else {

			// flip to the loaded image
			start(mDirection, mNextImagePosition);
		}
		mNextImageView.setOnImageSetListener(null);
	}
	
	/**
	 * 
	 * @param resource
	 */
	public void setCurrentTo(int resId) {
		stop();

		((LaizyImageView) getCurrentChild()).setImageResource(resId);
	}
	
	/**
	 * 
	 */
	private int advancePosition(int position) {
		position++;
		if (position > 2) {
			position = 0;
		}
		return position;
	}
}
