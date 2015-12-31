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

package ds.framework.v4.io;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionChecker {
	public static final int TIMEOUT = 10;
	
	private static int mTries;
	private static boolean mOnlyWifi = false;
	private static ConnectionChecker sInstance;

	private BroadcastReceiver mReceiver;
	private ConnectionChangedListener mConnectionChangedListener;
	private IntentFilter mReceiverIntentFilter;
	
	public static ConnectionChecker getInstance() {
		if (sInstance == null) {
			sInstance = new ConnectionChecker();
		}
		return sInstance;
	}
	
	/**
	 * check for connection waiting for full connection
	 * 
	 * @param context
	 * @return
	 */
	public static boolean check(Context context) {
		return check(context, true);
	}
	
	/**
	 * check for connection
	 * 
	 * @param context
	 * @param waitForFullConnection
	 * @return
	 */
	public static boolean check(Context context, boolean waitForFullConnection) {
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			return false;
		}
		if (mOnlyWifi && (ni.getType() != ConnectivityManager.TYPE_WIFI)) {
			return false;
		}
		
		if (!waitForFullConnection) {
			return ni.isConnected();
		}
		if (ni.isConnectedOrConnecting()) {
			mTries = 0;
			while(!ni.isConnected()) {
				if (mTries++ == TIMEOUT) {
					return false;
				}
				try {
					Thread.sleep(100);
				} catch(InterruptedException e) {
					;
				}
				ni = cm.getActiveNetworkInfo();
				if (ni == null || !ni.isConnectedOrConnecting()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * find route to specified host with the currently working connection<br/>
	 * !note: android.permission.CHANGE_NETWORK_STATE is required for this
	 * 
	 * @param context
	 * @param url
	 * @return
	 */
	public static boolean routeToHost(Context context, String url) {
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
		int ip = HttpURLRequest.getIP(url);
		if (ip == -1) {
			return false;
		}
		return cm.requestRouteToHost(cm.getActiveNetworkInfo().getType(), ip);
	}
	
	/**
	 * register connectivity broadcast receiver
	 */
	public void registerReceiver(Context context, ConnectionChangedListener listener) {
		PackageManager pm = context.getPackageManager();
		if (pm.checkPermission(
			    android.Manifest.permission.ACCESS_NETWORK_STATE, 
			    context.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		if (mReceiver != null) {
			if (listener == mConnectionChangedListener) {
				
				// already registered
				return;
			}
			unregisterReceiver(context);
		}
		mConnectionChangedListener = listener;
		mReceiver = new ConnectivityReceiver();
		mReceiverIntentFilter = new IntentFilter();
		mReceiverIntentFilter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
		
		context.registerReceiver(mReceiver, mReceiverIntentFilter);
	}

	/**
	 * unregister connectivity broadcast receiver
	 */
	public void unregisterReceiver(Context context) {
		if (mReceiver != null) {
			try {
				context.unregisterReceiver(mReceiver);
			} catch(IllegalArgumentException e) {
				;
			}
			mReceiver = null;
		}
		mConnectionChangedListener = null;	
	}
	
	public class ConnectivityReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (!intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                return;
			}
			
			if (mConnectionChangedListener != null) {
				mConnectionChangedListener.onConnectionChanged(!intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false));
			}
		}
		
	}
	
	public interface ConnectionChangedListener {
		public void onConnectionChanged(boolean connected);
	}

	public static void onlyWifi(boolean onlyWifi) {
		mOnlyWifi = onlyWifi;
	}
}
