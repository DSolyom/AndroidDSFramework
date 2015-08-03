/*
	Copyright 2011 Dániel Sólyom

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import android.widget.AdapterView;
import android.widget.ListAdapter;
import ds.framework.v4.app.ActivityInterface;

public abstract class TemplateListAdapter<T> extends AbsTemplateAdapter<T> 
		implements ListAdapter {
	
	final ArrayList<T> mItems = new ArrayList<T>();
	protected AdapterView<?> mAdapterView;
	
	public TemplateListAdapter(ActivityInterface in, int rowLayoutId) {
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
		} else {
			notifyDataSetInvalidated();
		}
		notifyDataSetChanged();
	}
	
	synchronized public void setItems(T[] items) {
		setItems(Arrays.asList(items));
	}
	
	synchronized public void addItems(Collection<T> items) {
		mItems.addAll(items);
		notifyDataSetChanged();
	}
	
	synchronized public void addItems(T[] items) {
		addItems(Arrays.asList(items));
	}
	
	synchronized public void addItem(T item) {
		mItems.add(item);
		notifyDataSetChanged();
	}
	
	synchronized public boolean removeItem(T item) {
		if (mItems.remove(item)) {
			notifyDataSetChanged();
			return true;
		}
		return false;
	}
	
	synchronized public T removeLastItem() {
		try {
			return mItems.remove(mItems.size() - 1);
		} catch(Exception e) {
			return null;
		}
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
	public void reset() {
		mItems.clear();
		super.reset();
	}
}
