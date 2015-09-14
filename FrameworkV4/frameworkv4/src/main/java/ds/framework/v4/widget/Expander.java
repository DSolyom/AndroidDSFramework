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

import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import ds.framework.v4.common.Interpolators;

public class Expander {
	
	private boolean mFinished;
	private Interpolator mInterpolator;
	private long mStartTime;
	private int mDuration;
	private int mTimePassed;
	private float mDurationReciprocal;
	private int mPos;
	private int mStart;
	private float mDelta;
	private int mFinal;

	public Expander() {
        this(null);
    }
	
    public Expander(Interpolator interpolator) {
        mFinished = true;
        mInterpolator = interpolator;
    }
    
    public final boolean isFinished() {
        return mFinished;
    }
    
    public void start(int pos, int finalPos, int duration) {
        mFinished = false;
        mDuration = duration;
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
        mTimePassed = 0;
        mStart = pos;
        mFinal = finalPos;
        mDelta = finalPos - pos;
        mDurationReciprocal = 1.0f / (float) mDuration;
    }
    
    public void restart(int finalPos) {
    	restart(finalPos, mTimePassed);
    }
    
    public void restart(int finalPos, int duration) {
    	mFinished = false;
    	mDuration = duration;
    	mStartTime = AnimationUtils.currentAnimationTimeMillis();
    	mTimePassed = 0;
    	mStart = mFinal;
    	mFinal = finalPos;
    	mDelta = finalPos - mFinal;
    	mDurationReciprocal = 1.0f / (float) mDuration;
    }
    
    public boolean compute() {
        if (mFinished) {
            return false;
        }

        mTimePassed = (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime);
    
        if (mTimePassed < mDuration) {
            float p = (float) mTimePassed * mDurationReciprocal;

            if (mInterpolator == null) {
                p = Interpolators.viscousFluid(p); 
            } else {
                p = mInterpolator.getInterpolation(p);
            }

            mPos = mStart + Math.round(p * mDelta);
        } else {
            mPos = mFinal;
            mTimePassed = mDuration;
        }
        mFinished = (mPos == mFinal);
        
        return true;
    }
    
    public int getPos() {
    	return mPos;
    }
    
    public void abort() {
    	mFinished = true;
    }
}
