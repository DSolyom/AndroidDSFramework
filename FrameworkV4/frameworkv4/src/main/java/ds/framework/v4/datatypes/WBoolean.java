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
public class WBoolean extends Datatype<Boolean> {
	
	public WBoolean() {
		mType = BOOLEAN;
	}
	
	public WBoolean(boolean value) {
		mType = BOOLEAN;
		mValue = value;
	}
	
	@Override
	public Boolean get() {
		if (mValue == null) {
			return false;
		}
		return mValue;
	}

	@Override
	public void set(Serializable value) {
		mValue = (Boolean) value;
	}
}
