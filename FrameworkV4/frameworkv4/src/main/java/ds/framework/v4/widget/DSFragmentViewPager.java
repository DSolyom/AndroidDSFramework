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
import android.app.Fragment;
import android.support.v13.app.FragmentPagerAdapterModByDS;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import ds.framework.v4.app.DSFragment;

public class DSFragmentViewPager extends DSViewPager {

	DSAdapterInterface mAdapter;
	
	/**
	 * current page
	 */
	protected int mCurrentPage;
	
	/**
	 * last page - needed to be able to active fragments only when scroll animation is finished
	 */
	private int mLastPage;
	
	private Integer mNextPage;
	
	private boolean mPageChanged;
	
	private int mScrollState;

	private boolean mScrollToNextPage;
	
	OnPageChangeListener mOnPageChangeListener;
	
	public DSFragmentViewPager(Context context) {
		super(context);
		
		super.setOnPageChangeListener(new MOnPageChangeListener());
	}
	
	public DSFragmentViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		super.setOnPageChangeListener(new MOnPageChangeListener());
	}
	
	@Override
	public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
		mOnPageChangeListener = onPageChangeListener;
	}
	
	@Override
	public void setAdapter(PagerAdapter adapter) {
		assert(adapter instanceof PagerAdapter);
		
		mAdapter = (DSAdapterInterface) adapter;
		
		super.setAdapter((PagerAdapter) mAdapter);
	}
	
	/**
	 * 
	 * @param page
	 * @param scrolled
	 */
	@Override
	public void setCurrentItem(int page, boolean scrolled) {
		if (mAdapter != null && mAdapter.getCount() >= page) {
			super.setCurrentItem(page, scrolled);
			if (!scrolled) {
				
				// need to activate new fragment here
				activateFragment(mCurrentPage, true);
				mLastPage = mCurrentPage;
			}
		} else {
			mNextPage = page;
			mScrollToNextPage = scrolled;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public int getScrollState() {
		return mScrollState;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isPageChangeHandled() {
		return !mPageChanged;
	}
	
	/**
	 * 
	 * @param page
	 */
	private void setCurrentPage(int page) {
		if (page == mCurrentPage) {
			return;
		}
		mPageChanged = true;
		
		if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
			mLastPage = mCurrentPage;
			activateFragment(mLastPage, false);
		}
		mCurrentPage = page;
		
		if (mAdapter == null || mAdapter.getCount() <= page) {
			return;
		}

		if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
			activateFragment(mCurrentPage, true);
			mLastPage = mCurrentPage;
		}
	}
	
	public void onResume() {	
		if (mAdapter != null && mAdapter.getCount() > mCurrentPage) {
			final Fragment fragment = (Fragment) mAdapter.getItem(mCurrentPage);
			if (fragment instanceof DSFragment) {
				((DSFragment) fragment).setActive(true);
			}
		}
		if (mNextPage != null) {
			setCurrentItem(mNextPage, mScrollToNextPage);
			mNextPage = null;
		}
	}
	
	public void onPause() {
		if (mAdapter != null && mAdapter.getCount() > 0) {
			final Fragment fragment = (Fragment) mAdapter.getItem(mCurrentPage);
			if (fragment instanceof DSFragment) {
				((DSFragment) fragment).setActive(false);
			}
		}
	}
	
	/**
	 * 
	 * @param page
	 * @param active
	 */
	private void activateFragment(int page, boolean active) {
		if (page != -1) {
			final Fragment fragment = (Fragment) mAdapter.getItem(page);
			if (fragment instanceof DSFragment) {
				((DSFragment) fragment).setActive(active);
			}
		}
	}

	/**
	 * @class MOnPageChangeListener
	 */
	class MOnPageChangeListener implements OnPageChangeListener {
		
		@Override
		public void onPageScrollStateChanged(int state) {
			mScrollState = state;
			if (mScrollState == ViewPager.SCROLL_STATE_IDLE && mPageChanged) {
				
				if (mLastPage != mCurrentPage) {
				
					// activate current and deactivate last fragment
					activateFragment(mLastPage, false);
					activateFragment(mCurrentPage, true);
					mLastPage = mCurrentPage;
				}
			}
			
			if (mOnPageChangeListener != null) {
				mOnPageChangeListener.onPageScrollStateChanged(state);
			}
			
			mPageChanged = false;
		}
	
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			if (mOnPageChangeListener != null) {
				mOnPageChangeListener.onPageScrolled(arg0, arg1, arg2);
			}
		}

		@Override
		public void onPageSelected(int page) {
			setCurrentPage(page);
			
			if (mOnPageChangeListener != null) {
				mOnPageChangeListener.onPageSelected(page);
			}
		}
	}
}
