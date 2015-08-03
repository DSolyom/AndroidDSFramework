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

package ds.framework.v4.datatypes;

import java.io.Serializable;

@SuppressWarnings("serial")
public class WString extends Datatype<String> {
	
	public WString() {
		mType = STRING;
	}
	
	public WString(String string) {
		mType = STRING;
		mValue = string;
	}

	@Override
	public String toString() {
		return mValue == null ? "" : mValue;
	}

	public int length() {
		if (mValue == null) {
			return 0;
		}
		return mValue.length();
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty() || length() == 0;
	}
	
	@Override
	public void set(Serializable value) {
		mValue = (String) value;
	}
}
