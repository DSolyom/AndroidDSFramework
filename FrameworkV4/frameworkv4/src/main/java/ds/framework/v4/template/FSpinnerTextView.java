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
package ds.framework.v4.template;

import java.util.Arrays;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ds.framework.v4.Global;
import ds.framework.v4.app.DSActivity;
import ds.framework.v4.app.DSFragment;
import ds.framework.v4.common.Debug;
import ds.framework.v4.datatypes.Datatype;

public class FSpinnerTextView<T> extends AbsFDataSet<TextView, String, T, T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3078176479888661763L;

	private OnItemSelectListener<T> mOnItemSelectListener;
	
	private boolean mParentActAsThis = false;
	
	String mHint;
	
	public FSpinnerTextView(Datatype<T> field, String[] identifiers) {
		super(field, identifiers);
		
		if (field.get() == null && identifiers.length > 0) {
			field.set(0);
		}
	}
	
	public FSpinnerTextView(Datatype<T> field, String[] identifiers, T[] validValues) {
		super(field, identifiers, validValues);
		
		if (field.get() == null && identifiers.length > 0) {
			field.set(validValues[0]);
		}
	}
	
	/**
	 * 
	 * @param newValue
	 * @param identifiers
	 * @param validValues
	 */
	public void setNew(T newValue, String[] identifiers, T[] validValues) {
		mIdentifiers.clear();
		mIdentifiers.addAll(Arrays.asList(identifiers));
		
		mValidValues.clear();
		mValidValues.addAll(Arrays.asList(validValues));
		
		mValue.set(newValue);
		
		if (mFieldView != null) {
			show();
		}
	}
	
	/**
	 * 
	 * @param listener
	 */
	public void setOnItemSelectedListener(OnItemSelectListener<T> listener) {
		mOnItemSelectListener = listener;
	}
	
	/**
	 * 
	 * @param as
	 */
	public void parentActAsThis(boolean as) {
		mParentActAsThis = as;
	}

	@Override
	public void save() {
		if (mFieldView != null) {
			// already done
		} else {
			mValue.reset();
		}
	}
	
	/**
	 * 
	 * @param value
	 */
	public void selectValue(T value) {
		if ((mValidValues.isEmpty() && mIdentifiers.size() <= (Integer) value) || !mValidValues.contains(value)) {
			throw(new IllegalArgumentException());
		}
		mValue.set(value);
		
		show();
	}

	@Override
	public void show() {
		if (mValidValues.isEmpty()) {
			
			// T must be Integer
			final Integer position = (Integer) mValue.get();
			if (position == null) {
				mFieldView.setText(mIdentifiers.get(0));
			} else {
				mFieldView.setText(mIdentifiers.get(position));
			}
		} else {
			mFieldView.setText(findOptionTitle(mValue.get()));
		}
	}

	@Override
	public void setFieldView(View view) {
		super.setFieldView(view);
		
		if (mParentActAsThis) {
			view = (View) view.getParent();
		}
		view.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mIdentifiers.size() <= 1) {
					return;
				}
				
				final SpinnerDialog dialog = new SpinnerDialog();
				dialog.init(FSpinnerTextView.this);
				try {
					dialog.show(((DSActivity) Global.getCurrentActivity()).getFragmentManager(), "spinner-dialog");
				} catch(Throwable e) {
					Debug.logException(e);
				}
			}
		});
	}
	
	private String findOptionTitle(T value) {
		if (value == null) {
			if (mHint == null) {
				return mIdentifiers.get(0);
			} else {
				return mHint;
			}
		}

		int sV = mValidValues.size();
		for(int i = 0; i < sV; ++i) { 
			if (value.equals(mValidValues.get(i))) {
				return mIdentifiers.get(i);
			}
		}
		if (mHint == null) {
			return mIdentifiers.get(0);
		} else {
			return mHint;
		}
	}
	
	public interface OnItemSelectListener<T> {
		public void onItemSelected(View view, Datatype<T> value);
	}

	public void setHint(String hint) {
		mHint = hint;
	}
	
	public Dialog getSpinnerDialog(Context context) {
		final String[] titles = new String[mIdentifiers.size()];
		mIdentifiers.toArray(titles);
		
		// find selected
		T selectedValue = mValue.get();
		Integer selected = -1;
		
		if (selectedValue != null && !mValidValues.isEmpty()) {
			int at = 0;
			for(T value : mValidValues) {
				if (selectedValue.equals(value)) {
					selected = at;
					break;
				}
				++at;
			}
		}
		
		return new AlertDialog.Builder(context).setSingleChoiceItems(titles, selected,
				new Dialog.OnClickListener() {
			
				@SuppressWarnings("unchecked")
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (mValidValues.isEmpty()) {
						
						// T must be Integer
						((Datatype<Integer>) mValue).set(which);
					} else {
						mValue.set(mValidValues.get(which));
					}
					show();
					if (mOnItemSelectListener != null) {
						mOnItemSelectListener.onItemSelected(mFieldView, mValue);
					}
					dialog.dismiss();
				}
	
			}
		).create();
	}
	
	/**
	 * @class FilterDialog
	 */
	public static class SpinnerDialog extends DSFragment {

		private FSpinnerTextView<?> mIn;
		
		/**
		 * 
		 * @param in
		 */
		public void init(FSpinnerTextView<?> in) {
			mIn = in;
		}
		
		@Override
		protected View getRootView(LayoutInflater inflater, ViewGroup container) {
			return null;
		}
		
		@Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mIn.getSpinnerDialog(getContext());
		}
		
		@Override
		public void onDestroyView() {
			dismiss();
			
			super.onDestroyView();
		}
	}
}
