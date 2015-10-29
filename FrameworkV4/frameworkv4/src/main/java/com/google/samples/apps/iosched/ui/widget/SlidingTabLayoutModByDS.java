/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.iosched.ui.widget;

import android.content.Context;
import android.support.v4.view.ViewPagerModByDS;
import android.util.AttributeSet;
import android.view.View;

/**
 * To be used with ViewPagerModByDS to provide a tab indicator component which give constant feedback as to
 * the user's scroll progress.
 * <p>
 * To use the component, simply add it to your view hierarchy. Then in your
 * {@link android.app.Activity} or {@link android.support.v4.app.Fragment} call
 * {@link #setViewPager(ViewPagerModByDS)} providing it the ViewPagerModByDS this layout is being used for.
 * <p>
 * The colors can be customized in two ways. The first and simplest is to provide an array of colors
 * via {@link #setSelectedIndicatorColors(int...)}. The
 * alternative is via the {@link TabColorizer} interface which provides you complete control over
 * which color is used for any individual position.
 * <p>
 * The views used as tabs can be customized by calling {@link #setCustomTabView(int, int)},
 * providing the layout ID of your custom layout.
 *
 * moved abstract parts to AbsSlidingTabLayout
 */
public class SlidingTabLayoutModByDS extends AbsSlidingTabLayout {

    private ViewPagerModByDS mViewPager;
    private ViewPagerModByDS.OnPageChangeListener mViewPagerModByDSPageChangeListener;

    public SlidingTabLayoutModByDS(Context context) {
        this(context, null);
    }

    public SlidingTabLayoutModByDS(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabLayoutModByDS(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Set the {@link ViewPagerModByDS.OnPageChangeListener}. When using {@link SlidingTabLayoutModByDS} you are
     * required to set any {@link ViewPagerModByDS.OnPageChangeListener} through this method. This is so
     * that the layout can update it's scroll position correctly.
     *
     * @see ViewPagerModByDS#setOnPageChangeListener(ViewPagerModByDS.OnPageChangeListener)
     */
    public void setOnPageChangeListener(ViewPagerModByDS.OnPageChangeListener listener) {
        mViewPagerModByDSPageChangeListener = listener;
    }

    /**
     * Sets the associated view pager. Note that the assumption here is that the pager content
     * (number of tabs and tab titles) does not change after this call has been made.
     */
    public void setViewPager(ViewPagerModByDS ViewPagerModByDS) {
        mViewPager = ViewPagerModByDS;
        if (ViewPagerModByDS != null) {
            ViewPagerModByDS.setOnPageChangeListener(new InternalViewPagerModByDSListener());
        }
        populateTabStrip();
    }

    /**
     *
     * @return
     */
    @Override
    public int getCount() {
        return mViewPager.getAdapter().getCount();
    }

    @Override
    protected int getCurrentItem() {
        if (mViewPager == null) {
            return -1;
        }
        return mViewPager.getCurrentItem();
    }

    /**
     * !note: make sure your adapter implements SlidingTabAdapterInterface
     *
     * @return
     */
    @Override
    protected SlidingTabAdapterInterface getAdapter() {
        return (SlidingTabAdapterInterface) mViewPager.getAdapter();
    }

    @Override
    protected void onCurrentItemSet(int position) {
        mViewPager.setCurrentItem(position);
    }

    private class InternalViewPagerModByDSListener implements ViewPagerModByDS.OnPageChangeListener {
        private int mScrollState;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int tabStripChildCount = mTabStrip.getChildCount();
            if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
                return;
            }

            mTabStrip.onSelectedTabChanged(position, positionOffset);

            View selectedTitle = mTabStrip.getChildAt(position);
            int extraOffset = (selectedTitle != null)
                    ? (int) (positionOffset * selectedTitle.getWidth())
                    : 0;
            scrollToTab(position, extraOffset);

            if (mViewPagerModByDSPageChangeListener != null) {
                mViewPagerModByDSPageChangeListener.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mScrollState = state;

            if (mViewPagerModByDSPageChangeListener != null) {
                mViewPagerModByDSPageChangeListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (mScrollState == ViewPagerModByDS.SCROLL_STATE_IDLE) {
                mTabStrip.onSelectedTabChanged(position, 0f);
                scrollToTab(position, 0);
            }
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                mTabStrip.getChildAt(i).setSelected(position == i);
            }
            if (mViewPagerModByDSPageChangeListener != null) {
                mViewPagerModByDSPageChangeListener.onPageSelected(position);
            }
        }

    }

}