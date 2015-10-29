package com.google.samples.apps.iosched.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by solyom on 27/10/15.
 */
public class StandaloneSlidingTabLayout extends AbsSlidingTabLayout {

    private int mCurrentItem;

    private SlidingTabAdapterInterface mAdapter;
    private OnTabSelectListener mListener;

    public StandaloneSlidingTabLayout(Context context) {
        super(context);
    }

    public StandaloneSlidingTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StandaloneSlidingTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     *
     * @param adapter
     */
    public void setAdapter(SlidingTabAdapterInterface adapter) {
        mAdapter = adapter;
        populateTabStrip();
    }

    /**
     *
     * @param listener
     */
    public void setOnTabSelectListener(OnTabSelectListener listener) {
        mListener = listener;
    }

    @Override
    public int getCount() {
        return mAdapter != null ? mAdapter.getCount() : 0;
    }

    @Override
    protected int getCurrentItem() {
        return mCurrentItem;
    }

    /**
     *
     * @param currentItem
     */
    public void setCurrentItem(int currentItem) {
        onCurrentItemSet(currentItem);
    }

    @Override
    protected SlidingTabAdapterInterface getAdapter() {
        return mAdapter;
    }

    @Override
    protected void onCurrentItemSet(int position) {
        if (mListener.onTabSelected(position)) {
            mCurrentItem = position;
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                mTabStrip.getChildAt(i).setSelected(mCurrentItem == i);
            }
            scrollToTab(mCurrentItem, 0, true);
            mTabStrip.onSelectedTabChanged(mCurrentItem, 0);
        }
    }

    public interface OnTabSelectListener {
        public boolean onTabSelected(int position);
    }
}
