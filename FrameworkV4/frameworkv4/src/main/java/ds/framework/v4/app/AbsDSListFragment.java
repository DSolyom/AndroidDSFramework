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
package ds.framework.v4.app;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import ds.framework.v4.R;
import ds.framework.v4.common.Debug;
import ds.framework.v4.data.AbsAsyncData;
import ds.framework.v4.template.Template;
import ds.framework.v4.widget.DSAdapterInterface;
import ds.framework.v4.widget.HorizontalListView;

/**
 * @class AbsDSListFragment
 * 
 * A Fragment (might) having a list which data (might) be loadad asynchronously<br/>
 * !note: there's should be only one AbsListData among the data objects 
 */
abstract public class AbsDSListFragment extends AbsDSAsyncDataFragment {

	protected DSAdapterInterface mAdapter;

	private int mEmptyViewResID;
	
	private int mSavedListFirstVisible;
	private int mSavedListPosition;

	protected View mListView;

	private View mListHeader;
	private View mListFooter;
	
	public AbsDSListFragment() {
		super();
	}
	
	public AbsDSListFragment(boolean isDialog) {
		super(isDialog);
	}
	
	@Override
	protected void onViewCreated(View rootView) {
		mListView = null;
		
		super.onViewCreated(rootView);
		
		mListView = mTemplate.findViewById(getListViewID());
	}
	
	@Override
	public void loadData() {
		ensureAdapter();
		super.loadData();
	}
	
	@Override
	public void onDataLoaded(AbsAsyncData data, int loadId) {		
		if (data instanceof AbsListData) {

			// save scroll position before setting/changing cursor
			saveListScrollPosition();
			
			setAdapterData((AbsListData) data, loadId);
		}
		
		super.onDataLoaded(data, loadId);
	}
		
	@Override
	public void onDestroy() {
		super.onDestroy();

		invalidateAdapter();
	}
	
	@Override
	public void display() {
		if (mEmptyViewResID == 0) {
			mEmptyViewResID = getListEmptyViewID();
		}
		
		super.display();

		if (mListView != null) {
			addListHeadersAndFooters();

			mTemplate.fill(mListView, mAdapter, Template.ADAPTER, "");
			
			if (mEmptyViewResID != 0) {
				setEmptyVisibility();
			}
		}
		
		restoreListScrollPosition();
	}
	
	/**
	 * 
	 */
	protected void setEmptyVisibility() {
		if (mListHeader != null) {
			final View emptyView = mListHeader.findViewById(mEmptyViewResID);
			if (emptyView != null) {
				emptyView.setVisibility(shouldShowEmpty() ? View.VISIBLE : View.GONE);
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
			} else {
				mTemplate.fill(mEmptyViewResID, shouldShowEmpty(), Template.VISIBLE);
			}
		} else {
			mTemplate.fill(mEmptyViewResID, shouldShowEmpty(), Template.VISIBLE);
		}
	}
	
	@Override
	protected void setLoadingVisibility() {
		if (mListHeader != null) {
			final View loaderView = mListHeader.findViewById(getLoadingViewID());
			if (loaderView != null) {
				loaderView.setVisibility(shouldShowLoading() ? View.VISIBLE : View.GONE);
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
			} else {
				super.setLoadingVisibility();
			}
		} else {
			super.setLoadingVisibility();
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isListEmpty() {
		if (shouldShowLoading() || (mAdapter != null && !mAdapter.isEmpty())) {
			return false;
		}
		if (mData != null)
		for(AbsAsyncData data : mData) {
			if (shouldShowLoadErrorFor(data, data.getLoadId())) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @return
	 */
	protected boolean shouldShowEmpty() {
		return isListEmpty();
	}
	
	/**
	 * 
	 * @return
	 */
	protected boolean isListDataValid() {
		return mData != null 
				&& mData.length > 0 
				&& (mData[0] instanceof AbsListData)
				&& ((AbsListData) mData[0]).isValid();
	}
	
	@Override
	protected boolean shouldShowLoading() {
		return (mListView != null && (mAdapter == null || mAdapter.isEmpty())) && super.shouldShowLoading();
	}
	
	@Override
	protected void reloadForSearch(boolean finalTouch) {
		if (mData == null 
				|| mData.length == 0 
				|| !(mData[0] instanceof AbsListData)) {
			return;
		}
		try {
			invalidateData(0, false);
			loadDataAndDisplay();
		} catch(Throwable e) {
			Debug.logException(e);
		}
	}
	
	/**
	 * 
	 */
	protected void ensureAdapter() {
		if (mAdapter == null) {
			mAdapter = getNewAdapter();
		}
	}
	
	/**
	 * 
	 */
	protected void addListHeadersAndFooters() {
		final View listView = getListView();
		if (!(listView instanceof ListView)) {
			return;
		}
		
		mListHeader = getHeaderView();
		if (mListHeader != null && (!(mListHeader instanceof ViewGroup) || ((ViewGroup) mListHeader).getChildCount() > 0)) {
			if (((ListView) listView).getHeaderViewsCount() == 0) {
				final ViewGroup headerParent = ((ViewGroup) mListHeader.getParent());
				if (headerParent != null) {
					headerParent.removeView(mListHeader);
				}
				mListHeader.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				((ListView) listView).addHeaderView(mListHeader);
			}
		}
		
		mListFooter = getFooterView();
		if (mListFooter != null && (!(mListFooter instanceof ViewGroup) || ((ViewGroup) mListFooter).getChildCount() > 0)) {
			if (((ListView) listView).getFooterViewsCount() == 0) {
				final ViewGroup headerParent = ((ViewGroup) mListFooter.getParent());
				if (headerParent != null) {
					headerParent.removeView(mListFooter);
				}
				mListFooter.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				((ListView) listView).addFooterView(mListFooter);
			}
		}
	}
	
	/**
	 * return view or view group to be the main list's header
	 * 
	 * @return
	 */
	protected View getHeaderView() {
		return mRootView.findViewById(R.id.container_list_header);
	}
	
	/**
	 * return view or view group to be the main list's footer
	 * 
	 * @return
	 */
	protected View getFooterView() {
		return mRootView.findViewById(R.id.container_list_footer);
	}
	
	@Override
	public void invalidateData() {
		saveListScrollPosition();
		
		super.invalidateData();
	}
	
	@Override
	public void invalidateData(int which) {
		invalidateData(which, true);
	}
	
	public void invalidateData(int which, boolean saveListScrollPosition) {
		if (which == 0 && mData[which] instanceof AbsListData) {
			
			// this is the list's data
			if (saveListScrollPosition) {
				saveListScrollPosition();
			} else {
				mSavedListFirstVisible = 0;
				mSavedListPosition = 0;
			}
		}

		super.invalidateData(which);
	}
	
	@Override
	public void reset() {
		invalidateAdapter();
		super.reset();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		saveListScrollPosition();
	}
	
	/**
	 * save list's scroll position<br/>
	 * works with AdapterViews only
	 */
	protected void saveListScrollPosition() {
		if (mAdapter == null) {
			return;
		}
		try {
			final View listView = getListView();
			if (listView instanceof AdapterView<?>) {
				final View v = ((AdapterView<?>) listView).getChildAt(0);
				
				if (v != null) {
					mSavedListFirstVisible = ((AdapterView<?>) listView).getFirstVisiblePosition();
				
					if (listView instanceof HorizontalListView) {
						mSavedListPosition = v.getLeft();
					} else {
						mSavedListPosition = v.getTop();
					}
				}
			} else {
				mSavedListFirstVisible = 0;
				mSavedListPosition = 0;
			}
		} catch(NullPointerException e) {
			Debug.logException(e);
		}
	}
	
	/**
	 * restore list's scroll position to a previously saved place<br/>
	 * only works with ListView at the moment
	 */
	protected void restoreListScrollPosition() {
		final View listView = getListView();
		if (listView instanceof ListView) {
			((ListView) listView).setSelectionFromTop(mSavedListFirstVisible, mSavedListPosition);
		} else if (listView instanceof HorizontalListView) {
			((HorizontalListView) listView).setSelectionFromLeft(mSavedListFirstVisible, mSavedListPosition);
		}
	}
	
	public DSAdapterInterface getAdapter() {
		if (mAdapter == null) {
			mAdapter = getNewAdapter();
		}
		return mAdapter;
	}
	
	public View getListView() {
		if (mListView == null) {
			mListView = mTemplate.findViewById(getListViewID());
		}
		return mListView;
	}
	
    /**
     * the list's adapter
     * 
     * @return
     */
    abstract protected DSAdapterInterface getNewAdapter();
	
	/**
	 * set adapter data
	 * 
	 * @param data
	 * @param loadId
	 */
	abstract protected void setAdapterData(AbsListData data, int loadId);
	
	/**
	 * invalidate the adapter's data
	 */
	abstract protected void invalidateAdapter();
	
	/**
	 * get the id of the list view we fill
	 * 
	 * @return
	 */
	abstract protected int getListViewID();
	
	/**
	 * get the view res id which shows when the list is empty
	 * 
	 * @return
	 */
	protected int getListEmptyViewID() {
		return 0;
	}
	
	/**
	 * @class AbsListData
	 */
	abstract public static class AbsListData extends AbsAsyncData {

		@Deprecated
		public AbsListData() {
		}
		
		public AbsListData(String loaderTag) {
			super(loaderTag);
		}
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("ds:listview:saved-list-first-visible", mSavedListFirstVisible);
		outState.putInt("ds:listview:saved-list-position", mSavedListPosition);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mSavedListFirstVisible = savedInstanceState.getInt("ds:listview:saved-list-first-visible");
		mSavedListPosition = savedInstanceState.getInt("ds:listview:saved-list-position");
	}
}
