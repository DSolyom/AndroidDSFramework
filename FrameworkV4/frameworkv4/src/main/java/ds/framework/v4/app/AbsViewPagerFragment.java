package ds.framework.v4.app;

import android.os.Bundle;
import android.support.v4.view.ViewPagerModByDS;
import android.support.v4.view.ViewPagerModByDS.OnPageChangeListener;
import android.view.View;
import ds.framework.v4.app.AbsDSListFragment.AbsListData;
import ds.framework.v4.data.AbsAsyncData;
import ds.framework.v4.template.Template;
import ds.framework.v4.widget.AbsTemplatePagerAdapter;

abstract public class AbsViewPagerFragment extends AbsDSAsyncDataFragment {

	protected AbsTemplatePagerAdapter mAdapter;
	
	private int mSavedPosition;
	
	protected ViewPagerModByDS mPager;
	
	public AbsViewPagerFragment() {
		super();
	}
	
	public AbsViewPagerFragment(boolean isDialog) {
		super(isDialog);
	}

	@Override
	protected void onViewCreated(View rootView) {
		mPager = null;
		
		super.onViewCreated(rootView);
		
		mPager = (ViewPagerModByDS) mTemplate.findViewById(getPagerID());
	}
	
	@Override
	public void loadData() {
		ensureAdapter();
		super.loadData();
	}
	
	/**
	 * 
	 */
	protected void ensureAdapter() {
		if (mAdapter == null) {
			mAdapter = getNewAdapter();
		}
	}
	
	@Override
	public void onDataLoaded(AbsAsyncData data, int loadId) {		
		if (data instanceof AbsListData) {
			setAdapterData((AbsListData) data, loadId);
		}
		
		super.onDataLoaded(data, loadId);
	}
	
	@Override
	public void display() {
		super.display();
		
		if (mPager != null) {
			mTemplate.fill(mPager, mAdapter, Template.ADAPTER, null);
		}
		
		mPager.setCurrentItem(mSavedPosition, false);
		
		mPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
            	AbsViewPagerFragment.this.onPageSelected(position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, 
            		int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            	
            }
		});
	}

    /**
     *
     * @param page
     * @param smoothScrolling
     */
    public void selectPage(int page, boolean smoothScrolling) {
        if (mPager != null) {
            mPager.setCurrentItem(page, smoothScrolling);
        } else {
            mSavedPosition = page;
        }
    }
	
	/**
	 * 
	 * @param position
	 */
	protected void onPageSelected(int position) {
		mSavedPosition = position;
	}
	
	@Override
	public void reset() {
		invalidateAdapter();
		
		mSavedPosition = 0;
		super.reset();
	}
	
	@Override
	public void onDestroy() {
		invalidateAdapter();
		
		super.onDestroy();
	}
	
	/**
	 * 
	 * @return
	 */
	protected boolean isPagerDataValid() {
		return mData != null 
				&& mData.length > 0 
				&& (mData[0] instanceof AbsListData)
				&& ((AbsListData) mData[0]).isValid();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("ds:viewpagerfragment:saved-position", mSavedPosition);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mSavedPosition = savedInstanceState.getInt("ds:viewpagerfragment:saved-position");
	}
	
	abstract protected int getPagerID();
	
	abstract protected AbsTemplatePagerAdapter getNewAdapter();
	
	abstract protected void setAdapterData(AbsAsyncData data, int loadId);
	
	abstract protected void invalidateAdapter();
}
