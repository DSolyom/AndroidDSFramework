/*
	Copyright 2012 Dániel Sólyom

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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public interface ActivityInterface extends NavigationInterface {
	
	// for templates
	Integer getUniqueId();
	
	DSActivity getScreenActivity();
	Context getContext();

	View inflate(int resId);
	View inflate(int resId, ViewGroup root);
	View inflate(int resId, ViewGroup viewParent, boolean addToParent);
	
	View getRootView();
	View findViewById(int id);
	
	void requestRefreshActivity(String activityId);
	void requestRefreshActivities(String[] activityIds);
	void requestRefreshActivities(String[] activityIds, String exceptId);

}
