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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import ds.framework.v4.common.Base64;
import ds.framework.v4.common.Common;
import ds.framework.v4.common.Debug;

public class Settings {
	
	public final static String SETTINGS_IMAGE_CACHE_SIZE = "image_cache_size";
	
	private static Settings sInstance;
	private static Editor sPreferencesEditor;

	public long updateInterval;
	public String updateUrl;
	public String imagesUrl;
	
	private HashMap<String, Object> mSettings = new HashMap<String, Object>();
	
	/**
	 * 
	 * @param context
	 */
	public Settings(Context context) {
		if (context == null) {
			return;
		}
		
		set(context);
	}
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public static Settings getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new Settings(context);
			sInstance.set(context);
			forceLocale(context);
		}
		return sInstance;
	}

	/**
	 * 
	 * @param context
	 */
	protected void set(Context context) {
		getPreferencesEditor(context).commit();
	}

	/**
	 * get current language
	 * 
	 * @return
	 */
	public static String getLanguage() {
		return getLocale().getLanguage();
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getCurrentLocale() {
		return getLocale().toString();
	}
	
	/**
	 * get saved language from preferences
	 * 
	 * @param context
	 * @return
	 */
	public static String getSavedLanguage(Context context) {
		return getPreferences(context).getString("app-language", null);
	}
	
	/**
	 * set language to (by language code)
	 *
	 * @param context
	 * @param language
	 */
	public static void setLanguage(Context context, String language) {
		putString(context, "app-language", language, false);
		putString(context, "old-app-language", language, true);
		forceLocale(context);
	}
	
	/**
	 * force current locale on app
	 * 
	 * @param context
	 */
	public static void forceLocale(Context context) {
		final String language = getSavedLanguage(context);
		
		if (language == null) {
			return;
		}
		
		Debug.logE("force locale", "language " + language);
		
		Resources res = context.getResources();

		Configuration config = res.getConfiguration();
		try {
			config.locale = new Locale(language);
		} catch(Throwable e) {
			;
		}
	    res.updateConfiguration(config, context.getResources().getDisplayMetrics());
	}
	
	/**
	 * get current locale
	 * 
	 * @return
	 */
	public static Locale getLocale() {
		return Global.getContext().getResources().getConfiguration().locale;
	}
		public static Editor getPreferencesEditor(Context context) {
		if (sPreferencesEditor != null) {
			return sPreferencesEditor;
		}
		return getPreferences(context).edit();
	}
	
	public static SharedPreferences getPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	}
	
	/**
	 * clear cache of setting variables
	 */
	public static void clearCache() {
		sInstance.mSettings.clear();
	}
	
	/**
	 * get an integer settings value by key
	 * 
	 * @param key
	 * @param defValue
	 * @return
	 */
	public int getInt(String key, int defValue) {
		return getInt(Global.getContext(), key, defValue);
	}
	
	/**
	 * get an integer settings value by key
	 * 
	 * @param context
	 * @param key
	 * @param defValue
	 * @return
	 */
	public static int getInt(Context context, String key, int defValue) {
		if (sInstance != null && sInstance.mSettings.containsKey(key)) {
			return (Integer) sInstance.mSettings.get(key);
		}
		
		int val = defValue;
		
		try {
			val = getPreferences(context).getInt(key, defValue);
		} catch(ClassCastException e) {
			Debug.logException(e);
		}
		
		if (sInstance != null) {
			sInstance.mSettings.put(key, val);
		}
		
		return val;
	}
	
	/**
	 * put an int value into settings
	 * 
	 * @param key
	 * @param value
	 * @param commit
	 */
	public void putInt(String key, int value, boolean commit) {
		putInt(Global.getContext(), key, value, commit);
	}

	/**
	 * put a string value into settings
	 * 
	 * @param context
	 * @param key
	 * @param value
	 * @param commit
	 */
	public static void putInt(Context context, String key, int value, boolean commit) {
		ensurePreferencesEditor(context);
		sPreferencesEditor.putInt(key, value);
		if (commit) {
			sPreferencesEditor.commit();
			sPreferencesEditor = null;
		}
		if (sInstance != null) {
			sInstance.mSettings.put(key, value);
		}
	}

	/**
	 * get a string settings value by key
	 * 
	 * @param key
	 * @param defValue
	 * @return
	 */
	public String getString(String key, String defValue) {
		return getString(Global.getContext(), key, defValue);
	}

	/**
	 * get a string settings value by key
	 * 
	 * @param context
	 * @param key
	 * @param defValue
	 * @return
	 */
	public static String getString(Context context, String key, String defValue) {
		if (sInstance != null && sInstance.mSettings.containsKey(key)) {
			return (String) sInstance.mSettings.get(key);
		}
		String val = defValue;
		try {
			val = getPreferences(context).getString(key, defValue);
		} catch(ClassCastException e) {
			Debug.logException(e);
		}
		
		if (sInstance != null) {
			sInstance.mSettings.put(key, val);
		}
		
		return val;
	}
	
	/**
	 * put a string value into settings
	 * 
	 * @param key
	 * @param value
	 * @param commit
	 */
	public void putString(String key, String value, boolean commit) {
		putString(Global.getContext(), key, value, commit);
	}

	/**
	 * put a string value into settings
	 * 
	 * @param context
	 * @param key
	 * @param value
	 * @param commit
	 */
	public static void putString(Context context, String key, String value, boolean commit) {
		ensurePreferencesEditor(context);
		sPreferencesEditor.putString(key, value);
		if (commit) {
			sPreferencesEditor.commit();
			sPreferencesEditor = null;
		}
		if (sInstance != null) {
			sInstance.mSettings.put(key, value);
		}
	}

	/**
	 * get an long settings value by key
	 * 
	 * @param key
	 * @param defValue
	 * @return
	 */
	public long getLong(String key, long defValue) {
		return getLong(Global.getContext(), key, defValue);
	}
	
	/**
	 * get an long settings value by key
	 * 
	 * @param context
	 * @param key
	 * @param defValue
	 * @return
	 */
	public static long getLong(Context context, String key, long defValue) {
		if (sInstance != null && sInstance.mSettings.containsKey(key)) {
			return (Long) sInstance.mSettings.get(key);
		}
		long val = defValue;
		try {
			val = getPreferences(context).getLong(key, defValue);
		} catch(ClassCastException e) {
			Debug.logException(e);
		}
		
		if (sInstance != null) {
			sInstance.mSettings.put(key, val);
		}
		
		return val;
	}
	
	/**
	 * put long value into settings
	 * 
	 * @param context
	 * @param key
	 * @param value
	 * @param commit
	 */
	public static void putLong(Context context, String key, long value, boolean commit) {
		ensurePreferencesEditor(context);
		sPreferencesEditor.putLong(key, value);
		if (commit) {
			sPreferencesEditor.commit();
			sPreferencesEditor = null;
		}
		if (sInstance != null) {
			sInstance.mSettings.put(key, value);
		}
	}
	
	/**
	 * get a boolean settings value by key
	 * 
	 * @param context
	 * @param key
	 * @param defValue
	 * @return
	 */
	public static boolean getBoolean(Context context, String key, boolean defValue) {
		if (sInstance !=null && sInstance.mSettings.containsKey(key)) {
			return (Boolean) sInstance.mSettings.get(key);
		}
		boolean val = defValue;
		try {
			val = getPreferences(context).getBoolean(key, defValue);
		} catch(ClassCastException e) {
			Debug.logException(e);
		}
		
		if (sInstance != null) {
			sInstance.mSettings.put(key, val);
		}
		
		return val;
	}
	
	/**
	 * get a boolean settings value by key
	 * 
	 * @param key
	 * @param defValue
	 * @return
	 */
	public boolean getBoolean(String key, boolean defValue) {
		return getBoolean(Global.getContext(), key, defValue);
	}
	
	/**
	 * put a boolean value into settings
	 * 
	 * @param key
	 * @param value
	 * @param commit
	 */
	public void putBoolean(String key, boolean value, boolean commit) {
		putBoolean(Global.getContext(), key, value, commit);
	}

	/**
	 * put a boolean value into settings
	 * 
	 * @param context
	 * @param key
	 * @param value
	 * @param commit
	 */
	public static void putBoolean(Context context, String key, boolean value, boolean commit) {
		ensurePreferencesEditor(context);
		sPreferencesEditor.putBoolean(key, value);
		if (commit) {
			sPreferencesEditor.commit();
			sPreferencesEditor = null;
		}
		if (sInstance != null) {
			sInstance.mSettings.put(key, value);
		}
	}
	
	/**
	 * get a Serializable settings value by key
	 * 
	 * @param context
	 * @param key
	 * @return
	 */
	public static Serializable getSerializable(Context context, String key) {
		if (sInstance !=null && sInstance.mSettings.containsKey(key)) {
			return (Serializable) sInstance.mSettings.get(key);
		}
		Serializable val = null;
		try {
			final String encodedValue = getPreferences(context).getString(key, null);
			if (encodedValue == null) {
				return null;
			}
			val = (Serializable) Common.deserializeObject(Base64.decode(encodedValue.getBytes(), Base64.DEFAULT));
		} catch(ClassCastException e) {
			Debug.logException(e);
		}
		
		if (sInstance != null) {
			sInstance.mSettings.put(key, val);
		}
		
		return val;
	}

	/**
	 * put a Serializable value into settings
	 * 
	 * @param context
	 * @param key
	 * @param value
	 * @param commit
	 */
	public static void putSerializable(Context context, String key, Serializable value, boolean commit) {
		ensurePreferencesEditor(context);
		
		String encodedValue = new String(Base64.encode(Common.serializeObject(value), Base64.DEFAULT));

		sPreferencesEditor.putString(key, encodedValue);
		if (commit) {
			sPreferencesEditor.commit();
			sPreferencesEditor = null;
		}
		if (sInstance != null) {
			sInstance.mSettings.put(key, value);
		}
	}
	
	/**
	 * 
	 * @param context
	 * @param key
	 * @param commit
	 */
	public static void remove(Context context, String key, boolean commit) {
		ensurePreferencesEditor(context);
		sPreferencesEditor.remove(key);
		
		if (commit) {
			sPreferencesEditor.commit();
		}
		if (sInstance != null) {
			sInstance.mSettings.remove(key);
		}
	}
	
	/**
	 * commit preference changes into shared preferences
	 */
	public static void commitChanges(Context context) {
		ensurePreferencesEditor(context);
		sPreferencesEditor.commit();
	}
	
	private static void ensurePreferencesEditor(Context context) {
		if (sPreferencesEditor == null) {
			sPreferencesEditor = getPreferencesEditor(context);
		}
	}
}
