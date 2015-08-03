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
package ds.framework.v4.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import ds.framework.v4.Global;
import ds.framework.v4.Settings;

public class DSPreferenceActivity extends PreferenceActivity {
	
	private static final int ACTIVITY_ID_SETTINGS = 9576;

	public DSPreferenceActivity() {
		super();
	}
	
	public DSPreferenceActivity(Activity activity, int xmlPreferences) {
		Intent i = new Intent(activity, getClass());
		i.putExtra("preferences_xml", xmlPreferences);
		activity.startActivityForResult(i, ACTIVITY_ID_SETTINGS);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent i = getIntent();
		int prefXml = i.getIntExtra("preferences_xml", 0);

		if (prefXml == 0) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
		
		addPreferencesFromResource(prefXml);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		Global.setCurrentActivity(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		Global.setCurrentActivity(null);
	}

	/**
	 * 
	 * @param key
	 */
	public void addPreferenceListener(String key) {
		try {
			Preference pref = findPreference(key);
			pref.setOnPreferenceChangeListener(new OnSettingChangeListener(pref, true));
		} catch(NullPointerException e) {
			; // this pref was not in the app specific xml
		}
	}
	
	/**
	 * 
	 * @param key
	 * @param entries
	 * @param values
	 */
	public void setupListPreference(String key, String[] entries, String[] values) {
		ListPreference pref = (ListPreference) findPreference(key);
		pref.setEntries(entries);
		pref.setEntryValues(values);
	}
	
	public class OnSettingChangeListener implements OnPreferenceChangeListener {

		private boolean mSummaryIsSelected;

		/**
		 * @param preference - the owner of this listener
		 * @param summaryIsSelected - if true selected items name will be the summary of the preference
		 */
		public OnSettingChangeListener(Preference preference, boolean summaryIsSelected) {
			mSummaryIsSelected = summaryIsSelected;
			
			if (preference instanceof ListPreference) {
				preference.setSummary(((ListPreference) preference).getEntry());
			} else if (preference instanceof EditTextPreference) {
				final String key = preference.getKey();
				if ("password".equals(key) || "new_password".equals(key)) {
					preference.setSummary("******");
				} else {
					preference.setSummary(((EditTextPreference) preference).getEditText().getText());
				}
			} else if (preference instanceof RingtonePreference) {
				String summary;
				try {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					Uri ringtoneUri = Uri.parse(prefs.getString(preference.getKey(), android.provider.Settings.System.DEFAULT_NOTIFICATION_URI.toString()));
					Ringtone ringtone = RingtoneManager.getRingtone(DSPreferenceActivity.this, ringtoneUri);
					summary = ringtone.getTitle(DSPreferenceActivity.this);
				} catch(NullPointerException e) {
					summary = "";
				}

				preference.setSummary(summary);
			}
		}
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			return DSPreferenceActivity.this.onPreferenceChange(preference, value, mSummaryIsSelected);
		}

	}
	
	/**
	 * 
	 * @param preference
	 * @param value
	 * @param summaryIsSelected
	 * @return
	 */
	protected boolean onPreferenceChange(Preference preference, Object value, boolean summaryIsSelected) {
		Settings.clearCache();
		
		if (summaryIsSelected) {
			if (preference instanceof ListPreference) {
				preference.setSummary(((ListPreference) preference).getEntries()[((ListPreference) preference).findIndexOfValue((String) value)]);
			} else if (preference instanceof EditTextPreference) {
				final String key = preference.getKey();
				if ("password".equals(key) || "new_password".equals(key)) {
					preference.setSummary("******");
				} else {
					preference.setSummary(((EditTextPreference) preference).getEditText().getText());
				}
			} else if (preference instanceof RingtonePreference) {
				String summary;
				try {
					Uri ringtoneUri = Uri.parse(value.toString());
					Ringtone ringtone = RingtoneManager.getRingtone(DSPreferenceActivity.this, ringtoneUri);
					summary = ringtone.getTitle(DSPreferenceActivity.this);
				} catch(NullPointerException e) {
					summary = "";
				}
				
				preference.setSummary(summary);
			}
		}
		return true;
	}
}
