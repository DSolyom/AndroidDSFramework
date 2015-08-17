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

import ds.framework.v4.common.Debug;

abstract public class RecyclerViewHeaderedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final static int ITEM_VIEW_TYPE_HEADER = 250;
    public final static int ITEM_VIEW_TYPE_FOOTER = 251;

    public final static int ITEM_VIEW_TYPE_DEFAULT = 252;

    protected View mHeaderView;
    protected View mFooterView;

    /**
     *
     * @param headerView
     */
    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyDataSetChanged();
    }

    /**
     *
     * @param footerView
     */
    public void setFooterView(View footerView) {
        mFooterView = footerView;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_HEADER) {
            return new RecyclerView.ViewHolder(mHeaderView) {};
        } else if (viewType == ITEM_VIEW_TYPE_FOOTER) {
            return new RecyclerView.ViewHolder(mFooterView) {};
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder.itemView == mHeaderView || holder.itemView == mFooterView) {
            return;
        }
        onBindViewHolderInner(holder, position - (mHeaderView != null ? 1: 0));
    }

    abstract public void onBindViewHolderInner(final RecyclerView.ViewHolder holder, final int position);

    @Override
    public int getItemCount() {
        return getCount() + (mHeaderView != null ? 1 : 0) + (mFooterView != null ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && mHeaderView != null) {
            return ITEM_VIEW_TYPE_HEADER;
        }
        if (mFooterView != null && position == getItemCount() - 1) {
            return ITEM_VIEW_TYPE_FOOTER;
        }
        return getItemViewTypeInner(position - (mHeaderView != null ? 1: 0));
    }

    /**
     *
     * @param position
     * @return
     */
    public int getItemViewTypeInner(int position) {
        return ITEM_VIEW_TYPE_DEFAULT;
    }

    public void reset() {
        notifyDataSetChanged();
    }

    abstract int getCount();
}