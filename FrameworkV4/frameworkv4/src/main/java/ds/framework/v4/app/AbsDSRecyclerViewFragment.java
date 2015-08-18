/*
	Copyright 2015 Dániel Sólyom

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

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ds.framework.v4.R;
import ds.framework.v4.common.Debug;
import ds.framework.v4.data.AbsAsyncData;
import ds.framework.v4.template.Template;
import ds.framework.v4.widget.RecyclerViewHeaderedAdapter;
import ds.framework.v4.widget.IRecyclerView;
import ds.framework.v4.widget.RecyclerViewMultiAdapter;

/**
 * @class AbsDSRecyclerViewFragment
 * 
 * A Fragment (might) having a recylcer view which data (might) be loadad asynchronously<br/>
 * !note: AbsRecyclerViewData's will 'fill' adapters in order of appearance in onDataLoaded<br/>
 * and should only be as many as adapters when using RecyclerViewMultiAdapter and only one with any other adapter
 */
abstract public class AbsDSRecyclerViewFragment extends AbsDSAsyncDataFragment {

	protected RecyclerViewHeaderedAdapter mAdapter;

	private int mEmptyViewResID;

	protected RecyclerView mRecyclerView;

	public AbsDSRecyclerViewFragment() {
		super();
	}

	public AbsDSRecyclerViewFragment(boolean isDialog) {
		super(isDialog);
	}
	
	@Override
	protected void onViewCreated(View rootView) {
        mRecyclerView = null;
		
		super.onViewCreated(rootView);

        mRecyclerView = (IRecyclerView) mTemplate.findViewById(getRecyclerViewID());
	}
	
	@Override
	public void loadData() {
		ensureAdapter();
		super.loadData();
	}
	
	@Override
	public void onDataLoaded(AbsAsyncData data, int loadId) {		
		if (data instanceof AbsRecyclerViewData) {
            if (mAdapter instanceof RecyclerViewMultiAdapter) {
                RecyclerViewHeaderedAdapter subAdapter;

                ArrayList<RecyclerViewHeaderedAdapter> subAdapters = ((RecyclerViewMultiAdapter) mAdapter).getAdapters();
                for(int i = subAdapters.size() - 1; i >= 0; --i) {
                    if (mData[i] == data) {
                        setAdapterData(subAdapters.get(i), (AbsRecyclerViewData) data, loadId);
                    }
                }
            } else if (mData.length > 0 && data == mData[0]){
                setAdapterData(mAdapter, (AbsRecyclerViewData) data, loadId);
            }
		}
		
		super.onDataLoaded(data, loadId);
	}
		
	@Override
	public void onDestroy() {
		super.onDestroy();

		invalidateAdapter(mAdapter);
	}
	
	@Override
	public void display() {
		if (mEmptyViewResID == 0) {
			mEmptyViewResID = getEmptyViewID();
		}
		
		super.display();

		if (mRecyclerView != null) {
            addListHeadersAndFooters();

            if (mRecyclerView.getLayoutManager() == null) {
                mRecyclerView.setLayoutManager(createLayoutManager());
            }
            mTemplate.fill(mRecyclerView, mAdapter, Template.ADAPTER, "");
			
			if (mEmptyViewResID != 0) {
				setEmptyVisibility();
			}
		}
	}
	
	/**
	 * 
	 */
	protected void setEmptyVisibility() {
		if (mRecyclerView instanceof IRecyclerView && ((IRecyclerView) mRecyclerView).getHeaderView() != null) {
			final View emptyView = ((IRecyclerView) mRecyclerView).getHeaderView().findViewById(mEmptyViewResID);
			if (emptyView != null) {
                mTemplate.fill(emptyView, shouldShowEmpty(), Template.VISIBLE, "");
			} else {
				mTemplate.fill(mEmptyViewResID, shouldShowEmpty(), Template.VISIBLE);
			}
		} else {
			mTemplate.fill(mEmptyViewResID, shouldShowEmpty(), Template.VISIBLE);
		}
	}
	
	@Override
	protected void setLoadingVisibility() {
        if (mRecyclerView instanceof IRecyclerView && ((IRecyclerView) mRecyclerView).getHeaderView() != null) {
            final View loadingView = ((IRecyclerView) mRecyclerView).getHeaderView().findViewById(mEmptyViewResID);
			if (loadingView != null) {
                mTemplate.fill(loadingView, shouldShowLoading(), Template.VISIBLE, "");
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
		if (shouldShowLoading() || (mAdapter != null && mAdapter.getItemCount() != 0)) {
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
				&& (mData[0] instanceof AbsRecyclerViewData)
				&& ((AbsRecyclerViewData) mData[0]).isValid();
	}
	
	@Override
	protected boolean shouldShowLoading() {
		return (mRecyclerView != null && (mAdapter == null || mAdapter.getItemCount() == 0)) && super.shouldShowLoading();
	}
	
	@Override
	protected void reloadForSearch(boolean finalTouch) {
		if (mData == null 
				|| mData.length == 0 
				|| !(mData[0] instanceof AbsRecyclerViewData)) {
			return;
		}
		try {
			invalidateData(0);
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
			mAdapter = createAdapter();
		}
	}
	
	/**
	 * 
	 */
	protected void addListHeadersAndFooters() {
		final View RecyclerView = getRecyclerView();
		if (!(RecyclerView instanceof IRecyclerView)) {
			return;
		}
		
		View listHeader = createHeaderView();
		if (listHeader != null && (!(listHeader instanceof ViewGroup) || ((ViewGroup) listHeader).getChildCount() > 0)) {
			if (((IRecyclerView) RecyclerView).getHeaderView() == null) {
				final ViewGroup headerParent = ((ViewGroup) listHeader.getParent());
				if (headerParent != null) {
					headerParent.removeView(listHeader);
				}
				((IRecyclerView) RecyclerView).setHeaderView(listHeader);
			}
		}

        View listFooter = createFooterView();
        if (listFooter != null && (!(listFooter instanceof ViewGroup) || ((ViewGroup) listFooter).getChildCount() > 0)) {
            if (((IRecyclerView) RecyclerView).getFooterView() == null) {
                final ViewGroup FooterParent = ((ViewGroup) listFooter.getParent());
                if (FooterParent != null) {
                    FooterParent.removeView(listFooter);
                }
                ((IRecyclerView) RecyclerView).setFooterView(listFooter);
            }
        }
	}
	
	/**
	 * return view or view group to be the main list's header
	 * 
	 * @return
	 */
	protected View createHeaderView() {
		return mTemplate.findViewById(R.id.container_list_header);
	}
	
	/**
	 * return view or view group to be the main list's footer
	 * 
	 * @return
	 */
	protected View createFooterView() {
		return mTemplate.findViewById(R.id.container_list_footer);
	}
	
	@Override
	public void reset() {
		invalidateAdapter(mAdapter);
		super.reset();
	}
	
	public RecyclerViewHeaderedAdapter getAdapter() {
		if (mAdapter == null) {
			mAdapter = createAdapter();
		}
		return mAdapter;
	}
	
	public View getRecyclerView() {
		if (mRecyclerView == null) {
			mRecyclerView = (RecyclerView) mTemplate.findViewById(getRecyclerViewID());
		}
		return mRecyclerView;
	}
	
    /**
     * the list's adapter
     * 
     * @return
     */
    abstract protected RecyclerViewHeaderedAdapter createAdapter();
	
	/**
	 * set adapter data
	 * 
	 * @param data
	 * @param loadId
	 */
	abstract protected void setAdapterData(RecyclerViewHeaderedAdapter adapter, AbsRecyclerViewData data, int loadId);
	
	/**
	 * invalidate the adapter's data
	 */
	protected void invalidateAdapter(RecyclerViewHeaderedAdapter adapter) {
        if (adapter != null) {
            adapter.reset();
        }
    }

    /**
     * Override to use other type of layout manager instead of LinearLayoutManager
     */
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(getContext());
    }
	
	/**
	 * get the id of the list view we fill
	 * 
	 * @return
	 */
	abstract protected int getRecyclerViewID();
	
	/**
	 * get the view res id which shows when the list is empty
	 * 
	 * @return
	 */
	protected int getEmptyViewID() {
		return 0;
	}
	
	/**
	 * @class AbsListData
	 */
	abstract public static class AbsRecyclerViewData extends AbsAsyncData {

		public AbsRecyclerViewData() {
		}
		
		public AbsRecyclerViewData(String loaderTag) {
			super(loaderTag);
		}
		
	}
}
