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

package ds.framework.v4.template;

import android.view.View;
import android.widget.Spinner;
import ds.framework.v4.datatypes.Datatype;

public class FSpinner<T> extends AbsFAdapterView<Spinner, T> {

	private String mTitle;
	
	public FSpinner(Datatype<T> fieldValue, String[] labels) {
		super(fieldValue, labels);
	}

	public FSpinner(Datatype<T> fieldValue, String[] labels, int dropDownViewResource) {
		super(fieldValue, labels, dropDownViewResource );
	}
	
	public FSpinner(Datatype<T> fieldValue, String[] labels, T[] values) {
		super(fieldValue, labels, values);
	}
	
	public FSpinner(Datatype<T> fieldValue, String[] labels, T[] values, int dropDownViewResource) {
		super(fieldValue, labels, values, dropDownViewResource);
	}
	
	public void setTitle(String title) {
		mTitle = title;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void save() {
		int selected = mFieldView.getSelectedItemPosition();
		if (selected == -1) {
			mValue.reset();
			return;
		}
		
		if (mValidValues.isEmpty()) {
			
			// K must be Integer
			mValue.set((T) new Integer(selected));
		} else {
			mValue.set(mValidValues.get(selected));
		}
	}

	@Override
	public void show() {
		Integer position;
		if (mValidValues.isEmpty()) {
			
			// K must be Integer
			position = (Integer) mValue.get();
			if (position == null) {
				position = Spinner.INVALID_POSITION;
			}
		} else {
			position = findOptionValuePosition(mValue.get());
		}
		mFieldView.setSelection(position);
		
		if (mTitle != null) {
			mFieldView.setPrompt(mTitle);
		}
	}
	
	@Override
	public void setFieldView(View view) {
		super.setFieldView(view);
		
		mFieldView.setAdapter(mAdapter);
	}
	
	private Integer findOptionValuePosition(T value) {
		Integer position = Spinner.INVALID_POSITION;
		
		if (value == null) {
			return position;
		}

		int sV = mValidValues.size();
		for(int i = 0; i < sV; ++i) { 
			if (value.equals(mValidValues.get(i))) {
				return i;
			}
		}
		return position;
	}
}
