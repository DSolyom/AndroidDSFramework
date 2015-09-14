package ds.framework.v4.widget;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;
import ds.framework.v4.R;
import ds.framework.v4.common.Debug;

public class AnimGifView extends View {
	
	private Movie mMovie;
    private long mMovieStart;
    private long mMovieDuration;
    
	private int mAnimGifResID;
	
	public AnimGifView(Context context) {
		this(context, null, 0);
	}

	public AnimGifView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AnimGifView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		try {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		} catch(Throwable e) {
			;
		}
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DsView, 0, 0);

		mAnimGifResID = a.getResourceId(R.styleable.DsView_gif_src, 0);		
		
		a.recycle();
		
		if (mAnimGifResID != 0) {
			InputStream is = context.getResources().openRawResource(mAnimGifResID);
			mMovie = Movie.decodeStream(is);
			try {
				is.close();
			} catch (IOException e) {
				;
			}
			mMovieDuration = mMovie.duration();
			if (mMovieDuration == 0) {
				mMovieDuration = 1000;
			}
		}
	}

	@Override 
	protected void onDraw(Canvas canvas) {
        if (mMovie != null) {
            long now = android.os.SystemClock.uptimeMillis();
            if (mMovieStart == 0) {
                mMovieStart = now;
            }
            
            final float vwidth = getWidth();
            final float vheight = getHeight();
            final float mwidth = mMovie.width();
            final float mheight = mMovie.height();
            
            final float wscale = vwidth / mwidth;
            final float hscale = vheight / mheight;
            
            canvas.save();
            canvas.scale(wscale, hscale);
            
            mMovie.setTime((int) ((now - mMovieStart) % mMovieDuration));
            mMovie.draw(canvas, 0, 0);
            
            canvas.restore();
            
            invalidate();
        }
    }
	
}
