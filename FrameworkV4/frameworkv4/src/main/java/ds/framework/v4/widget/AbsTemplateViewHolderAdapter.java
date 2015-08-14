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
import ds.framework.v4.template.Template;

abstract public class AbsTemplateViewHolderAdapter<T> extends RecyclerViewHeaderedAdapter {
    
	protected ActivityInterface mIn;

    // TODO: to be able to add multiple layout resource id for different view types
	private HashMap<Integer, Integer> mRowLayoutRess = new HashMap<Integer, Integer>();
	protected Template mTemplate;

	public AbsTemplateViewHolderAdapter(ActivityInterface in, int rowLayoutId) {
		mIn = in;
		mRowLayoutRess.put(ITEM_VIEW_TYPE_DEFAULT, rowLayoutId);
		mTemplate = new Template(mIn, null);
	}
	
	public ActivityInterface getIn() {
		return mIn;
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
        return new TemplateHolder(inflateTemplateView(mRowLayoutRess.get(viewType), parent));
    }

    @Override
    public void onBindViewHolderInner(final RecyclerView.ViewHolder holder, final int position) {
        mTemplate.setRoot(holder.itemView);
        fillRow(getItem(position), position);
    }
	
	/**
	 * override to do something with the just inflated view
	 * 
	 * @return
	 */
	protected View inflateTemplateView(int rowRes, ViewGroup viewParent) {
		final View view = mIn.inflate(rowRes, viewParent, false);
		return view;
	}
	
	/**
	 * @param data
	 */
	abstract protected void fillRow(T data, int position);

    /**
     *
     * @param position
     * @return
     */
    abstract T getItem(int position);

    /**
     * @class TemplateHolder
     */
    private class TemplateHolder extends RecyclerView.ViewHolder {

        public TemplateHolder(View itemView) {
            super(itemView);
        }
    }
}
