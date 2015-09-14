package ds.framework.v4.widget;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import ds.framework.v4.Global;

public class ImageViewResizer {

	public final static int TYPE_FROM_WIDTH_FULLSCREEN = 0;
	public final static int TYPE_FROM_HEIGHT = 1;
	public final static int TYPE_FROM_WIDTH = 2;
	
	public static ImageViewResizer sInstance;
	
	@SuppressLint("UseSparseArrays")
	private final HashMap<Integer, PointF> sDrawableSizes = new HashMap<Integer, PointF>();
	
	private int mType = TYPE_FROM_WIDTH_FULLSCREEN;
	private int mWidth;
	private int mHeight;
	
	public static ImageViewResizer getInstance() {
		if (sInstance == null) {
			sInstance = new ImageViewResizer();
		}
		return sInstance;
	}
	
	/**
	 * 
	 * @param type
	 */
	public void setType(int type) {
		mType = type;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getWidth() {
		return mWidth;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getHeight() {
		return mHeight;
	}
	
	/**
	 * 
	 * @param context
	 * @param view
	 * @param sizeSourceResID
	 */
	public void resize(Context context, View view, int sizeSourceResID) {
		if (this == sInstance || mWidth == 0 || mHeight == 0) {
			final PointF dsize = getSize(context, sizeSourceResID, view);
			
			switch(mType) {
				case TYPE_FROM_WIDTH_FULLSCREEN:
					mWidth = (int) Global.getScreenWidth();
					mHeight = (int) (mWidth * dsize.y / dsize.x);
					break;
					
				case TYPE_FROM_WIDTH:
					mWidth = view.getLayoutParams().width;
					mHeight = (int) (mWidth * dsize.y / dsize.x);
					break;
					
				case TYPE_FROM_HEIGHT:
					mHeight = view.getLayoutParams().height;
					mWidth = (int) (mHeight * dsize.x / dsize.y);
					break;
			}
		}
		
		final LayoutParams lp = view.getLayoutParams();
		lp.width = mWidth;
		lp.height = mHeight;
	}
	
	private PointF getSize(Context context, int sizeSourceResID, View view) {
		if (!sDrawableSizes.containsKey(sizeSourceResID)) {
			final Drawable drawable = context.getResources().getDrawable(sizeSourceResID);
			
			sDrawableSizes.put(sizeSourceResID, new PointF(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));	
		}
		
		return sDrawableSizes.get(sizeSourceResID);
	}
}
