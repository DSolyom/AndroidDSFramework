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

import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import ds.framework.v4.app.DSActivity;

public abstract class TemplateRecyclerViewAdapter<T> extends AbsTemplateViewHolderAdapter<T> {

	final ArrayList<T> mItems = new ArrayList<T>();
	protected AdapterView<?> mAdapterView;

	public TemplateRecyclerViewAdapter(DSActivity in, int rowLayoutId) {
		super(in, rowLayoutId);
	}
	
	public void setAdapterView(AdapterView<?> view) {
		mAdapterView = view;
	}
	
	public AdapterView<?> getAdapterView() {
		return mAdapterView;
	}
	
	synchronized public void setItems(Collection<T> items) {
		mItems.clear();
		if (items != null && items.size() != 0) {
			mItems.addAll(items);
		}
		notifyDataSetChanged();
	}
	
	synchronized public void setItems(T[] items) {
		setItems(Arrays.asList(items));
	}
	
	synchronized public void addItems(Collection<T> items) {
        addItems(mItems.size(), items);
    }

    synchronized public void addItems(int index, Collection<T> items) {
		final int itemsS = items.size();
		mItems.addAll(index, items);
		notifyItemRangeInserted(index, itemsS);
	}
	
	synchronized public void addItems(T[] items) {
		addItems(Arrays.asList(items));
	}

    synchronized public void addItems(int index, T[] items) {
        addItems(index, Arrays.asList(items));
    }
	
	synchronized public void addItem(T item) {
		mItems.add(item);
		notifyItemInserted(mItems.size() - 1);
	}

	synchronized public void addItem(int index, T item) {
		mItems.add(index, item);
		notifyItemInserted(index);
	}
	
	synchronized public boolean removeItem(T item) {
        int indexOfItem = mItems.indexOf(item);
		if (indexOfItem != -1) {
            mItems.remove(item);
			notifyItemRemoved(indexOfItem);
			return true;
		}
		return false;
	}

    synchronized public int removeItems(int position, int itemCount) {
        if (mItems.size() < position + itemCount) {
            itemCount = mItems.size() - position;
        }
        for(int i = itemCount; i > 0; --i) {
            mItems.remove(position);
        }
        notifyItemRangeRemoved(position, itemCount);

        return itemCount;
    }

	synchronized public boolean replaceItem(T newItem, T oldItem) {
		final int at = mItems.indexOf(oldItem);
		if (at == -1) {
			return false;
		}
		mItems.remove(oldItem);
		mItems.add(at, newItem);
		
		return true;
	}
	
	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public T getItem(int position) {
		return mItems.get(position);
	}
	
	/**
	 * return all items in the data set
	 * 
	 * @return
	 */
	public ArrayList<T> getItems() {
		return mItems;
	}
	
	@Override
	public void invalidate() {
		mItems.clear();
        super.invalidate();
	}

}
