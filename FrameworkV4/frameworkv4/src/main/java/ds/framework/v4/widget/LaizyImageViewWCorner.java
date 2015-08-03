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

	used code for configureBounds from android.widget.ImageView

 	which is:
 	
		Copyright (C) 2006 The Android Open Source Project
	 	
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
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import ds.framework.v4.R;

public class LaizyImageViewWCorner extends LaizyImageView {

	public static final String TAG = "LaizyImageViewWCorner";
	
	public static final int DEFAULT_PAINT_FLAGS = 
			Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG;
	
	private float mCornerRadius;
	private int mBorderWidth;
	private int mBorderColor;

	private ScaleType mScaleType;

	private boolean mHaveFrame;

	private Matrix mDrawMatrix = new Matrix();
	
	final RectF mTempSrc = new RectF();
	final RectF mTempDst = new RectF();
	
	private static final ScaleType[] SCALE_TYPES = {
        ScaleType.MATRIX,
        ScaleType.FIT_XY,
        ScaleType.FIT_START,
        ScaleType.FIT_CENTER,
        ScaleType.FIT_END,
        ScaleType.CENTER,
        ScaleType.CENTER_CROP,
        ScaleType.CENTER_INSIDE
	};
	
	public LaizyImageViewWCorner(Context context) {
		super(context);
	}
	
	public LaizyImageViewWCorner(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public LaizyImageViewWCorner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DsView, defStyle, 0);

        int index = a.getInt(R.styleable.DsView_android_scaleType, 5);
        if (index >= 0) {
            setScaleType(SCALE_TYPES[index]);
        }
		
		mCornerRadius = a.getDimensionPixelSize(R.styleable.DsView_corner_radius, 0);
		mBorderWidth = a.getDimensionPixelSize(R.styleable.DsView_border_width, 0);
		
		mBorderColor = a.getColor(R.styleable.DsView_border_color, android.R.color.transparent);
		
		a.recycle();

	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		if (drawable != null) {
			drawable = BitmapDrawableWCorner.convertDrawable(drawable, mCornerRadius, mBorderWidth, mBorderColor);
		}
		super.setImageDrawable(drawable);
		
		configureBounds();
	}
	
	public void setImageBitmap(Bitmap bm) {
		if (bm != null) {
			super.setImageDrawable(new BitmapDrawableWCorner(bm, mCornerRadius, mBorderWidth, mBorderColor));
		} else {
			load();
			super.setImageDrawable(null);
		}
		configureBounds();
    }
	
	// Added for compatibility By DS
	@Override
	public void setImageResource(int resID) {
		setImageDrawable(getContext().getResources().getDrawable(resID));
	}
	
	// Added for compatibility By DS
	@Override
	public void setImageURI(Uri uri) {
		super.setImageURI(uri);
		
		setImageDrawable(getDrawable());
	}
	
	@Override
	public void setScaleType(ScaleType scaleType) {
		super.setScaleType(ScaleType.FIT_XY);
		
		final Drawable drawable = getDrawable();
		if (drawable instanceof BitmapDrawableWCorner) {
			if (mScaleType != scaleType) {
				mScaleType = scaleType;
				
				setWillNotCacheDrawing(true);
				configureBounds();
			}
		} else {
			mScaleType = scaleType;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public float getCornerRadius() {
		return mCornerRadius;
	}

	/**
	 * 
	 * @return
	 */
	public int getBorderWidth() {
		return mBorderWidth;
	}

	/**
	 * 
	 * @return
	 */
	public int getBorderColor() {
		return mBorderColor;
	}

	/**
	 * 
	 * @param radius
	 */
	public void setCornerRadius(float radius) {
		mCornerRadius = radius;
		
		final Drawable drawable = getDrawable();
		if (drawable instanceof BitmapDrawableWCorner) {
			((BitmapDrawableWCorner) getDrawable()).setCornerRadius(mCornerRadius);
			
			invalidate();
		}
	}

	/**
	 * 
	 * @param width
	 */
	public void setBorderWidth(int width) {
		mBorderWidth = width;
		
		final Drawable drawable = getDrawable();
		if (drawable instanceof BitmapDrawableWCorner) {
			((BitmapDrawableWCorner) getDrawable()).setBorderWidth(mBorderWidth);
			
			configureBounds();
		}
	}

	/**
	 * 
	 * @param color
	 */
	public void setBorderColor(int color) {
		mBorderColor = color;
		
		final Drawable drawable = getDrawable();
		if (drawable instanceof BitmapDrawableWCorner) {
			((BitmapDrawableWCorner) getDrawable()).setBorderColor(mBorderColor);
			
			invalidate();
		}
	}
	
	@Override
    protected boolean setFrame(int l, int t, int r, int b) {
		final boolean ret = super.setFrame(l, t, r, b);
		mHaveFrame = true;
		configureBounds();
		return ret;
	}
	
	private void configureBounds() {
		final BitmapDrawableWCorner drawable = (BitmapDrawableWCorner) getDrawable();
		
        if (drawable == null || !mHaveFrame) {
            return;
        }

        int dwidth = drawable.getIntrinsicWidth();
        int dheight = drawable.getIntrinsicHeight();

        int vwidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int vheight = getHeight() - getPaddingTop() - getPaddingBottom();

        drawable.setBounds(0, 0,  vwidth, vheight);
        
        mDrawMatrix.set(null);
        
        if (dwidth <= 0 || dheight <= 0 || ScaleType.FIT_XY == mScaleType) {
            
            drawable.setDrawBounds(mDrawMatrix, false);
        } else {
            
            if (ScaleType.CENTER == mScaleType) {
                // Center bitmap in view, no scaling.
                mDrawMatrix.setTranslate((vwidth - dwidth) * 0.5f,
                                         (vheight - dheight) * 0.5f);
                
                drawable.setDrawBounds(mDrawMatrix, false);
            } else if (ScaleType.CENTER_CROP == mScaleType) {
                float scale;
                float dx = 0, dy = 0;

                if (dwidth * vheight > vwidth * dheight) {
                    scale = (float) vheight / (float) dheight; 
                    dx = (vwidth - dwidth * scale) * 0.5f;
                } else {
                    scale = (float) vwidth / (float) dwidth;
                    dy = (vheight - dheight * scale) * 0.5f;
                }

                mDrawMatrix.setScale(scale, scale);
                mDrawMatrix.postTranslate(dx, dy);
                
                drawable.setDrawBounds(mDrawMatrix, false);
            } else if (ScaleType.CENTER_INSIDE == mScaleType) {
                float scale;
                float dx;
                float dy;
                
                if (dwidth <= vwidth && dheight <= vheight) {
                    scale = 1.0f;
                } else {
                    scale = Math.min((float) vwidth / (float) dwidth, 
                            (float) vheight / (float) dheight);
                }
                
                dx = (vwidth - dwidth * scale) * 0.5f;
                dy = (vheight - dheight * scale) * 0.5f;

                mDrawMatrix.setScale(scale, scale);
                mDrawMatrix.postTranslate(dx, dy);
                
                drawable.setDrawBounds(mDrawMatrix, true);
            } else if (ScaleType.FIT_CENTER == mScaleType) {
            	
                mTempSrc.set(0, 0, dwidth, dheight);
                mTempDst.set(0, 0, vwidth, vheight);
                
                mDrawMatrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.CENTER);
                
                drawable.setDrawBounds(mDrawMatrix, true);
            } else if (ScaleType.FIT_END == mScaleType) {
            	
                mTempSrc.set(0, 0, dwidth, dheight);
                mTempDst.set(0, 0, vwidth, vheight);
                
                mDrawMatrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.END);
                
                drawable.setDrawBounds(mDrawMatrix, true);
            } else if (ScaleType.FIT_START == mScaleType) {
            	
                mTempSrc.set(0, 0, dwidth, dheight);
                mTempDst.set(0, 0, vwidth, vheight);
                
                mDrawMatrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.START);
                
                drawable.setDrawBounds(mDrawMatrix, true);
            } else if (ScaleType.MATRIX == mScaleType) {
            	
            	drawable.setDrawBounds(getImageMatrix(), true);
            }
        }
		
		requestLayout();
		invalidate();
	}
	
	/**
	 * @class ImageDrawableWCorner
	 */
	public static class BitmapDrawableWCorner extends Drawable {

		protected BitmapWCornerState mState;
		
		private int mBitmapWidth;
		private int mBitmapHeight;
		
		public BitmapDrawableWCorner() {
			
		}
		
		public BitmapDrawableWCorner(Bitmap bm, float mCornerRadius,
				int mBorderWidth, int mBorderColor) {
			mState = new BitmapWCornerState(bm, mCornerRadius, mBorderWidth, mBorderColor);
			mBitmapWidth = bm.getWidth();
			mBitmapHeight = bm.getHeight();
		}

		/**
		 * 
		 * @param borderColor
		 */
		public void setBorderColor(int borderColor) {
			mState.mBorderPaint.setColor(borderColor);
		}

		/**
		 * 
		 * @param borderWidth
		 */
		public void setBorderWidth(int borderWidth) {
			mState.mBorderWidth = borderWidth;
		}

		/**
		 * 
		 * @param cornerRadius
		 */
		public void setCornerRadius(float cornerRadius) {
			mState.mCornerRadius = cornerRadius;
		}
		
		@Override
		public void onBoundsChange(Rect bounds) {
			super.onBoundsChange(bounds);
			
			mState.mBounds = new RectF(bounds);
		}
		
		/**
		 * call after setBounds 
		 * 
		 * @param drawMatrix
		 */
		public void setDrawBounds(Matrix drawMatrix, boolean mapRect) {
			mState.mBorderBounds = new RectF(mState.mBounds);
			if (mapRect) {
				drawMatrix.mapRect(mState.mBorderBounds);
			}
				
			mState.mBounds = new RectF(mState.mBounds.left + mState.mBorderWidth, 
					mState.mBounds.top + mState.mBorderWidth, 
					mState.mBounds.right - mState.mBorderWidth, 
					mState.mBounds.bottom - mState.mBorderWidth
			);
			mState.mShader.setLocalMatrix(drawMatrix);
		}
		
		@Override
		public void draw(Canvas canvas) {
			if (mState.mBorderWidth > 0) {
				canvas.drawRoundRect(mState.mBorderBounds, mState.mCornerRadius, mState.mCornerRadius, mState.mBorderPaint);
			}
			canvas.drawRoundRect(mState.mBounds, Math.max(mState.mCornerRadius - mState.mBorderWidth, 0), 
					Math.max(mState.mCornerRadius - mState.mBorderWidth, 0), mState.mPaint);
		}
		
		@Override
		public int getIntrinsicWidth() {
			return mBitmapWidth;
		}
		
		@Override
		public int getIntrinsicHeight() {
			return mBitmapHeight;
		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}

		@Override
		public void setAlpha(int alpha) {
			mState.mPaint.setAlpha(alpha);
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			mState.mPaint.setColorFilter(cf);
		}
		
		@Override
		public final ConstantState getConstantState() {
			return mState;
		}
		
		/**
		 * 
		 * @param drawable
		 * @param radius
		 * @param border
		 * @param borderColor
		 * @return
		 */
		public static Drawable convertDrawable(Drawable drawable, float radius, int border, int borderColor) {
			if (drawable != null) {
				if (drawable instanceof TransitionDrawable) {
			    	TransitionDrawable td = (TransitionDrawable) drawable;
			    	int num = td.getNumberOfLayers();
			    	
			    	Drawable[] drawableList = new Drawable[num];
			    	for (int i = 0; i < num; i++) {
			    		drawableList[i] = getBitmapDrawableWCornerFromDrawable(
			    				td.getDrawable(i), radius, border, borderColor
			    		);
			    	}
			    	return new TransitionDrawable(drawableList);
			    }
				
				return getBitmapDrawableWCornerFromDrawable(drawable, radius, border, borderColor);
			}
			return drawable;
		}

		public static Drawable getBitmapDrawableWCornerFromDrawable(Drawable drawable,
				float radius, int border, int borderColor) {
		    if (drawable instanceof BitmapDrawable) {
		        return new BitmapDrawableWCorner(
		        		((BitmapDrawable) drawable).getBitmap(), 
		        		radius, border, borderColor
		        );
		    } else if (drawable instanceof ColorDrawable) {
    			return drawable;
    		}
		    
		    Bitmap bitmap = null;
		    final int width = drawable.getIntrinsicWidth();
		    final int height = drawable.getIntrinsicHeight();
		    if (width > 0 && height > 0) {
			    bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			    Canvas canvas = new Canvas(bitmap); 
			    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			    drawable.draw(canvas);
		    }

		    return new BitmapDrawableWCorner(bitmap, radius, border, borderColor);
		}
		
		/**
		 * @class BitmapWCornerState
		 */
		public class BitmapWCornerState extends ConstantState {

			Bitmap mBitmap;
			
			float mCornerRadius;
			
			RectF mBounds = new RectF();
			Paint mPaint = new Paint(DEFAULT_PAINT_FLAGS);
			BitmapShader mShader;
			
			Paint mBorderPaint = new Paint(DEFAULT_PAINT_FLAGS);
			RectF mBorderBounds = new RectF();
			int mBorderWidth;
			
			public BitmapWCornerState(Bitmap bitmap) {
				mBitmap = bitmap;
				mShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
			}
			
			public BitmapWCornerState(Bitmap bitmap, float cornerRadius, int borderWidth, int borderColor) {
				this(bitmap);
				
				mCornerRadius = cornerRadius;
				mBorderWidth = borderWidth;				
				mBorderPaint.setColor(borderColor);

				mPaint.setShader(mShader);
			}
			
			public BitmapWCornerState(BitmapWCornerState otherState) {
				mPaint = new Paint(otherState.mPaint);
				mBorderPaint = new Paint(otherState.mBorderPaint);
				mBitmap = otherState.mBitmap;
				mCornerRadius = otherState.mCornerRadius;
				mBorderBounds = new RectF(otherState.mBorderBounds);
				
				mShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
				mPaint.setShader(mShader);
				mPaint.setStrokeWidth(10);
				mPaint.setStyle(Style.FILL_AND_STROKE);
			}
			
			@Override
			public int getChangingConfigurations() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Drawable newDrawable() {
				return new BitmapDrawableWCorner();
			}
			
		}
	}
}
