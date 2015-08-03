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

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import ds.framework.v4.Global;
import ds.framework.v4.R;
import ds.framework.v4.common.AnimationStarter;
import ds.framework.v4.common.Debug;
import ds.framework.v4.io.ImageLoader;
import ds.framework.v4.io.ImageLoader.ImageInfo;
import ds.framework.v4.io.LaizyLoader.Callback;

public class LaizyImageView extends ImageView implements Callback<ImageInfo, Bitmap> {

	public final int STATE_DEFAULT = 0;
	public final int STATE_LOADING = 1;
	public final int STATE_SET = 2;
	public final int STATE_ERROR = 3;
	
	public static final ScaleType[] sScaleTypeArray = {
		ScaleType.MATRIX,
		ScaleType.FIT_XY,
		ScaleType.FIT_START,
		ScaleType.FIT_CENTER,
		ScaleType.FIT_END,
		ScaleType.CENTER,
		ScaleType.CENTER_CROP,
		ScaleType.CENTER_INSIDE
	};
	
	private static ImageLoader sImageLoader;

	private ImageLoader mImageLoader;
	
	public static int NO_IMAGE = -1;
	public static int HIDE_IMAGE = 0;
	
	private LaizyImageViewInfo mInfo;
	
	private ImageInfo mCurrentImageInfo;
	
	private ScaleType mOriginalScaleType;

	private OnImageSetListener mOnImageSetListener;

	protected int mImageState;

	private Animation mAnimFadeIn;
	private boolean mNeedVisibility = false;
	private boolean mAlwaysLoadInBackground = true;
	
	private ScaleType mDefaultScaleType;
	private ScaleType mLoadingScaleType;
	private ScaleType mErrorScaleType;
	
	public LaizyImageView(Context context) {
		super(context);
	}
	
	public LaizyImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public LaizyImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DsView, defStyle, 0);
		
		mAlwaysLoadInBackground = a.getBoolean(R.styleable.DsView_alwaysInBg, true);
		
		int index = a.getInt(R.styleable.DsView_defaultScaleType, -1);
		if (index != -1) {
			mDefaultScaleType = sScaleTypeArray[index];
		}
		index = a.getInt(R.styleable.DsView_errorScaleType, -1);
		if (index != -1) {
			mErrorScaleType = sScaleTypeArray[index];
		}
		index = a.getInt(R.styleable.DsView_loadingScaleType, -1);
		if (index != -1) {
			mLoadingScaleType = sScaleTypeArray[index];
		}
		
		a.recycle();
	}
	
	/**
	 * 
	 * @param listener
	 */
	public void setOnImageSetListener(OnImageSetListener listener) {
		mOnImageSetListener = listener;
	}
	
	public void load(LaizyImageViewInfo info) {
		synchronized(LaizyImageView.class) {	// synchronize all info change and check
			mInfo = info;
		}
		load();
	}
	
	/**
	 * !note: put in synchronized(LaizyImageView.class) when not called from ui thread
	 * 
	 * @return
	 */
	public LaizyImageViewInfo getInfo() {
		return mInfo;
	}
	
	/**
	 * is this still the image I need to show?<br/>
	 * !note: always put in synchronized(LaizyImageView.class)
	 * 
	 * @return
	 */
	public boolean validate(LaizyImageViewInfo info) {
		return info.info.equals(mInfo.info);
	}
	
	/**
	 * stop loading
	 */
	public void stopLoading() {
		if (mImageLoader != null) {
			mImageLoader.stopLoading(mCurrentImageInfo, this);
		}
	}
	
	/**
	 * 
	 */
	public void reset() {
		stopLoading();
		synchronized(LaizyImageView.class) {
			mCurrentImageInfo = null;
			mInfo = null;
		}
	}

	/**
	 * load and show image - defaultRes is 	must be a valid resource - no image refresh will happen otherwise
	 */
	public void load() {
		if (mInfo == null || mInfo.info.url == null || mInfo.info.url.trim().length() == 0) {
			stopLoading();

			synchronized(LaizyImageView.class) {	// synchronize all info change
				mCurrentImageInfo = null;
			}
			showDefault();
			return;
		}
		
		ImageInfo infoSavedForStop;

		synchronized(LaizyImageView.class) {	// synchronize all info change and check
			if (mCurrentImageInfo != null && mInfo.info.equals(mCurrentImageInfo)) {
				
				// already loading this
				return;
			}
			infoSavedForStop = mCurrentImageInfo;
		}
		
		if (sImageLoader == null) {
			sImageLoader = Global.getImageLoaderInstance(getContext());
		}
		mImageLoader = sImageLoader;
		
		if (infoSavedForStop != null) {
			mImageLoader.stopLoading(infoSavedForStop, this);
		}

		synchronized(LaizyImageView.class) { // synchronize all info change and check
			mCurrentImageInfo = mInfo.info;
		}

		if (!mAlwaysLoadInBackground ) {
		
			// first try imageLoader's memory cache
			final Bitmap bmp = ImageLoader.getFromCache(mCurrentImageInfo);
			if (bmp != null) {
				setImage(bmp, false);
				return;
			}
			showDefault();
			
			startLoadingInBg();
		} else {
			showDefault();
			
			class FastImageLoaderThread extends Thread {
				
				ImageInfo mFastInfo;
				
				public FastImageLoaderThread(ImageInfo info) {
					mFastInfo = info;
				}
			}
			new FastImageLoaderThread(mCurrentImageInfo) {
				
				@Override
				public void run() {
					final Bitmap bmp = ImageLoader.getFromCache(mFastInfo);
					if (bmp != null) {
						synchronized(LaizyImageView.class) {
							if (mCurrentImageInfo == null) {
								return;
							} else if (mFastInfo.equals(mCurrentImageInfo)) {
								onLoadFinished(mFastInfo, bmp);
							}
						}
					} else {
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								synchronized(LaizyImageView.class) {
									if (mFastInfo.equals(mCurrentImageInfo)) {
										startLoadingInBg();
									}
								}
							}
						});
					}
				}
			}.start();
			
		}
	}
	
	private void startLoadingInBg() {
		if (mAnimFadeIn != null) {
			mAnimFadeIn.cancel();
			mAnimFadeIn = null;
			setAnimation(null);
		}

		showLoading();

		mImageLoader.load(mCurrentImageInfo, this);
	}

	@Override
	public void onLoadFinished(final ImageInfo info, final Bitmap result) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				synchronized(LaizyImageView.class) { // synchronize all info change and check
					if (mCurrentImageInfo == null) {
						return;
					} else if (mCurrentImageInfo.equals(info)) {
						setImage(result, mInfo.needFading);
						return;
					}
					
					// need to reload
					mCurrentImageInfo = null;
				}

				// need to reload
				load();
			}
		});
	}

	@Override
	public void onLoadFailed(final ImageInfo info) {

		// show error or default image
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				synchronized(LaizyImageView.class) { // synchronize all info change and check
					if (mCurrentImageInfo == null) {
						return;
					} else if (mCurrentImageInfo.equals(info)) {			
						showError();
					}
				}
			}
		});
	}
	
	/**
	 * 
	 * @param bmp
	 * @param fadeIn
	 */
	public void setImage(Bitmap bmp, boolean fadeIn) {
		synchronized(bmp) {
			if (bmp.isRecycled()) {
				synchronized(LaizyImageView.class) { // synchronize all info change and check
					mCurrentImageInfo = null;
				}
				load();
				return;
			}
			if (mNeedVisibility) {
				setVisibility(View.VISIBLE);
			}
			setImageBitmap(bmp);
			
			restoreScaleType();
			
			if (mInfo.isAnimated) {
				AnimationStarter.start(this);
			} else if (fadeIn) {
				mAnimFadeIn = new AlphaAnimation(0.80f, 1.00f);
				mAnimFadeIn.setDuration(350);
				setAnimation(mAnimFadeIn);
				mAnimFadeIn.start();
			}
			
			if (mOnImageSetListener != null) {
				mOnImageSetListener.onImageSet(this);
			}
			
			mImageState = STATE_SET;
		}
	}
	
	/**
	 * 
	 */
	public void showDefault() {
		mImageState = STATE_DEFAULT;
		
		if (mInfo == null || mInfo.defaultResID == NO_IMAGE) {
			return;
		}

		if (mInfo.defaultResID == HIDE_IMAGE) {
			mNeedVisibility = getVisibility() == View.VISIBLE;
			setVisibility(View.GONE);
		} else {
			setTempScaleType(mInfo.defaultScaleType != null ? mInfo.defaultScaleType : mDefaultScaleType);
			
			if (mNeedVisibility) {
				setVisibility(View.VISIBLE);
			}
			setImageResource(mInfo.defaultResID);

			if (mInfo.defaultIsAnimated) {				
				AnimationStarter.start(this);
			}
		}
		
		if (mOnImageSetListener != null) {
			mOnImageSetListener.onDefaultSet(this);
		}
	}
	
	/**
	 * 
	 */
	public void showLoading() {
		showLoading(mInfo);
		if (mOnImageSetListener != null) {
			mOnImageSetListener.onLoadingSet(this);
		}
	}
	
	/**
	 * 
	 * @param info
	 */
	public void showLoading(LaizyImageViewInfo info) {
		mImageState = STATE_LOADING;
		
		if (info == null || info.loadingResID == NO_IMAGE) {
			return;
		}

		if (info.loadingResID == HIDE_IMAGE) {
			mNeedVisibility  = getVisibility() == View.GONE;
			setVisibility(View.GONE);
		} else {
			if (mNeedVisibility) {
				setVisibility(View.VISIBLE);
			}
			setImageResource(info.loadingResID);
			
			setTempScaleType(mInfo.loadingScaleType != null ? mInfo.loadingScaleType : mLoadingScaleType);
			
			if (info.loadingIsAnimated) {				
				AnimationStarter.start(this);
			}
		}
	}
	
	/**
	 * 
	 */
	public void showError() {
		mImageState = STATE_ERROR;
		
		if (mInfo == null) {
			return;
		}
		
		if (mInfo.errorResID == NO_IMAGE || mInfo.errorResID == HIDE_IMAGE) {
			if (mInfo.defaultResID != NO_IMAGE && mInfo.defaultResID != HIDE_IMAGE) {
				showDefault();
			} else {
				mNeedVisibility = getVisibility() == View.VISIBLE;
				setVisibility(View.GONE);
			}
			return;
		}
		
		if (mNeedVisibility) {
			setVisibility(View.VISIBLE);
		}
		setImageResource(mInfo.errorResID);

		setTempScaleType(mInfo.errorScaleType != null ? mInfo.errorScaleType : mErrorScaleType);
		
		if (mOnImageSetListener != null) {
			mOnImageSetListener.onErrorSet(this);
		}
	}
	
	/**
	 * 
	 * @param scaleType
	 */
	protected void setTempScaleType(ScaleType scaleType) {
		if (scaleType == null) {
			return;
		}
		if (mOriginalScaleType == null) {
			mOriginalScaleType = getScaleType();
		}
		setScaleType(scaleType);
	}
	
	/**
	 * 
	 */
	protected void restoreScaleType() {
		if (mOriginalScaleType != null) {
			setScaleType(mOriginalScaleType);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isImageSet() {
		return mImageState == STATE_SET;
	}
	
	@Override
	public void setImageBitmap(Bitmap bmp) {
		if (bmp != null) {
			super.setImageBitmap(bmp);
		} else {
			load();
		}
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		try {
			final Bitmap bmp = ((BitmapDrawable) getDrawable()).getBitmap();
			if (bmp != null) {
				synchronized(bmp) {
					if (bmp.isRecycled()) {
Debug.logD("LaizyImageView", "bitmap was recylced at draw");
						synchronized(LaizyImageView.class) { // synchronize all info change and check
							mCurrentImageInfo = null;
						}
						load();
						return;
					}
				}
			} else {
				return;
			}
		} catch(Throwable e) {
			;
		}
		
		super.onDraw(canvas);
	}
	
	private void runOnUiThread(Runnable runnable) {
		final Handler handler = getHandler();
		if (handler != null) {
			handler.post(runnable);
		} else {
			final Activity activity = Global.getCurrentActivity();
			if (activity != null) {
				activity.runOnUiThread(runnable);
			}
		}
	}
	
	public static class LaizyImageViewInfo {
		
		public ImageInfo info;		
		public int errorResID;	// error image resource
		public ScaleType errorScaleType;
		public int loadingResID;	// loading image resource
		public ScaleType loadingScaleType;
		public boolean loadingIsAnimated;
		public int defaultResID;	// default image if no image set
		public ScaleType defaultScaleType;
		public boolean isAnimated;
		public boolean defaultIsAnimated;
		public boolean needFading = true;
		
		public LaizyImageViewInfo(String imageUrl, int defaultResID) {
			this(new ImageInfo(imageUrl), defaultResID);
		}

		/**
		 * 
		 * @param info
		 * @param defaultResID
		 */
		public LaizyImageViewInfo(ImageInfo info, int defaultResID) {
			this(info, defaultResID, -1, false, false);
		}
		
		/**
		 * 
		 * @param url
		 * @param defaultResID
		 * @param isThumbnail
		 */
		public LaizyImageViewInfo(String imageUrl, int defaultResID, boolean isThumbnail) {
			this(isThumbnail ? new ImageInfo("150x150", imageUrl) : new ImageInfo(imageUrl), defaultResID, -1, defaultResID, false, false, false);
		}
		
		/**
		 * 
		 * @param info
		 * @param defaultResID
		 * @param errorResID
		 * @param isAnimated
		 * @param defaultIsAnimated
		 */
		public LaizyImageViewInfo(ImageInfo info, int defaultResID, int errorResID, boolean isAnimated, boolean defaultIsAnimated) {
			this(info, defaultResID, errorResID, defaultResID, isAnimated, defaultIsAnimated, defaultIsAnimated);
		}
		
		/**
		 * 
		 * @param info
		 * @param defaultResID
		 * @param errorResID
		 * @param loadingResID
		 * @param isAnimated
		 * @param defaultIsAnimated
		 * @param loadingIsAnimated
		 */
		public LaizyImageViewInfo(ImageInfo info, int defaultResID, int errorResID, int loadingResID, boolean isAnimated, boolean defaultIsAnimated, boolean loadingIsAnimated) {
			this.info = info;
			this.defaultResID = defaultResID;
			this.errorResID = errorResID;
			this.isAnimated = isAnimated;
			this.defaultIsAnimated = defaultIsAnimated;
			this.loadingResID = loadingResID;
			this.loadingIsAnimated = loadingIsAnimated;
		}
	}

	public interface OnImageSetListener {
		public void onDefaultSet(LaizyImageView view);
		public void onLoadingSet(LaizyImageView view);
		public void onErrorSet(LaizyImageView view);
		public void onImageSet(LaizyImageView view);
	}
}
