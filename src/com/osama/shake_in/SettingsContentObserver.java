package com.osama.shake_in;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.widget.Toast;

public class SettingsContentObserver extends ContentObserver {
//	int previousVolume;
	Context context;

	public SettingsContentObserver(Context c, Handler handler) {
		super(handler);
		context = c;

		// AudioManager audio = (AudioManager) context
		// .getSystemService(Context.AUDIO_SERVICE);
		// previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
	}

	@Override
	public boolean deliverSelfNotifications() {
		return super.deliverSelfNotifications();
	}

	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);

		// AudioManager audio = (AudioManager) context
		// .getSystemService(Context.AUDIO_SERVICE);
		// int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

		// int delta = previousVolume - currentVolume;

		Toast.makeText(context, "volume ::)", Toast.LENGTH_SHORT).show();

		// if (delta > 0) {
		// previousVolume = currentVolume;
		// } else if (delta < 0) {
		// previousVolume = currentVolume;
		// }
	}

}