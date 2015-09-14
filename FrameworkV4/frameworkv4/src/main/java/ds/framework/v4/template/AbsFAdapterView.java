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

import ds.framework.v4.datatypes.Datatype;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;

abstract public class AbsFAdapterView<S extends AdapterView<? extends Adapter>, T> extends AbsFDataSet<S, String, T, T> {
	
	ArrayAdapter<CharSequence> mAdapter;
	protected OnItemSelectedListener mOnItemSelectedListener;
	int mDropDownViewResource;
	
	public AbsFAdapterView(Datatype<T> fieldValue, String[] labels) {
		this(fieldValue, labels, android.R.layout.simple_spinner_dropdown_item);
	}
	
	public AbsFAdapterView(Datatype<T> fieldValue, String[] labels, int dropDownViewResource) {
		super(fieldValue, labels);
		
		mDropDownViewResource = dropDownViewResource;
	}
	
	public AbsFAdapterView(Datatype<T> fieldValue, String[] labels, T[] values) {
		this(fieldValue, labels, values, android.R.layout.simple_spinner_dropdown_item);
	}
	
	public AbsFAdapterView(Datatype<T> fieldValue, String[] labels, T[] values, int dropDownViewResource) {
		super(fieldValue, labels, values);

		mDropDownViewResource = dropDownViewResource;
	}
	
	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		mOnItemSelectedListener = listener;
		if (mFieldView != null) {
			mFieldView.setOnItemSelectedListener(mOnItemSelectedListener);
		}
	}
	
	@Override
	public void setFieldView(View view) {
		super.setFieldView(view);
		
		final CharSequence[] identifiers = new CharSequence[mIdentifiers.size()];
		int i = 0;
		for(CharSequence cs : mIdentifiers) {
			identifiers[i++] = cs;
		}
		mAdapter = new ArrayAdapter<CharSequence>(
				mFieldView.getContext(), 
				android.R.layout.simple_spinner_item,
				identifiers
		);

		mAdapter.setDropDownViewResource(mDropDownViewResource);
		
		if (mOnItemSelectedListener != null) {
			mFieldView.setOnItemSelectedListener(mOnItemSelectedListener);
		}
	}
	
	public ArrayAdapter<CharSequence> getAdapter() {
		return mAdapter;
	}
}
