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
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

public class CenteredWrapLayout extends WrapLayout {

    public CenteredWrapLayout(Context context) {
        super(context);
    }

    public CenteredWrapLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
	}
    
    public CenteredWrapLayout(Context context, AttributeSet attrs){
    	super(context, attrs);
    }
    
    @Override
    protected void countGravityMargins(int width, int count) {
    	mGravityMargin = new int[count];
		int addPerChild = 0;
		boolean firstLine = true;
        
    	for(int i = 0; i < count; i++) {
    		int lW = getPaddingLeft();
    		
    		if (mGravity == Gravity.LEFT) {
    			
    			// add its width as that still counts
    			final View child = getChildAt(i);
    			
    			LayoutParams lp = (LayoutParams) child.getLayoutParams();
    			
                if (child.getVisibility() == GONE) {
                	continue;
                }
                lW += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;

    					
    			// no padding for first item
    			++i;
    		}
    		int lineStart = i;

    		while(i < count) {
    			final View child = getChildAt(i);
    			
    			LayoutParams lp = (LayoutParams) child.getLayoutParams();
    			
                if (child.getVisibility() == GONE) {
                	continue;
                }
                final int childw = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;

    			if (lW + childw > width) {
        			break;
                }
    			lW += childw;
    			++i;
    		}
    		
    		if (i == lineStart) {
    			
    			// even one is too big
    			firstLine = false;
    			continue;
    		}
            
            if (i != count || firstLine || mGravity == Gravity.CENTER || mGravity == Gravity.CENTER_HORIZONTAL) {
                addPerChild = (width - lW - getPaddingRight()) / (i - lineStart);
                if (addPerChild <= 0) {
                	addPerChild = 0;
                	
                	// no space left
                	continue;
                }
            	mGravityMargin[lineStart++] = addPerChild / 2;
            } else if (mGravity == Gravity.RIGHT) {
            	
            	// use last addPerChild
            	mGravityMargin[lineStart] = width - getPaddingRight() - lW - (i - lineStart) * addPerChild + addPerChild / 2;
            	++lineStart;
            } else if (mGravity == Gravity.LEFT) {

            	// use last addPerChild
            	mGravityMargin[lineStart++] = addPerChild / 2;
            }
            
            for(int j = lineStart; j < i; ++j) {
            	mGravityMargin[j] = addPerChild;
            }
            
            if (i != count) {
            	--i;
            }
            firstLine = false;
    	}
    }
}
