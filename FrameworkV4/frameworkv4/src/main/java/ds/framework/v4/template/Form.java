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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import ds.framework.v4.common.Debug;

public class Form extends ArrayList<Form.FormItem> {

	private static final long serialVersionUID = -4460404892652144839L;

	public static final int EDITTEXT = 1;
	
	private HashMap<Integer, Serializable> mSerializedValues;

	public Form() {
	}
	
	public void registerItem(FormItem field) {
		FormItem found = null;
		for(FormItem oldField: this) {
			if (oldField.value == field.value) {
				found = oldField;
				break;
			}
		}
		if (found != null) {
			remove(found);
		}
		add(field);
	}
	
	/**
	 * set field values to the views
	 */
	public void show() {
		for(FormItem item : this) {
			if (mSerializedValues != null) {
				final int key = item.value.getFieldView().getId();
				if (mSerializedValues.containsKey(key)) {
					item.value.get().set(mSerializedValues.get(key));
				}
			}
			item.show();
		}
		mSerializedValues = null;
	}
	
	/**
	 * save values from views
	 */
	public void save() {
		for(FormItem item : this) {
			item.value.save();
		}
	}
	
	/**
	 * reset field values
	 */
	public void reset() {
		for(FormItem item : this) {
			item.value.reset();
		}
	}
	
	/**
	 * remove all data from the form
	 */
	public void remove() {
		clear();
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public FormItem geFormItemById(String id) {
		for(FormItem item : this) {
			if (item.id != null && item.id.equals(id)) {
				return item;
			}
		}
		return null;
	}

	public Serializable getSerializedValues() {
		save();
		final HashMap<Integer, Serializable> values = new HashMap<Integer, Serializable>();
		for(FormItem field : this) {
			values.put(field.value.getFieldView().getId(), (Serializable) field.value.get().get());
		}
		return values;
	}

	@SuppressWarnings("unchecked")
	public void setSerializedValues(Serializable values) {
		if (values == null) {
			mSerializedValues = null;
			return;
		}
		try {
			mSerializedValues = (HashMap<Integer, Serializable>) values;
		} catch(Throwable e) {
			Debug.logException(e);
		}
	}
	
	static public class FormItem {
		String id;
		FormField<?, ?> value;
		
		public FormItem(String id, FormField<?, ?> value) {
			this.id = id;
			this.value = value;
		}

		public void show() {
			((FormField<?, ?>) value).show();
		}
	}
}
