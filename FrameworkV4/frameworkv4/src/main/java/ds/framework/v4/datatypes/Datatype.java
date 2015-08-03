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
abstract public class Datatype<T> implements Serializable {

	public static final int INTEGER = 0;
	public static final int STRING = 1;
	public static final int BOOLEAN = 2;
	public static final int DOUBLE = 3;
	
	public static final int ARRAY = 5;
	
	public static final int INTERVAL = 8;
	
	public static final int TRANSPORT = 10;
	
	public static final int FORMFIELD = 15;
	
	public static final int ADAPTER = 20;
	public static final int CONTINUOUS_LIST = 21;
	
	public static final int LAIZY_IMAGE = 30;
	
	public static final int TYPE_THREAD = 50;
	
	protected T mValue;
	protected int mType;
	
	/**
	 * set value<br/>
	 * <b>!no type check</b>
	 * 
	 * @param value
	 */
	public void set(T value) {
		mValue = value;
	}
	
	@SuppressWarnings("unchecked")
	public void copy(Datatype<?> value) {
		mValue = (T) value.get();
	}
	
	public T get() {
		return mValue;
	}
	
	public boolean equals(Object value) {
		if (mValue == null) {
			return value == null;
		}
		return mValue.equals(value);
	}
	
	public boolean equals(Datatype<T> other) {
		if (this == other) {
			return true;
		}
		if (mValue == null) {
			return other.mValue == null;
		}
		
		return mValue.equals(other.mValue);
	}
	
	@Override
	public String toString() {
		return mValue.toString();
	}
	
	public int getType() {
		return mType;
	}
	
	public boolean isEmpty() {
		return mValue == null;
	}
	
	public void reset() {
		mValue = null;
	}

	/**
	 * to be able to restore values from serialization
	 * 
	 * @param serializable
	 */
	public void set(Serializable serializable) {}
}
