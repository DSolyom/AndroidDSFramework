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

import java.util.ArrayList;

import ds.framework.v4.data.AbsAsyncData;
import ds.framework.v4.data.AbsRecyclerViewData;

/**
 * Created by solyom on 13/08/15.
 */
public class RecyclerViewMultiAdapter extends RecyclerViewHeaderedAdapter {

    public final static int VIEW_TYPE_SHIFT = 8;
    public final static int VIEW_TYPE_REAL_MASK = 255;

    private ArrayList<Integer> mAdapterStartPositions = new ArrayList<>();
    private ArrayList<RecyclerViewHeaderedAdapter> mAdapters = new ArrayList<>();
    private AbsRecyclerViewData[] mAdapterData;

    private int mItemCount;

    private final MultiAdapterObserver mObserver = new MultiAdapterObserver();

    /**
     * add an adapter
     *
     * @param adapter
     * @param position
     */
    public void addAdapter(RecyclerViewHeaderedAdapter adapter, int position) {
        mAdapters.add(position, adapter);
        countItems();

        adapter.registerAdapterDataObserver(mObserver);

        notifyDataSetChanged();
    }

    /**
     *
     * @param adapter
     */
    public void addAdapter(RecyclerViewHeaderedAdapter adapter) {
        addAdapter(adapter, mAdapters.size());
    }

    /**
     *
     * @param position
     */
    public void removeAdapter(int position) {
        RecyclerViewHeaderedAdapter removed = mAdapters.remove(position);
        countItems();

        removed.unregisterAdapterDataObserver(mObserver);

        notifyDataSetChanged();
    }

    /**
     *
     * @param removed
     */
    public void removeAdapter(RecyclerViewHeaderedAdapter removed) {
        mAdapters.remove(removed);
        countItems();

        removed.unregisterAdapterDataObserver(mObserver);

        notifyDataSetChanged();
    }

    /**
     *
     * @return
     */
    public ArrayList<RecyclerViewHeaderedAdapter> getAdapters() {
        return mAdapters;
    }

    /**
     *
     * @return
     */
    public RecyclerViewHeaderedAdapter getAdapter(int position) {
        try {
            return mAdapters.get(position);
        } catch(IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);
        if (holder != null) {
            return holder;
        }
        return mAdapters.get(viewType >> VIEW_TYPE_SHIFT).onCreateViewHolder(parent, viewType & VIEW_TYPE_REAL_MASK);
    }

    @Override
    public void onBindViewHolderInner(RecyclerView.ViewHolder holder, int position) {
        final AdapterInfo adapterInfo = getAdapterInfoFor(position);
        mAdapters.get(adapterInfo.index).onBindViewHolder(holder, position - adapterInfo.startPosition);
    }

    @Override
    public int getItemViewTypeInner(int position) {

        // create 'unique' type by adapter and adapters position type
        final AdapterInfo adapterInfo = getAdapterInfoFor(position);

        return mAdapters.get(adapterInfo.index).getItemViewType(position - adapterInfo.startPosition) + (adapterInfo.index << VIEW_TYPE_SHIFT);
    }

    @Override
    public int getCount() {
        return mItemCount;
    }

    @Override
    public AbsRecyclerViewData[] getRecyclerViewData() {
        if (mAdapterData == null) {
            mAdapterData = new AbsRecyclerViewData[mAdapters.size()];

            for(int i = 0; i < mAdapterData.length; ++i) {
                mAdapterData[i] = mAdapters.get(i).getRecyclerViewData()[0];
            }
        }
        return mAdapterData;
    }

    @Override
    public void onDataLoaded(AbsAsyncData data, int loadId) {
        ArrayList<RecyclerViewHeaderedAdapter> subAdapters = getAdapters();
        for(int i = subAdapters.size() - 1; i >= 0; --i) {
            RecyclerViewHeaderedAdapter subAdapter = subAdapters.get(i);
            if (mAdapterData[i] == data) {  // TODO: multi adapter in a multi adapter
                subAdapter.onDataLoaded(data, loadId);
            }
        }
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    private void countItems() {
        mItemCount = 0;
        mAdapterStartPositions.clear();
        for(RecyclerViewHeaderedAdapter adapter : mAdapters) {
            mAdapterStartPositions.add(mItemCount);
            mItemCount += adapter.getItemCount();
        }
    }

    protected AdapterInfo getAdapterInfoFor(int position) {
        final AdapterInfo ai = new AdapterInfo();
        final int aS = mAdapters.size();
        for(int i = 1; i < aS; ++i) {
            if (position < mAdapterStartPositions.get(i)) {
                --i;
                ai.index = i;
                ai.startPosition = mAdapterStartPositions.get(i);
                return ai;
            }
        }
        ai.index = aS - 1;
        ai.startPosition = mAdapterStartPositions.get(aS - 1);
        return ai;
    }

    /**
     * @class AdapterInfo
     */
    public class AdapterInfo {
        public int index;
        public int startPosition;
    }

    /**
     *
     */
    public void reset() {
        for(RecyclerViewHeaderedAdapter adapter : mAdapters) {
            adapter.reset();
        }

        mAdapterData = null;

        super.reset();
    }

    /**
     * @Class MultiAdapterObserver
     */
    private class MultiAdapterObserver extends RecyclerView.AdapterDataObserver {

        @Override
        public void onChanged() {
            countItems();
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            countItems();

            notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            countItems();

            notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            countItems();

            notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            countItems();

            notifyItemMoved(fromPosition, toPosition);
        }
    }
}
