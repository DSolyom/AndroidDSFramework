/*
	Copyright 2012 Dániel Sólyom

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
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class IGallery extends android.widget.Gallery {

	protected boolean mMoveOnlyOne = true;
	
	public IGallery(Context context) {
		super(context);
	}

	public IGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public IGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {
		  return e2.getX() > e1.getX();
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (!mMoveOnlyOne) {
			return super.onFling(e1, e2, velocityX, velocityY);
		}
		
		int kEvent;
		if(isScrollingLeft(e1, e2)) { 
			
			//Check if scrolling left
			kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
		} else{ 
			
			//Otherwise scrolling right
			kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
		}
		onKeyDown(kEvent, null);
		return true;  
	}
}
