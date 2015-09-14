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

import java.util.Calendar;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import ds.framework.v4.common.Common;

public class LocationChecker {
	
	public final static int GPS = 0;
	public final static int NETWORK = 1;
	public static final long DEFAULT_LOCATION_UPDATE_FREQUENCY = 60000;
	public static final long DEFAULT_LOCATION_EXPIRE_TIME = 60000;

	private MyLocationListener mGPSListener;
	private MyLocationListener mNetworkListener;
	
	private boolean mGpsOn;
	private boolean mNetworkOn;

	private boolean mGpsOnly = false;

	public OnStateChangedListener mOnStateChangedListener;
	public OnLocationChangedListener mOnLocationChangedListener;
	private boolean mStarted = false;
	
	private Context mContext;
	private long mLocationUpdateFrequency;
	private long mLocationExpireTime = DEFAULT_LOCATION_EXPIRE_TIME;
	private float mMinAccuracy = 50.0f;
	private float mMinAccuracyMultiplier = 1.8f;
	
	private boolean mNeedAccuracy = false;
	private boolean mFastCheckTillFirstLocation = false;
	private Location mLastLocation;
	private long mTimeOfFirstTry = 0;
	
	public LocationChecker(Context context) {
		mContext = context;
		
		mLocationUpdateFrequency = DEFAULT_LOCATION_UPDATE_FREQUENCY;
	}
	
	synchronized public void start() {
		if (mStarted) {
			return;
		}
		
		final LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		
		mNetworkOn = !mGpsOnly && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		mGpsOn = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
	
		mTimeOfFirstTry = Common.getCalendarTimeInMillisUTC();
		
		requestLocationUpdates(lm);
		
		mStarted  = true;
	}
	
	synchronized public void stop() {
		if (!mStarted) {
			return;
		}
		
		final LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		
		lm.removeUpdates(mGPSListener);
		lm.removeUpdates(mNetworkListener);
		
		mStarted = false;
		mGpsOn = false;
		mNetworkOn = false;
	}
	
	public void reset() {
		mLastLocation = null;
		
		if (mStarted) {
			stop();
			start();
		}
	}
	
	private void requestLocationUpdates(LocationManager lm) {
		if (mNetworkListener != null) {
			lm.removeUpdates(mNetworkListener);
		}
		if (mGPSListener != null) {
			lm.removeUpdates(mGPSListener);
		}
		
		try {
			lm.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 
					mFastCheckTillFirstLocation ? 0 : mLocationUpdateFrequency, 0, 
					mNetworkListener = new MyLocationListener(NETWORK));
		} catch(Throwable e) {
			;
		}
		try {
			lm.requestLocationUpdates( LocationManager.GPS_PROVIDER, 
					mFastCheckTillFirstLocation ? 0 : mLocationUpdateFrequency, 0, 
					mGPSListener = new MyLocationListener(GPS));
		} catch(Throwable e) {
			;
		}
	}
	
	/**
	 * set update frequency
	 * 
	 * @param frequency
	 */
	public void setUpdateFrequency(long frequency) {
		mLocationUpdateFrequency = frequency;
	}
	
	/**
	 * obsolete period for gps coordinates versus network coordinates
	 * 
	 * @param timeInMillis
	 */
	public void setTimeToExpire(long timeInMillis) {
		mLocationExpireTime = timeInMillis;
	}
	
	/**
	 * only use gps?
	 * 
	 * @param val
	 */
	public void setGpsOnly(boolean val) {
		mGpsOnly = val;
	}
	
	/**
	 * minimum valid base accuracy
	 * 
	 * @param accuracy
	 */
	public void setMinAccuracy(float accuracy) {
		mMinAccuracy = accuracy;
	}
	
	/**
	 * minimum valid accuracy will be multiplied with this over time
	 * 
	 * @param mul
	 */
	public void setMinAccuracyMultiplier(float mul) {
		mMinAccuracyMultiplier = mul;
	}
	
	/**
	 * set on state changed listener
	 * 
	 * @param listener
	 */
	public void setOnStateChangedListener(OnStateChangedListener listener) {
		mOnStateChangedListener = listener;
	}
	
	/**
	 * set on location changed listener
	 * 
	 * @param listener
	 */
	public void setOnLocationChangedListener(OnLocationChangedListener listener) {
		mOnLocationChangedListener = listener;
	}
	
	public boolean isGpsWorking() {
		return mGpsOn;
	}
	
	public boolean isNetworkWorking() {
		return !mGpsOnly && mNetworkOn;
	}
	
	public boolean isWorking() {
		return isGpsWorking() || isNetworkWorking();
	}
	
	/**
	 * get current location - force get providers last know location
	 * 
	 * @return
	 */
	public Location getLocationForced() {
		boolean networkOn;
		boolean gpsOn;
		
		final LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
	
		if (!mStarted) {
			networkOn = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			gpsOn = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} else {
			networkOn = mNetworkOn;
			gpsOn = mGpsOn;
		}
		
		Location location = getLocation();
			
		if (location == null) {
			if (gpsOn) {
				location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			if (location == null && networkOn) {
				location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
		}
		return location;
	}
	
	/**
	 * last location expired?
	 * 
	 * @return
	 */
	public boolean isExpired() {
		return mLastLocation != null && Calendar.getInstance().getTimeInMillis() - mLastLocation.getTime() > mLocationExpireTime; 
	}
	
	/**
	 * got a locataion? ever? (valid to call only before getLocation())
	 * 
	 * @return
	 */
	public boolean hasLocation() {
		return mLastLocation != null;
	}
	
	/**
	 * get last known location
	 * 
	 * @return
	 */
	public Location getLocation() {
		if (isExpired()) {

			// locations are obsolete - restore fast check if needed
			mLastLocation = null;
			
			synchronized(this) {
				if (mStarted && mFastCheckTillFirstLocation && mLocationUpdateFrequency != 0) {
					requestLocationUpdates((LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE));
				}
			}
		}

		return mLastLocation;
	}
	
	class MyLocationListener implements LocationListener {
		
		private int type;
		
		public MyLocationListener(int type) {
			this.type = type;
		}

		@Override
		public void onLocationChanged(Location location) {
			location.setTime(Common.getCalendarTimeInMillisUTC());
			
			// check if this location is accurate enough
			if (location.hasAccuracy()) {
				float mul = 1;
				float omul = 1;
				
				long lastTime = 0;
				if (mLastLocation != null) {
					lastTime = mLastLocation.getTime();
				} else {
					lastTime = mTimeOfFirstTry;
				}
				final float omam = ((mMinAccuracyMultiplier - 1) / 3) + 1; 
				long locTimeDiff = location.getTime() - lastTime;
				while(locTimeDiff > 3000) {
					mul *= mMinAccuracyMultiplier;
					omul *= omam;
					locTimeDiff -= 3000;
				}
				
				if (location.getAccuracy() > mMinAccuracy * mul || 
						mLastLocation != null 
						&& mLastLocation.hasAccuracy() 
						&& !isExpired()
						&& (location.getAccuracy() > mLastLocation.getAccuracy() * omul))	{
					return;
				}
			} else if (mNeedAccuracy) {
				return;
			}
			
			if (mLastLocation == null) {
				if (mFastCheckTillFirstLocation && mLocationUpdateFrequency != 0) {
					requestLocationUpdates((LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE));
				}
			} else if (mLastLocation.getLatitude() == location.getLatitude() && mLastLocation.getLongitude() == location.getLongitude()) {
				
				// same location
				// still save it because it is newer
				mLastLocation = location;
				return;
			}
			
			mLastLocation = location;
			
			if (mOnLocationChangedListener != null) {
				mOnLocationChangedListener.onLocationChanged(location);
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			boolean on = mGpsOn || mNetworkOn;
			
			switch(type) {
				case GPS:
					mGpsOn = false;
					break;
					
				case NETWORK:
					mNetworkOn = false;
					break;
			}
			
			if (on && !(mGpsOn || mNetworkOn) && mOnStateChangedListener != null) {
				mOnStateChangedListener.onStateChanged(false);
			}
		}

		@Override
		public void onProviderEnabled(String provider) {
			boolean on = mGpsOn || mNetworkOn;
			
			switch(type) {
				case GPS:
					mGpsOn = true;
					break;
					
				case NETWORK:
					if (!mGpsOnly) {
						mNetworkOn = true;
					}
					break;
			}
			
			if (!on && (mGpsOn || mNetworkOn) && mOnStateChangedListener != null) {
				mOnStateChangedListener.onStateChanged(true);
			}
		}

		@Override
		public void onStatusChanged(String provider, int state, Bundle extras) {
			boolean on = mGpsOn || mNetworkOn;
			
			switch(type) {
				case GPS:
					mGpsOn = (state == LocationProvider.AVAILABLE);
					break;
					
				case NETWORK:
					mNetworkOn = (state == LocationProvider.AVAILABLE);
					break;
			}
			
			if (((mGpsOn || mNetworkOn) ^ on) && mOnStateChangedListener != null) {
				mOnStateChangedListener.onStateChanged(!on);
			}
		}
		
	}
	
	/**
	 *	interface to act on state change 
	 */
	public interface OnStateChangedListener {
		public void onStateChanged(boolean on);
	}
	
	/**
	 *	interface to act on location change
	 */
	public interface OnLocationChangedListener {
		public void onLocationChanged(Location location);
	}
}
