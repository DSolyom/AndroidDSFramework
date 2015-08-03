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

import android.os.Bundle;
import android.view.View;
import ds.framework.v4.datatypes.Datatype;
import ds.framework.v4.template.FCheckBox;
import ds.framework.v4.template.FEditText;
import ds.framework.v4.template.Form;
import ds.framework.v4.template.FormField;

// TODO: put this all in Form
abstract public class DSFormFragment extends DSFragment {

	private final Form mForm = new Form();
	
	public DSFormFragment() {
		
	}
	
	public DSFormFragment(boolean isDialog) {
		super(isDialog);
	}
	
	/**
	 * create and register text field for data wrapper and view
	 * 
	 * @param value
	 * @param viewID
	 * @return
	 */
	public FEditText registerEditText(Datatype<String> value, int viewID) {
		FEditText fEditText = new FEditText(value);
		registerFormItem(fEditText, mRootView.findViewById(viewID));
		return fEditText;
	}
	
	/**
	 * create and register text field for data wrapper and view
	 * 
	 * @param value
	 * @param view
	 * @return
	 */
	public FEditText registerEditText(Datatype<String> value, View view) {
		FEditText fEditText = new FEditText(value);
		registerFormItem(fEditText, view);
		return fEditText;
	}
	
	/**
	 * create and register text field for data wrapper and view</br>
	 * registers with true and false boolean values
	 * 
	 * @param value
	 * @param viewID
	 * @return
	 */
	public FCheckBox<Boolean> registerCheckBox(Datatype<Boolean> value, View view) {
		FCheckBox<Boolean> fCheckbox = new FCheckBox<Boolean>(value, true, false);
		registerFormItem(fCheckbox, view);
		return fCheckbox;
	}
	
	/**
	 * create and register checkbox for data wrapper and view</br>
	 * registers with true and false boolean values
	 * 
	 * @param value
	 * @param viewID
	 */
	public void registerCheckBox(Datatype<Boolean> value, int viewID) {
		FCheckBox<Boolean> fCheckbox = new FCheckBox<Boolean>(value, true, false);
		registerFormItem(fCheckbox, mRootView.findViewById(viewID));
	}
	
	/**
	 * register a form field
	 * 
	 * @param field
	 * @param view
	 */
	public void registerFormItem(FormField<?, ?> field, int viewID) {
		registerFormItem(field, mRootView.findViewById(viewID));
	}
	
	/**
	 * register a form field
	 * 
	 * @param field
	 * @param view
	 */
	public void registerFormItem(FormField<?, ?> field, View view) {
		registerFormItem(field, view, null);
	}
	
	/**
	 * register a form field
	 * 
	 * @param field
	 * @param view
	 * @param id
	 */
	public void registerFormItem(FormField<?, ?> field, View view, String id) {
		field.setFieldView(view);
		final Form.FormItem item = new Form.FormItem(id, field);
		mForm.registerItem(item);
		item.show();
	}
	
	/**
	 * save form values
	 */
	public void saveForm() {
		mForm.save();
	}
	
	public Form getForm() {
		return mForm;
	}
	
	@Override
	public void display() {
		super.display();
		mForm.show();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable(mFragmentId + "-form-values", mForm.getSerializedValues());
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		mForm.setSerializedValues(savedInstanceState.getSerializable(mFragmentId + "-form-values"));
	}

	/* TODO
case FIELD_RADIOGROUP:
	
	FRadioGroup<Integer> fRadioGroup = new FRadioGroup<Integer>((Datatype<Integer>) value);
	fRadioGroup.setFieldView(view);
	mOwner.registerFormItem(new Form.FormItem(id, fRadioGroup));
	break;

	
case FIELD_CHECKBOX:
	
	FCheckBox<Boolean> fCheckBox = new FCheckBox<Boolean>((Datatype<Boolean>) value, true, false);
	fCheckBox.setFieldView(view);
	mOwner.registerFormItem(new Form.FormItem(id, fCheckBox));
	break;
	*/
}
