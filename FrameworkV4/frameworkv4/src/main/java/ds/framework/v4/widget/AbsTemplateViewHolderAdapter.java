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

import java.util.HashMap;

import ds.framework.v4.app.ActivityInterface;
import ds.framework.v4.data.AbsAsyncData;
import ds.framework.v4.template.Template;

abstract public class AbsTemplateViewHolderAdapter<T> extends RecyclerViewHeaderedAdapter {
    
	protected ActivityInterface mIn;

    /**
     * layout resourcer ids for view types
     * !note: use type < 240 to be able to use {@link RecyclerViewMultiAdapter} and {@link AbsLoadingRecyclerViewAdapter}
     * use {@link #setViewTypeLayoutResID(int viewType, int resID)} to set
     */
	private HashMap<Integer, Integer> mRowLayoutResIDs = new HashMap<>();

	protected Template mTemplate;

    protected AbsAsyncData mRecyclerViewData;

    public AbsTemplateViewHolderAdapter(ActivityInterface in, int rowLayoutId) {
		mIn = in;
		mRowLayoutResIDs.put(VIEWTYPE_DEFAULT, rowLayoutId);
		mTemplate = new Template(mIn, null);

        // just use an empty data as default
        mRecyclerViewData = new AbsAsyncData() {

            @Override
            protected LoaderThread createLoader() {
                return null;
            }
        };
	}
	
	public ActivityInterface getIn() {
		return mIn;
	}

    /**
     *
     * @param viewType
     * @param resID
     */
    public void setViewTypeLayoutResID(int viewType, int resID) {
        mRowLayoutResIDs.put(viewType, resID);
    }
	
	@Override
	public long getItemId(int position) {
		return position;
	}

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);

        if (holder != null) {
            return holder;
        }
        return new TemplateHolder(inflateTemplateView(mRowLayoutResIDs.get(viewType), parent, viewType));
    }

    @Override
    public void onBindViewHolderInner(final RecyclerView.ViewHolder holder, final int position) {
        mTemplate.setRoot(holder.itemView);
        fillRow((T) getItem(position), position, holder.getItemViewType());
    }
	
	/**
	 * override to do something with the just inflated view
	 * 
	 * @return
	 */
	protected View inflateTemplateView(int rowRes, ViewGroup viewParent, int viewType) {
		final View view = mIn.inflate(rowRes, viewParent, false);
		return view;
	}

    @Override
    public AbsAsyncData[] getRecyclerViewData() {
        return new AbsAsyncData[] { mRecyclerViewData };
    }

    /**
     *
     * @param data
     */
    public void setRecyclerViewData(AbsAsyncData data) {
        if (mRecyclerViewData == data) {
            return;
        }

        mRecyclerViewData = data;

        super.invalidate();
    }

    @Override
    public void invalidate() {
        mRecyclerViewData.invalidate();

        super.invalidate();
    }

    @Override
    public boolean hasData(AbsAsyncData data) {
        return mRecyclerViewData == data;
    }

	/**
     * @param data
     * @param viewType
     */
	abstract protected void fillRow(T data, int position, int viewType);


    /**
     * @class TemplateHolder
     */
    private class TemplateHolder extends RecyclerView.ViewHolder {

        public TemplateHolder(View itemView) {
            super(itemView);
        }
    }
}
