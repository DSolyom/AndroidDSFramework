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
package ds.framework.v4;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.DisplayMetrics;
import ds.framework.v4.common.Debug;
import ds.framework.v4.db.Db;
import ds.framework.v4.io.ImageLoader;

public class Global {
	
	protected static Global sInstance;
	
	static ArrayList<Activity> sActivities = new ArrayList<Activity>();

	public boolean mDEBUG = false;
	public float[] mScreenInfo = new float[] { 0.0f, 0.0f, 0.0f };

	private ImageLoader mImageLoader;
	private Context mContext;
	private Activity mCurrentActivity;
	
	private Db mDb;
	
	/**
	 * for Dialogs to be removed on the activities onPause
	 */
	private Dialog mRegisteredDialog;
	
	private boolean mIsLargeScreen;
	
	/**
	 * 
	 * @return
	 */
	public static Global getInstance() {
		if (sInstance == null) {
			sInstance = new Global();
		}
		
		return sInstance;
	}
	
	/**
	 * 
	 */
	public Global() {
		;
	}
	
	/**
	 * 
	 * @param context
	 */
	public void onApplicationCreate(Context context) {
		mContext = context;
	    try {
			PackageManager pm = context.getPackageManager();
	        ApplicationInfo appinfo = pm.getApplicationInfo(context.getPackageName(), 0);
	        sInstance.mDEBUG = (0 != (appinfo.flags &= ApplicationInfo.FLAG_DEBUGGABLE));
	    } catch(Throwable e) {
	        e.printStackTrace();
	    }
	    
	    sInstance.mIsLargeScreen = context.getResources().getBoolean(R.bool.screen_large); 
	}
	
	public static Context getContext() {
		return sInstance.mContext;
	}
	
	public static boolean isLargeScreen() {
		return sInstance.mIsLargeScreen;
	}
	
	/**
	 * for activities to set themselves as running
	 * 
	 * @param a
	 */
	public static void setCurrentActivity(Activity a) {
		sInstance.mCurrentActivity = a;
		
		if (a == null) {
			Global.removeRegisteredDialog();
		} else {
			sInstance.mContext = a.getApplicationContext();
		}
	}
	
	/**
	 * 
	 * @param a
	 */
	public static void onActivityDestroyed(Activity a) {
//		Debug.logD("activity destroyed", a.toString() + " " + sActivities.size() + "");
		sActivities.remove(a);
	}
	
	/**
	 * get the currently running activity<br/>
	 * it will return null if not any of our activities is running at the moment
	 * 
	 * @return
	 */
	public static Activity getCurrentActivity() {
		return sInstance.mCurrentActivity;
	}
	
	public static boolean isDEBUG() {
		return sInstance.mDEBUG;
	}
	
	/**
	 * get the global db
	 * 
	 * @return
	 */
	public static Db getOpenDb() {
		if (sInstance.mDb == null) {
			sInstance.mDb = sInstance.createDb();
			
			if (sInstance.mDb != null) {
				sInstance.mDb.open(sInstance.mContext);
			}
		}
		
		return sInstance.mDb;
	}
	
	/**
	 * Override if you need a database
	 */
	public Db createDb() {
		return null;
	}
	
	/**
	 * get the global image loader instance
	 * 
	 * @return
	 */
	public static ImageLoader getImageLoader() {
		final Context context = getContext();
		try {
			if (sInstance.mImageLoader == null) {
				sInstance.mImageLoader = getImageLoaderInstance(context);
			}
			return sInstance.mImageLoader;
		} catch(Throwable e) {
			Debug.logException(e);
			return null;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public static ImageLoader getImageLoaderInstance(Context context) {
		int cs = 0;

		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		cs = 1024 * am.getLargeMemoryClass() / 7;

		return ImageLoader.getInstance(context, cs, sInstance.mContext.getPackageName() + "/images/", 
				Settings.getInstance(getContext()).getLong(Settings.SETTINGS_IMAGE_CACHE_SIZE, -1));
	}
	
	/*
	 * Screen stuff
	 */
	public static float getDipMultiplier() {
		ensureMetrics();
		return sInstance.mScreenInfo[2];
	}
	
	public static float getScreenWidth() {
		ensureMetrics();
		return sInstance.mScreenInfo[0];
	}
	
	public static float getScreenHeight() {
		ensureMetrics();
		return sInstance.mScreenInfo[1];
	}
	
	public static void clearScreenInfo() {
		sInstance.mScreenInfo = new float[] { 0.0f, 0.0f, 0.0f };
	}
	
	private static void ensureMetrics() {
		try {
			DisplayMetrics metrics = new DisplayMetrics();
			Global.getCurrentActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
			sInstance.mScreenInfo[0] = metrics.widthPixels;
			sInstance.mScreenInfo[1] = metrics.heightPixels;
			sInstance.mScreenInfo[2] = metrics.density;
		} catch(Throwable e) {
			Debug.logException(e);
		}
	}
	
// ---------- Dialog ----------
	
	/**
	 * 
	 * @param dialog
	 */
	public static void registerDialog(Dialog dialog) {
		sInstance.mRegisteredDialog = dialog;
	}
	
	/**
	 * 
	 */
	public static void removeRegisteredDialog() {
		if (sInstance.mRegisteredDialog != null && sInstance.mRegisteredDialog.isShowing()) {
			sInstance.mRegisteredDialog.dismiss();
		}
		sInstance.mRegisteredDialog = null;
	}
}
