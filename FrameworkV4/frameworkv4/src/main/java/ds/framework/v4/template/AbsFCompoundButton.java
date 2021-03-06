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

import android.widget.CompoundButton;
import ds.framework.v4.datatypes.Datatype;

abstract public class AbsFCompoundButton<S extends CompoundButton, T> extends FormField<S, T> {

	T mValueTrue;
	T mValueFalse;
	
	public AbsFCompoundButton(Datatype<T> field, T valueTrue, T valueFalse) {
		super(field);
		mValueTrue = valueTrue;
		mValueFalse = valueFalse;
	}

	@Override
	public void save() {
		mValue.set(((CompoundButton) mFieldView).isChecked() ? mValueTrue : mValueFalse);
	}

	@Override
	public void show() {
		((CompoundButton) mFieldView).setChecked(mValue.equals(mValueTrue));
	}
}
