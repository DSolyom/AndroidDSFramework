/*
	Copyright 2016 Dániel Sólyom

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
package ds.framework.v4.app;

import android.os.Bundle;
import android.support.v4.view.ViewPagerModByDS;
import android.support.v4.view.ViewPagerModByDS.OnPageChangeListener;
import android.view.View;
import ds.framework.v4.data.AbsAsyncData;
import ds.framework.v4.template.Template;
import ds.framework.v4.widget.AbsTemplatePagerAdapter;

abstract public class AbsDSViewPagerFragment extends AbsDSAsyncDataFragment {

	protected AbsTemplatePagerAdapter mAdapter;
	
	private int mSavedPosition;
	
	protected ViewPagerModByDS mPager;
	
	public AbsDSViewPagerFragment() {
		super();
	}
	
	public AbsDSViewPagerFragment(boolean isDialog) {
		super(isDialog);
	}

	@Override
	protected void onViewCreated(View rootView) {
		mPager = null;
		
		super.onViewCreated(rootView);
		
		mPager = (ViewPagerModByDS) mTemplate.findViewById(getPagerID());
	}

	@Override
	protected AbsAsyncData[] getAsyncDataObjects() {
		return new AbsAsyncData[] {};
	}

    @Override
    public void createData() {
        ensureAdapter();
        super.createData();

        if (mAdapter != null) {
            final AbsAsyncData pagerData = mAdapter.getPagerData();
            final AbsAsyncData[] normalData = mData;
            mData = new AbsAsyncData[normalData.length + 1];

            int i = 0;
            for (; i < normalData.length; ++i) {
                mData[i] = normalData[i];
            }
            mData[i] = pagerData;
        }
    }

    @Override
    public void onDataLoaded(AbsAsyncData data, int loadId) {
        if (mAdapter != null && mAdapter.hasData(data)) {
            mAdapter.onDataLoaded(data, loadId);
        }

        super.onDataLoaded(data, loadId);
    }

    @Override
    public void onDestroy() {
        invalidateAdapter();

        super.onDestroy();
    }

    /**
     *
     */
    protected void ensureAdapter() {
        if (mAdapter == null) {
            mAdapter = createAdapter();
        }
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
            	AbsDSViewPagerFragment.this.onPageSelected(position);
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

    /**
     *
     * @return
     */
    public AbsTemplatePagerAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = createAdapter();
        }
        return mAdapter;
    }

    protected void invalidateAdapter() {
        if (mAdapter != null) {
            mAdapter.invalidate();
        }
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
	
	abstract protected AbsTemplatePagerAdapter createAdapter();
}
