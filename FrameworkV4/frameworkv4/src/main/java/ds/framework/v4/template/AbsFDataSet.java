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

import java.util.ArrayList;
import java.util.Arrays;

import android.view.View;
import ds.framework.v4.datatypes.Datatype;

abstract public class AbsFDataSet<S extends View, K, T, L> extends FormField<S, T> {

	final protected ArrayList<K> mIdentifiers = new ArrayList<K>();
	final protected ArrayList<L> mValidValues = new ArrayList<L>();

	public AbsFDataSet(Datatype<T> field, K[] identifiers) {
		super(field);
		if (identifiers != null) {
			mIdentifiers.addAll(Arrays.asList(identifiers));
		}
	}
	
	public AbsFDataSet(Datatype<T> field, K[] identifiers, L[] validValues) {
		super(field);
		if (identifiers != null) {
			mIdentifiers.addAll(Arrays.asList(identifiers));
		}
		if (validValues != null) {
			mValidValues.addAll(Arrays.asList(validValues));
		}
	}
}
