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

import ds.framework.v4.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class OverlayedImageView extends ImageView {

	private Drawable mOverlay;
	
	public OverlayedImageView(Context context) {
		super(context);
	}

	public OverlayedImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public OverlayedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DsView, defStyle, 0);
		
		setOverlay(a.getResourceId(R.styleable.DsView_overlay, -1));
		
		a.recycle();
	}
	
	/**
	 * set the frame's resource
	 * 
	 * @param frameRes
	 */
	public void setOverlay(int overlayRes) {
		if (overlayRes != -1) {
			mOverlay = getContext().getResources().getDrawable(overlayRes);
		}
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mOverlay == null) {
			return;
		}
		
		final Drawable d = getDrawable();
		
		if (d == null) {
			return;
		}

		final int scrollX = getScrollX();
		final int scrollY = getScrollY();
		
		final Matrix matrix = getImageMatrix();
		final RectF rect = new RectF(d.getBounds());
			
		matrix.mapRect(rect);
		mOverlay.setBounds(0, 0, Math.round(rect.width()), Math.round(rect.height()));

		if ((scrollX | scrollY) == 0) {
			mOverlay.draw(canvas);
		} else {
			canvas.translate(scrollX, scrollY);
			mOverlay.draw(canvas);
			canvas.translate(-scrollX, -scrollY);
		}
	}
}
