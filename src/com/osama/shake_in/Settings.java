package com.osama.shake_in;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

// TODO: use fragment instead of activity.
public class Settings extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// TODO : use something other than addPreferenceFromResource(....) 
		addPreferencesFromResource(R.xml.preferences);

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		pref.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		if (key.equals("gesture")) {
			if (sharedPreferences.getBoolean("gesture", true)) {
				// start the service
				Toast.makeText(this, "starting the service", Toast.LENGTH_SHORT)
						.show();

				startService(new Intent(this, Listener.class));
			} else {
				// kill the service
				Toast.makeText(this, "killing the service", Toast.LENGTH_SHORT)
						.show();

				Intent intent = new Intent(this, Listener.class);
				stopService(intent);
			}

		}
	}
}
