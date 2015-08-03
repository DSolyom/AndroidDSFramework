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

import java.util.ArrayList;

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
import ds.framework.v4.R;
import ds.framework.v4.app.DSActivity;
import ds.framework.v4.app.DSFragment;
import ds.framework.v4.common.Debug;
import ds.framework.v4.datatypes.Datatype;

public class FMultiSpinnerTextView<T> extends AbsFDataSet<TextView, String, ArrayList<T>, T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3078176479888661763L;

	private OnMultiItemSelectedListener<T> mOnMultiItemSelectedListener;
	
	private boolean mParentActAsThis = false;
	
	String mTitle;
	
	public FMultiSpinnerTextView(Datatype<ArrayList<T>> field, String[] identifiers) {
		super(field, identifiers);
		
		if (field.get() == null && identifiers.length > 0) {
			field.set(0);
		}
	}
	
	public FMultiSpinnerTextView(Datatype<ArrayList<T>> field, String[] identifiers, T[] validValues) {
		super(field, identifiers, validValues);
		
		if (field.get() == null && identifiers.length > 0) {
			field.reset();
		}
	}
	
	public void setOnMultiItemSelectedListener(OnMultiItemSelectedListener<T> listener) {
		mOnMultiItemSelectedListener = listener;
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

	@Override
	public void show() {
		String result = "";
		if (mValidValues.isEmpty()) {
			
			// T must be Integer
			for(int i = 0; i < mIdentifiers.size(); ++i) {
				if (mValue.get().contains(i)) {
					if (!result.isEmpty()) {
						result += ", ";
					}
					result += mIdentifiers.get(i);
				}
			}
		} else {
			for(int i = 0; i < mIdentifiers.size(); ++i) {
				if (mValue.get().contains(mValidValues.get(i))) {
					if (!result.isEmpty()) {
						result += ", ";
					}
					result += mIdentifiers.get(i);
				}
			}
		}
		
		mFieldView.setText(result);
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
				dialog.init(FMultiSpinnerTextView.this);
				try {
					dialog.show(((DSActivity) Global.getCurrentActivity()).getFragmentManager(), "spinner-dialog");
				} catch(Throwable e) {
					Debug.logException(e);
				}
			}
		});
	}
	
	public interface OnMultiItemSelectedListener<T> {
		public void onMultiItemSelected(View view, Datatype<ArrayList<T>> value);
	}

	public void setTitle(String title) {
		mTitle = title;
	}
	
	public Dialog getSpinnerDialog(Context context) {
		final String[] titles = new String[mIdentifiers.size()];
		mIdentifiers.toArray(titles);
		
		// find selected
		boolean[] selected = new boolean[mIdentifiers.size()];
		
		if (mValidValues.isEmpty()) {
			
			// T must be Integer
			for(int i = 0; i < mIdentifiers.size(); ++i) {
				selected[i] = mValue.get().contains(i);
			}
		} else {
			for(int i = 0; i < mIdentifiers.size(); ++i) {
				selected[i] = mValue.get().contains(mValidValues.get(i));
			}
		}
		
		return new AlertDialog.Builder(context).setMultiChoiceItems(titles, selected,
				new Dialog.OnMultiChoiceClickListener() {
			
					@SuppressWarnings("unchecked")
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						ArrayList<T> values = mValue.get();
						if (isChecked) {
							if (mValidValues.isEmpty()) {
								
								// T must be Integer
								if (!values.contains(which)) {
									values.add((T) Integer.valueOf(which));
								}
							} else {
								T value = mValidValues.get(which);
								if (!values.contains(value)) {
									values.add(value);
								}
							}
						} else {
								if (mValidValues.isEmpty()) {
								
								// T must be Integer
								if (values.contains(which)) {
									values.remove((T) Integer.valueOf(which));
								}
							} else {
								T value = mValidValues.get(which);
								if (values.contains(value)) {
									values.remove(value);
								}
							}
						}
						show();
						if (mOnMultiItemSelectedListener != null) {
							mOnMultiItemSelectedListener.onMultiItemSelected(mFieldView, mValue);
						}
					}
	
				}
		).setPositiveButton(R.string.x_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).setTitle(mTitle).create();
	}
	
	/**
	 * @class FilterDialog
	 */
	public static class SpinnerDialog extends DSFragment {

		private FMultiSpinnerTextView<?> mIn;
		
		/**
		 * 
		 * @param in
		 */
		public void init(FMultiSpinnerTextView<?> in) {
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
