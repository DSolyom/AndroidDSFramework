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

// import ds.framework.v4.app.AbsScreenFragment;

public class Transport {
	public Object to;
	public Object data;
	// public AbsScreenFragment fromFragment;

	public Transport(Object to) {
		this.to = to;
		this.data = null;
	}
	
	public Transport(Object to, Object data) {
		this.to = to;
		this.data = data;
	}
	
	public Transport(Object to, Object... data) {
		this.to = to;
		this.data = data;
	}
}
