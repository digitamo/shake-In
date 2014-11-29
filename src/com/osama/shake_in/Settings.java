package com.osama.shake_in;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class Settings extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// TODO : use fragment instead
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

				Intent intent = new Intent(this, Listener.class);
				stopService(intent);
			}
		} else if (key.equals("NFC")) {
			Toast.makeText(getApplicationContext(),
					"TODO: disable NFC feature", Toast.LENGTH_SHORT).show();
		} else if (key.equals("foreground_service")
				|| key.equals("seekBarPreference")) {
			// restart the service

			Intent intent = new Intent(this, Listener.class);
			stopService(intent);
			startService(intent);

			Log.d("osama", "service restarted");

			// TODO: use HandlerThread and Handler instead of restarting the
			// service.

			if (key.equals(key.equals("seekBarPreference"))) {
				Toast.makeText(getApplicationContext(), "the senstivity changed",
						Toast.LENGTH_SHORT).show();
			}
		}

	}
}
