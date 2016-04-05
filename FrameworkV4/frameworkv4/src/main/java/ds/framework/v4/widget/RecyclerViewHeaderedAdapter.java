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
import android.view.View;
import android.view.ViewGroup;

import ds.framework.v4.data.AbsAsyncData;

abstract public class RecyclerViewHeaderedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements AbsAsyncData.OnDataLoadListener {

    public final static int VIEWTYPE_HEADER = 250;
    public final static int VIEWTYPE_FOOTER = 251;

    public final static int VIEWTYPE_DEFAULT = 252;

    protected View mHeaderView;
    protected View mFooterView;

    /**
     * @param headerView
     */
    public void setHeaderView(View headerView) {
        if (mHeaderView == headerView) {
            return;
        }
        mHeaderView = headerView;
        notifyDataSetChanged();
    }

    /**
     * @param footerView
     */
    public void setFooterView(View footerView) {
        if (mFooterView == footerView) {
            return;
        }
        mFooterView = footerView;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEWTYPE_HEADER) {
            return new RecyclerView.ViewHolder(mHeaderView) {
            };
        } else if (viewType == VIEWTYPE_FOOTER) {
            return new RecyclerView.ViewHolder(mFooterView) {
            };
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder.itemView == mHeaderView || holder.itemView == mFooterView) {
            return;
        }
        onBindViewHolderInner(holder, position - (mHeaderView != null ? 1 : 0));
    }

    abstract public void onBindViewHolderInner(final RecyclerView.ViewHolder holder, final int position);

    abstract public Object getItem(int position);

    @Override
    public int getItemCount() {
        return getCount() + (mHeaderView != null ? 1 : 0) + (mFooterView != null ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && mHeaderView != null) {
            return VIEWTYPE_HEADER;
        }
        if (mFooterView != null && position == getItemCount() - 1) {
            return VIEWTYPE_FOOTER;
        }
        return getItemViewTypeInner(position - (mHeaderView != null ? 1 : 0));
    }

    /**
     * @param position
     * @return
     */
    public int getItemViewTypeInner(int position) {
        return VIEWTYPE_DEFAULT;
    }

    /**
     *
     */
    public void invalidate() {
        notifyDataSetChanged();
    }

    /**
     * @return
     */
    public boolean isEmpty() {
        return getCount() == 0;
    }

    /**
     * @return
     */
    abstract public int getCount();

    /**
     * @return
     */
    abstract public AbsAsyncData[] getRecyclerViewData();

// implements AbsAsyncData.OnDataLoadListener

    public void onDataLoadStart(AbsAsyncData data, int loadId) {

    }

    public void onDataLoadFailed(AbsAsyncData data, int loadId) {

    }

    public void onDataLoadInterrupted(AbsAsyncData data, int loadId) {

    }

    abstract public boolean hasData(AbsAsyncData data);
}
