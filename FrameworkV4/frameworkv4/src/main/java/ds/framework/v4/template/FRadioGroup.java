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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import ds.framework.v4.datatypes.Datatype;

public class FRadioGroup<T> extends AbsFDataSet<RadioGroup, Integer, T, T> {

	private android.widget.RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener;
	
	/**
	 * values (in this case) are the positions of the ids in buttonIds and T must be Integer
	 * 
	 * @param field
	 * @param buttonIds
	 */
	public FRadioGroup(Datatype<T> field) {
		super(field, null);
	}
	
	public FRadioGroup(Datatype<T> field, T[] buttonValues) {
		super(field, null, buttonValues);
	}
	
	public void setOnCheckedChangeListener(final OnCheckedChangeListener<T> listener) {
		mOnCheckedChangeListener = new android.widget.RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId != -1) {
					listener.onCheckedChanged(findValue(checkedId));
				}
			}
			
		};
		if (mFieldView != null) {
			mFieldView.setOnCheckedChangeListener(mOnCheckedChangeListener);
		}
	}
	
	public void setFieldView(View view) {
		super.setFieldView(view);
		
		int ms = mFieldView.getChildCount();
		
		for(int i = 0; i < ms; ++ i) {
			final View child = mFieldView.getChildAt(i);
			if (!(child instanceof RadioButton)) {
				continue;
			}
			mIdentifiers.add(child.getId());
		}
		
		if (mOnCheckedChangeListener != null) {
			mFieldView.setOnCheckedChangeListener(mOnCheckedChangeListener);
		}
	}

	@Override
	public void save() {
		mValue.set(findValue(mFieldView.getCheckedRadioButtonId()));
	}

	@Override
	public void show() {
		mFieldView.clearCheck();
		mFieldView.check(findId(mValue.get()));
	}

	private int findId(T value) {
		assert(value != null);
		
		if (mValidValues.isEmpty()) {
			return mIdentifiers.get((Integer) value);
		}
		for(int i = 0; i < mValidValues.size(); ++i) {
			if (mValidValues.get(i).equals(value)) {
				return mIdentifiers.get(i);
			}
		}
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	private T findValue(int id) {
		for(int i = 0; i < mIdentifiers.size(); ++i) {
			if (mIdentifiers.get(i) == id) {
				if (mValidValues.isEmpty()) {
					return (T) (new Integer(i));
				}
				return mValidValues.get(i);
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		try {
			return findValue(mFieldView.getCheckedRadioButtonId()).toString();
		} catch(Exception e) {
			return super.toString();	// fall back
		}
	}
	
	public interface OnCheckedChangeListener<T> {
		public void onCheckedChanged(T selectedValue);
	}
}
