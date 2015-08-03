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
import ds.framework.v4.datatypes.Datatype;

@SuppressWarnings("serial")
abstract public class FormField<S extends View, T> extends Datatype<Datatype<T>> {
	
	public static final int NO_ERROR = -1;
	
	protected S mFieldView;
	
	public FormField(Datatype<T> fieldValue) {
		mType = FORMFIELD;
		mValue = fieldValue;
	}
	
	@SuppressWarnings("unchecked")
	public void setFieldView(View view) {
		mFieldView = (S) view;
	}
	
	public S getFieldView() {
		return mFieldView;
	}

	/**
	 * set field's value from view
	 */
	abstract public void save();
	
	/**
	 * show field's value in view
	 */
	abstract public void show();
}
