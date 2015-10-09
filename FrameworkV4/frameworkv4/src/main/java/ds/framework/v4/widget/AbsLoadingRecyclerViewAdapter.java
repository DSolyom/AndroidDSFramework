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
package ds.framework.v4.widget;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import ds.framework.v4.app.ActivityInterface;
import ds.framework.v4.common.Debug;
import ds.framework.v4.data.AbsAsyncData;

abstract public class AbsLoadingRecyclerViewAdapter<T> extends AbsTemplateViewHolderAdapter<T> {

    public final static int LOADING_VIEW_TYPE_DEFAULT = 240;

    protected int mDataOffset;
    protected int mLoadThreshold;

    private int mCount;

    protected RecyclerViewHeaderedAdapter mAdapter;
    private int mAdapterDataCount;

    private int mCountBeforeLoad;
    private int mPositionBeforeLoad = -1;

    private final LoadingAdapterObserver mObserver = new LoadingAdapterObserver();

    public AbsLoadingRecyclerViewAdapter(ActivityInterface in, int loadingRowLayoutId) {
        super(in, loadingRowLayoutId);
    }

    @Override
    public void onBindViewHolderInner(RecyclerView.ViewHolder holder, int position) {
        if (position < mDataOffset) {

            // start loading items at the 'top'
            loadSomeIfNeeded(position);
            super.onBindViewHolderInner(holder, position);
        } else if (position >= mDataOffset + mAdapterDataCount) {

            // start loading items at the 'bottom'
            loadSomeIfNeeded(position);
            super.onBindViewHolderInner(holder, position);
        } else {
            loadSomeIfNeeded(position);

            mAdapter.onBindViewHolderInner(holder, position - mDataOffset);
        }
    }

    /**
     *
     * @param position
     */
    protected void loadSomeIfNeeded(int position) {
        if (position == -1) {
            return;
        }
        mPositionBeforeLoad = position;
        if (position < mDataOffset + mLoadThreshold && mDataOffset > 0) {
            mCountBeforeLoad = getCount();
            ((LoadingRecyclerViewAdapterData) mRecyclerViewData).loadSome(false, position + mDataOffset, this);
        } else if (position >= mDataOffset + mAdapterDataCount - mLoadThreshold) {
            mCountBeforeLoad = getCount();
            ((LoadingRecyclerViewAdapterData) mRecyclerViewData).loadSome(true, position + mDataOffset, this);
        } else {
            mPositionBeforeLoad = -1;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType != LOADING_VIEW_TYPE_DEFAULT) {
            return mAdapter.onCreateViewHolder(parent, viewType);
        } else {
            return super.onCreateViewHolder(parent, ITEM_VIEW_TYPE_DEFAULT);
        }
    }

    @Override
    public Object getItem(int position) {
        if (position < mDataOffset || position >= mDataOffset + mAdapterDataCount) {
            return null;
        }

        return mAdapter.getItem(position - mDataOffset);
    }

    @Override
    public int getCount() {
        return ((LoadingRecyclerViewAdapterData) mRecyclerViewData).getAllCount();
    }

    /**
     *
     * @param dataOffset
     */
    protected void setDataOffset(int dataOffset) {
        if (mDataOffset == dataOffset) {
            return;
        }
        mDataOffset = dataOffset;
        notifyItemRangeChanged(0, mDataOffset);
    }

    /**
     *
     * @param adapter
     */
    public void setAdapter(RecyclerViewHeaderedAdapter adapter) {
        if (mAdapter == adapter) {
            return;
        }
        if (mAdapter != null) {
            mAdapter.unregisterAdapterDataObserver(mObserver);
        }

        mAdapter = adapter;
        mAdapterDataCount = mAdapter.getItemCount();
        mAdapter.registerAdapterDataObserver(mObserver);

        // TODO: multi adapter
        setRecyclerViewData(mAdapter.getRecyclerViewData()[0]);
    }

    /**
     *
     * @param position
     * @return
     */
    public int getItemViewTypeInner(int position) {
        if (position < mDataOffset || position >= mDataOffset + mAdapterDataCount) {
            return LOADING_VIEW_TYPE_DEFAULT;
        }
        return mAdapter.getItemViewType(position - mDataOffset);
    }


    @Override
    public void onDataLoaded(AbsAsyncData absAsyncData, int loadId) {
        mAdapter.onDataLoaded(absAsyncData, loadId);

        setDataOffset(((LoadingRecyclerViewAdapterData) absAsyncData).getDataOffset());
    }

    /**
     * @Class LoadingAdapterObserver
     */
    private class LoadingAdapterObserver extends RecyclerView.AdapterDataObserver {

        @Override
        public void onChanged() {
            mAdapterDataCount = mAdapter.getItemCount();
            notifyDataSetChanged();
            loadSomeIfNeeded(mPositionBeforeLoad);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mAdapterDataCount = mAdapter.getItemCount();
            notifyItemRangeChanged(positionStart, itemCount);
            loadSomeIfNeeded(mPositionBeforeLoad);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mAdapterDataCount = mAdapter.getItemCount();

            // need to check if mAdapter's insert is an insert or just a change for us
            if (mCountBeforeLoad < getCount()) {
                notifyItemRangeInserted(positionStart, itemCount);
            } else {
                notifyItemRangeChanged(positionStart, itemCount);
            }
            loadSomeIfNeeded(mPositionBeforeLoad);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mAdapterDataCount = mAdapter.getItemCount();
            notifyItemRangeChanged(positionStart, itemCount);
            loadSomeIfNeeded(mPositionBeforeLoad);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mAdapterDataCount = mAdapter.getItemCount();
            notifyItemMoved(fromPosition, toPosition);
            loadSomeIfNeeded(mPositionBeforeLoad);
        }
    }

    /**
     * @Interface LoadingRecyclerViewAdapterData
     */
    public interface LoadingRecyclerViewAdapterData {
        void loadSome(boolean wayForward, int positionAskingForLoad, AbsAsyncData.OnDataLoadListener onDataLoadListener);
        int getDataOffset();
        int getAllCount();
    }
}
