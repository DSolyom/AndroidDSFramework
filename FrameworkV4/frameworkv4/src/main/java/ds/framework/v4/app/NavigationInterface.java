/*
	Copyright 2013 Dániel Sólyom

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
package ds.framework.v4.app;

public interface NavigationInterface {

	/**
	 * transport to target without added data
	 * 
	 * @param to
	 */
	void transport(Object to);
	
	/**
	 * transport to target
	 * 
	 * @param to
	 * @param data
	 */
	void transport(Object to, Object... data);
	
	/**
	 * forward to target removing current from navigation history
	 * 
	 * @param to
	 * @param data
	 */
	void forward(Object to, Object... data);
	
	/**
	 * forward to target creating new task and removing the current one
	 * 
	 * @param to
	 * @param data
	 */
	void forwardAndClear(Object to, Object... data);
	
	/**
	 * actively go back to the previous target
	 * 
	 * @param result
	 * @return
	 */
	void goBack(Object result);
	
	/**
	 * actively go back to a previous target
	 * 
	 * @param to
	 * @param data
	 */
	void goBackTo(Object to, Object data);
}
