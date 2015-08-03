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
import android.view.ViewGroup;

import java.security.InvalidParameterException;

public class WrapLayout extends ViewGroup {
	
	protected int mGravity;
    protected int[] mLineHeights;
    protected int[] mGravityMargin;

    public WrapLayout(Context context) {
        super(context);
    }

    public WrapLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
	}
    
    public WrapLayout(Context context, AttributeSet attrs){
    	super(context, attrs);
    	
    	mGravity = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android", "gravity", Gravity.LEFT);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            throw new InvalidParameterException("Invalid width constraint (UNSPECIFIED)");
        };

        int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        final int count = getChildCount();
        
    	int posX = 0;
        int posY = 0;
        int lineCount = 0;
        mLineHeights = new int[count];
        
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
            	
            	LayoutParams lp = (LayoutParams) child.getLayoutParams();
            	
                child.measure(
                		getChildMeasureSpec(
								widthMeasureSpec,
								child.getPaddingLeft() + child.getPaddingRight(), 
								lp.width),
                        getChildMeasureSpec(
								heightMeasureSpec,
								child.getPaddingTop() + child.getPaddingBottom(), 
								lp.height)
				);
                
                final int childw = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;

                if (posX > 0 && posX + childw > width) {
                    posX = 0;
                    if (lineCount == 0 && mLineHeights[lineCount] == 0) {
                    	
                    	// the first visible child is too big, but we don't know it's height yet
                    	mLineHeights[lineCount] = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
                    }
                    posY += mLineHeights[lineCount++];
                }
                
                mLineHeights[lineCount] = Math.max(mLineHeights[lineCount], child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                
                posX += childw;
            }
        }
        
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY){
        	height = posY + getPaddingTop() + getPaddingBottom() + (posX != 0 ? mLineHeights[lineCount] : 0);
        }
        
	    // Check against minimum height and width
	    height = Math.max(height, getSuggestedMinimumHeight());
	    width = Math.max(width, getSuggestedMinimumWidth());

	    setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        final int width = r - l;
        int posX = getPaddingLeft();
        int posY = getPaddingTop();

        countGravityMargins(width, count);
        
        int lineCount = 0;
        int at = 0;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final int childw = child.getMeasuredWidth();
                final int childh = child.getMeasuredHeight();
                
                LayoutParams lp = (LayoutParams) child.getLayoutParams();

                posX += mGravityMargin[i] + lp.leftMargin;
                if (posX + childw + lp.rightMargin > width && at != 0) {
                    posX = getPaddingLeft() + mGravityMargin[i] + lp.leftMargin;
                    posY += mLineHeights[lineCount++];
                }
                ++at;
                
                child.layout(posX, posY + lp.topMargin, posX + childw, posY + lp.topMargin + childh);
                posX += childw + lp.rightMargin;
            }
        }
    }
    
    protected void countGravityMargins(int width, int count) {
    	mGravityMargin = new int[count];
        
        if (mGravity == Gravity.CENTER || mGravity == Gravity.CENTER_HORIZONTAL
        		|| mGravity == Gravity.RIGHT) {
        	
        	for(int i = 0; i < count; i++) {
        		int lW = getPaddingLeft();
        		
        		int lineStart = i;

        		while(i < count) {
        			final View child = getChildAt(i);
                    if (child.getVisibility() == GONE) {
                    	continue;
                    }
                    
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    
                    final int childw = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;

        			if (lW + childw > width) {
	        			break;
                    }
        			lW += childw;
        			++i;
        		}
        		
        		if (i == lineStart) {
        			
        			// even one is too big
        			continue;
        		}

        		if (mGravity != Gravity.RIGHT) {
	                final int addPerChild = (width - lW - getPaddingRight()) / (i - lineStart);
	                if (addPerChild <= 0) {
	                	
	                	// no space left
	                	continue;
	                }
	                
	                mGravityMargin[lineStart++] = addPerChild / 2;
	                
	                for(int j = lineStart; j < i; ++j) {
	                	mGravityMargin[j] = addPerChild;
	                }
        		} else {
        			mGravityMargin[lineStart] = width - lW - getPaddingRight();
        		}
                
                if (i != count) {
                	--i;
                }
        	}
        }
    }
    
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new WrapLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }


    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof WrapLayout.LayoutParams;
    }

    
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {

    	public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }
    	
    	public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }
    }
}