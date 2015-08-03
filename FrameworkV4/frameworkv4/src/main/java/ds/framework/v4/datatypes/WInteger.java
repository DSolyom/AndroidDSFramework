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
public class WInteger extends Datatype<Integer> {
	
	public WInteger() {
		mType = INTEGER;
	}
	
	public WInteger(int value) {
		mType = INTEGER;
		mValue = value;
	}
	
	@Override
	public String toString() {
		if (mValue == null) {
			return "0";
		}
		return String.valueOf(mValue);
	}

	public void inc() {
		if (mValue == null) {
			mValue = 1;
		} else {
			++mValue;
		}
	}
	
	public void dec() {
		if (mValue == null) {
			mValue = -1;
		} else {
			--mValue;
		}
	}
	
	@Override
	public boolean isEmpty() {
		return mValue == null || mValue == 0;
	}
	
	@Override
	public void set(Serializable value) {
		mValue = (Integer) value;
	}
}
