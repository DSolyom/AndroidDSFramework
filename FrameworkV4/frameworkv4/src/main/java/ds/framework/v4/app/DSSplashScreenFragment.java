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

import ds.framework.v4.datatypes.Transport;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;


/**
 * @class DSSplashScreenFragment
 * 
 * fragment which moves the activity to the next one after doing background stuff or just waiting for some time
 */
abstract public class DSSplashScreenFragment extends AbsDSAsyncDataFragment {

	private long mStartAt;
	
	// minimal time to stay in this activity - in milliseconds
	protected long mMinDelay = 1500;
	
	@Override
	public void onViewCreated(View rootView) {
		super.onViewCreated(rootView);
		
		mStartAt = System.currentTimeMillis();
	}
	
	@Override
	public void loadData() {
		super.loadData();
	}
	
	@Override
	public void display() {
		super.display();
		
		getDSActivity().getActionBar().hide();
	}
	
	@Override
	public void onDataLoaded() {
		
		// check for time spent - delay if not enough
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				final Transport transport = getTransport();
				try {
					((ActivityInterface) getDSActivity()).forward(transport.to, transport.data);
				} catch(Throwable e) {
					;	// has no activity 
				}
			}
			
		}, mMinDelay - (System.currentTimeMillis() - mStartAt));
	}
	
	public void setMinDelay(long delay) {
		mMinDelay = 1500;
	}
	
	abstract public Transport getTransport();
}
