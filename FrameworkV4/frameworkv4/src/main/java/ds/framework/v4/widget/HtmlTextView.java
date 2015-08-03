package ds.framework.v4.widget;

import ds.framework.v4.Global;
import ds.framework.v4.io.ImageLoader;
import ds.framework.v4.io.ImageLoader.ImageInfo;
import ds.framework.v4.io.LaizyLoader.Callback;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.AttributeSet;
import android.widget.TextView;

public class HtmlTextView extends TextView implements ImageGetter, Callback<ImageInfo, Bitmap> {
	
	private int mDefaultResID;
	private String mHtmlText;
	private static ImageLoader sImageLoader;
	private ImageLoader mImageLoader;

	public HtmlTextView(Context context) {
		super(context);
	}
	
	public HtmlTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public HtmlTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * 
	 * @param textResID
	 */
	public void setHtmlText(int textResID, int defaultResID) {
		setHtmlText(getResources().getString(textResID), defaultResID);
	}
	
	/**
	 * 
	 * @param htmlText
	 */
	synchronized public void setHtmlText(String htmlText, int defaultResID) {
		if (sImageLoader == null) {
			sImageLoader = Global.getImageLoaderInstance(getContext());
		}
		mImageLoader = sImageLoader;
		
		mHtmlText = htmlText;
		mDefaultResID = defaultResID;
		
		setText(Html.fromHtml(mHtmlText, this, null));
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		try {
			super.onDraw(canvas);
		} catch(Throwable e) {
			setText(Html.fromHtml(mHtmlText, this, null));
		}
	}

	@Override
	public Drawable getDrawable(String source) {
		final Bitmap bmp = ImageLoader.getFromCache(new ImageInfo(source));
		if (bmp != null) {
			BitmapDrawable ret = new BitmapDrawable(getResources(), bmp);
			
			ret.setBounds(0, 0, 
					ret.getIntrinsicWidth(), 
					ret.getIntrinsicHeight()
			);
			return ret;
		}
		
		
		final int iw = getWidth() - getPaddingLeft() - getPaddingRight();
		
		mImageLoader.load(new ImageInfo(iw + "X" + (int) Global.getScreenHeight(), source), this);
		
		return getResources().getDrawable(mDefaultResID);
	}

	@Override
	public void onLoadFinished(ImageInfo item, Bitmap result) {
		post(new Runnable() {
			@Override
			public void run() {
				synchronized(this) {
					setHtmlText(mHtmlText, mDefaultResID);
				}	
			}
		});
	}

	@Override
	public void onLoadFailed(ImageInfo item) {
		;	// do nothing (for now)
	}
}
