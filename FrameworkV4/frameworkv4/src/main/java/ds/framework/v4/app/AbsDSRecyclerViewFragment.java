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

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import ds.framework.v4.R;
import ds.framework.v4.common.Debug;
import ds.framework.v4.data.AbsAsyncData;
import ds.framework.v4.template.Template;
import ds.framework.v4.widget.IRecyclerView;
import ds.framework.v4.widget.RecyclerViewHeaderedAdapter;

/**
 * @class AbsDSRecyclerViewFragment
 * 
 * A Fragment (might) having a recylcer view which data (might) be loadad asynchronously<br/>
 * !note: AbsRecyclerViewData's will 'fill' adapters in order of appearance in onDataLoaded<br/>
 * and should be exactly as many data objects as adapters when using RecyclerViewMultiAdapter and only one with any other adapter
 */
abstract public class AbsDSRecyclerViewFragment extends AbsDSAsyncDataFragment {

	protected RecyclerViewHeaderedAdapter mAdapter;

    private boolean mRecyclerViewDataAdded;

	private int mEmptyViewResID;

	protected ViewGroup mRecyclerAdapterView;

    // usually activity can hold header or footer views
    // exception is when there is more than one list in it
    // like when using a view pager
    protected boolean mActivityCanHoldHeader = true;
    protected boolean mActivityCanHoldFooter = true;

	public AbsDSRecyclerViewFragment() {
		super();
	}

	public AbsDSRecyclerViewFragment(boolean isDialog) {
		super(isDialog);
	}
	
	@Override
	protected void onViewCreated(View rootView) {
        mRecyclerAdapterView = null;

        super.onViewCreated(rootView);

        if (getDSActivity().findViewById(R.id.view_pager) != null) {
            mActivityCanHoldFooter = mActivityCanHoldHeader = false;
        }

        mRecyclerAdapterView = (ViewGroup) mTemplate.findViewById(getRecyclerViewID());
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
            final AbsAsyncData[] recyclerViewData = mAdapter.getRecyclerViewData();
            final AbsAsyncData[] normalData = mData;
            mData = new AbsAsyncData[normalData.length + recyclerViewData.length];

            int i = 0;
            for (; i < normalData.length; ++i) {
                mData[i] = normalData[i];
            }
            for (int j = 0; j < recyclerViewData.length; ++j) {
                mData[i + j] = recyclerViewData[j];
            }
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
		super.onDestroy();

        invalidateAdapter(mAdapter);
	}
	
	@Override
	public void display() {
		if (mEmptyViewResID == 0) {
			mEmptyViewResID = getEmptyViewID();
		}
		
		super.display();

		if (mRecyclerAdapterView != null) {
            if (mRecyclerAdapterView instanceof RecyclerView) {
                addListHeadersAndFooters();

                if (((RecyclerView) mRecyclerAdapterView).getLayoutManager() == null) {
                    ((RecyclerView) mRecyclerAdapterView).setLayoutManager(createLayoutManager());
                }
            }
			
			if (mEmptyViewResID != 0) {
				setEmptyVisibility();
			}

			mTemplate.fill(mRecyclerAdapterView, mAdapter, Template.ADAPTER, "");
		}
	}
	
	/**
	 * 
	 */
	protected void setEmptyVisibility() {
        final View emptyView = getEmptyView();
        if (emptyView != null) {
            mTemplate.fill(emptyView, shouldShowEmpty(), Template.VISIBLE, "");
        }
    }

    /**
     *
     * @return
     */
    protected View getEmptyView() {
		return mTemplate.findViewById(mEmptyViewResID);
	}

    /**
     *
     */
    protected void setLoadingVisibility() {
        final View loadingView = getLoadingView();
        if (loadingView != null) {
            mTemplate.fill(loadingView, shouldShowLoading(), Template.VISIBLE, "");
        }
    }

    /**
     *
     * @return
     */
    protected View getLoadingView() {
        return mTemplate.findViewById(mLoadingViewResID);
    }
	
	/**
	 * 
	 * @return
	 */
	public boolean isListEmpty() {
		if (shouldShowLoading() || (mAdapter != null && mAdapter.getCount() != 0)) {
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
	
	@Override
	protected boolean shouldShowLoading() {
		return (mRecyclerAdapterView != null && (mAdapter == null || mAdapter.getCount() == 0)) && super.shouldShowLoading();
	}
	
	@Override
	protected void reloadForSearch(boolean finalTouch) {
        reloadListData();
    }

    /**
     *
     */
    public void reloadListData() {
		if (mAdapter == null) {
			return;
		}

		try {
            mAdapter.invalidate();

            reloadDataAndDisplay();
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
		final View recyclerView = getRecyclerView();
		if (!(recyclerView instanceof IRecyclerView)) {
			return;
		}
        final IRecyclerView irecyclerView = (IRecyclerView) recyclerView;
        if (irecyclerView.getHeaderView() != null || irecyclerView.getFooterView() != null) {
            return;
        }
		
		View listHeader = createHeaderView();
		if (listHeader != null && (!(listHeader instanceof ViewGroup) || ((ViewGroup) listHeader).getChildCount() > 0)) {
            final ViewGroup headerParent = ((ViewGroup) listHeader.getParent());
            if (headerParent != null) {
                headerParent.removeView(listHeader);
            }
            (irecyclerView).setHeaderView(listHeader);

            mTemplate.addOtherRoot(listHeader);
		}

        View listFooter = createFooterView();
        if (listFooter != null && (!(listFooter instanceof ViewGroup) || ((ViewGroup) listFooter).getChildCount() > 0)) {
            final ViewGroup FooterParent = ((ViewGroup) listFooter.getParent());
            if (FooterParent != null) {
                FooterParent.removeView(listFooter);
            }
            (irecyclerView).setFooterView(listFooter);

            mTemplate.addOtherRoot(listFooter);
        }
	}

	/**
	 * return view or view group to be the main list's header
	 *
	 * @return
	 */
	protected View createHeaderView() {
		View headerView = mRootView.findViewById(R.id.container_list_header);

		if (headerView == null && mActivityCanHoldHeader) {

			// list header is not in the fragments scope in layout
			headerView = getDSActivity().findViewById(R.id.container_list_header);
		}

		if (headerView != null) {
			final View lv = headerView.findViewById(getRecyclerViewID());

			if (lv != null && (lv instanceof IRecyclerView)) {

				// list is inside its header - move it out
				// !note: list might go out of the root view's and fragment's scope
				ViewGroup.LayoutParams lp = lv.getLayoutParams();
				final int lpWidth = lp.width;
				final int lpHeight = lp.height;
				((ViewGroup) lv.getParent()).removeViewInLayout(lv);

				((ViewGroup) headerView.getParent()).addView(lv, lpWidth, lpHeight);
			}
		}
		return headerView;
	}

	/**
	 * return view or view group to be the main list's header
	 *
	 * @return
	 */
	protected View createFooterView() {
		View footerView = mRootView.findViewById(R.id.container_list_footer);

		if (footerView == null && mActivityCanHoldFooter) {

			// list footer is not in the fragments scope in layout
			footerView = getDSActivity().findViewById(R.id.container_list_footer);
		}

		if (footerView != null) {
			final View lv = footerView.findViewById(getRecyclerViewID());

			if (lv != null && (lv instanceof IRecyclerView)) {

				// list is inside its footer - move it out
				// !note: list might go out of the root view's and fragment's scope
				ViewGroup.LayoutParams lp = lv.getLayoutParams();
				final int lpWidth = lp.width;
				final int lpHeight = lp.height;
				((ViewGroup) lv.getParent()).removeViewInLayout(lv);

				((ViewGroup) footerView.getParent()).addView(lv, lpWidth, lpHeight);
			}
		}
		return footerView;
	}
	
	@Override
	public void reset() {
		invalidateAdapter(mAdapter);
		super.reset();
	}

    /**
     *
     * @return
     */
	public RecyclerViewHeaderedAdapter getAdapter() {
		if (mAdapter == null) {
			mAdapter = createAdapter();
		}
		return mAdapter;
	}
	
	public View getRecyclerView() {
		if (mRecyclerAdapterView == null) {
			mRecyclerAdapterView = (RecyclerView) mTemplate.findViewById(getRecyclerViewID());
		}
		return mRecyclerAdapterView;
	}
	
    /**
     * create and return the list's adapter
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
    /*
	abstract protected void setAdapterData(RecyclerViewHeaderedAdapter adapter, AbsRecyclerViewData data, int loadId);
	*/

	/**
	 * invalidate the adapter's data
	 */
	protected void invalidateAdapter(RecyclerViewHeaderedAdapter adapter) {
        if (adapter != null) {
            adapter.invalidate();
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

}
