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

import android.widget.TextView;
import ds.framework.v4.datatypes.Datatype;

public class FEditText extends FormField<TextView, String> {

	public FEditText(Datatype<String> field) {
		super(field);
	}

	@Override
	public void save() {
		if (mFieldView != null) {
			mValue.set(((TextView) mFieldView).getText().toString());
		} else {
			mValue.reset();
		}
	}

	@Override
	public void show() {
		if (mFieldView != null) {
			((TextView) mFieldView).setText(mValue.toString());
		}
	}
}
