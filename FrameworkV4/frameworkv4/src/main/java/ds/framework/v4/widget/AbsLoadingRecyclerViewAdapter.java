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

abstract public class AbsLoadingRecyclerViewAdapter<T> extends AbsTemplateViewHolderAdapter<T> {

    public final static int LOADING_VIEW_TYPE_DEFAULT = 240;

    protected int mDataOffset;
    protected int mLoadThreshold;

    private int mCount;

    protected RecyclerViewHeaderedAdapter mAdapter;
    private int mAdapterDataCount;

    private final LoadingAdapterObserver mObserver = new LoadingAdapterObserver();

    public AbsLoadingRecyclerViewAdapter(ActivityInterface in, int loadingRowLayoutId) {
        super(in, loadingRowLayoutId);
    }

    @Override
    public void onBindViewHolderInner(RecyclerView.ViewHolder holder, int position) {
        if (position < mDataOffset) {

            // start loading items at the 'top'
            ((LoadingRecyclerViewAdapterData) mRecyclerViewData).loadSome(false);
            super.onBindViewHolderInner(holder, position);
        } else if (position >= mDataOffset + mAdapterDataCount) {

            // start loading items at the 'bottom'
            ((LoadingRecyclerViewAdapterData) mRecyclerViewData).loadSome(true);
            super.onBindViewHolderInner(holder, position);
        } else {

            // check if reached load threshold
            if (position < mDataOffset + mLoadThreshold) {

                // start loading items at the 'top'
                ((LoadingRecyclerViewAdapterData) mRecyclerViewData).loadSome(false);
            }
            if (position >= mDataOffset + mAdapterDataCount - mLoadThreshold) {

                // start loading items at the 'bottom'
                ((LoadingRecyclerViewAdapterData) mRecyclerViewData).loadSome(true);
            }

            mAdapter.onBindViewHolderInner(holder, position - mDataOffset);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType != LOADING_VIEW_TYPE_DEFAULT) {
            return mAdapter.onCreateViewHolder(parent, viewType);
        } else {
            return super.onCreateViewHolder(parent, viewType);
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
        return mCount;
    }

    /**
     *
     * @param count
     */
    public void setCount(int count) {
        mCount = count;
    }

    /**
     *
     * @param dataOffset
     */
    public void setDataOffset(int dataOffset) {
        mDataOffset = dataOffset;
        notifyDataSetChanged();
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

    /**
     * @Class LoadingAdapterObserver
     */
    private class LoadingAdapterObserver extends RecyclerView.AdapterDataObserver {

        @Override
        public void onChanged() {
            mAdapterDataCount = mAdapter.getItemCount();
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mAdapterDataCount = mAdapter.getItemCount();
            notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mAdapterDataCount = mAdapter.getItemCount();
            notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mAdapterDataCount = mAdapter.getItemCount();
            notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mAdapterDataCount = mAdapter.getItemCount();
            notifyItemMoved(fromPosition, toPosition);
        }
    }

    /**
     * @Interface LoadingRecyclerViewAdapterData
     */
    public interface LoadingRecyclerViewAdapterData {
        void loadSome(boolean wayForward);
    }
}
