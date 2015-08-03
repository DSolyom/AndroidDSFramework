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

import android.content.Context;
import android.support.v4.view.ViewPagerModByDS;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class DSViewPager extends ViewPagerModByDS {

	private boolean mEnabled = true;
	
	public DSViewPager(Context context) {
		super(context);
	}
	
	public DSViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mEnabled ? super.onTouchEvent(event) : false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
    	return mEnabled ? super.onInterceptTouchEvent(event) : false;

    }
    
    public boolean isEnabled() {
    	return mEnabled;
    }
 
    /**
     * can page?
     */
    public void setEnabled(boolean enabled) {
    	mEnabled = enabled;
    }
}
