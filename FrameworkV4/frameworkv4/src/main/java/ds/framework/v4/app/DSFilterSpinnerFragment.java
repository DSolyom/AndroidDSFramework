/*
	Copyright 2013 Dániel Sólyom

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

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import ds.framework.v4.data.DbEntryFilter;
import ds.framework.v4.data.DbEntryFilter.FilterItem;
import ds.framework.v4.data.DbEntryFilter.OnFilterItemSelectedListener;
import ds.framework.v4.template.Template;

abstract public class DSFilterSpinnerFragment extends DSFragment implements OnFilterItemSelectedListener {

	private DbEntryFilter mFilter;
	private FilterItem mSelected;
	private OnFilterItemSelectedListener mListener;

	/**
	 * container of the spinner<br/> 
	 * - it is hidden if no items in the spinner<br/> 
	 * - can be the spinner itself
	 */
	private int mContainerRes;
	
	private int mSpinnerRes;
	
	public DSFilterSpinnerFragment(int containerRes, int spinnerRes, 
			OnFilterItemSelectedListener listener) {
		mContainerRes = containerRes;
		mSpinnerRes = spinnerRes;
		mListener = listener;
	}
	
	@Override
	public void loadData() {
		if (mFilter == null) {
			mFilter = getFilter(getContext());
		}

		super.loadData();
	}
	
	@Override
	public void display() {	
		super.display();
		
		mTemplate.fill(mContainerRes, mFilter.getCount() != 0, Template.VISIBLE);
		mTemplate.fill(mSpinnerRes, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mFilter.show(DSFilterSpinnerFragment.this);
			}
			
		});
		final FilterItem[] extra = mFilter.getExtraItems();
		String selectedTitle;
		if (mSelected == null) {
			if (extra != null && extra.length > 0) {
				selectedTitle = mFilter.getExtraItems()[0].title;
			} else {
				final FilterItem firstItem = mFilter.getFilterItem(0);
				if (firstItem != null) {
					onFilterItemSelected(firstItem);
					selectedTitle = mSelected.title;
				} else {
					selectedTitle = "";
				}
			}
		} else {
			selectedTitle = mSelected.title;
		}
		mTemplate.fill(mSpinnerRes, selectedTitle);
	}
	
	@Override
	public void onFilterItemSelected(FilterItem selected) {
		if (selected != null && mSelected.id == selected.id) {
			mSelected = selected;
			return;
		}
		mSelected = selected;
		mListener.onFilterItemSelected(selected);
	}
	
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(mFragmentId + "-selected-filter-item", mSelected);
	}
	
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		onFilterItemSelected((FilterItem) savedInstanceState.getSerializable(mFragmentId + "-selected-filter-item"));
	}
	
	abstract protected DbEntryFilter getFilter(Context context);
}
