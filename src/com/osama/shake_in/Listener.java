package com.osama.shake_in;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.Looper;
//import android.os.Message;

//  TODO: listen for the motion around the z axis only and you may add a custom gesture
//  TODO: handle sending repeated events
public class Listener extends Service implements SensorEventListener {
	// private Looper mServiceLooper;
	// private ServiceHandler mServiceHandler;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private long lastUpdate = 0;
	private float last_x, last_y, last_z;
	private static final int SHAKE_THRESHOLD = 139;
	private static final int ONGOING_NOTIFICATION_ID = 39;

	// private final BroadcastReceiver receiver = new BroadcastReceiver() {
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
	// KeyEvent event = (KeyEvent) intent
	// .getParcelableExtra(Intent.EXTRA_KEY_EVENT);
	// if (KeyEvent.ACTION_DOWN == event.getKeyCode()) {
	// Toast.makeText(getApplicationContext(), "keydown",
	// Toast.LENGTH_SHORT).show();
	//
	// }
	// }
	// }
	//
	// };

	/*
	 * public final class ServiceHandler extends Handler { public
	 * ServiceHandler(Looper looper) { super(looper); }
	 * 
	 * @Override public void handleMessage(Message msg) { // Handle the message
	 * and stop the service after finishing. super.handleMessage(msg); } }
	 */

	@Override
	public void onCreate() {
		super.onCreate();

		// HandlerThread thread = new HandlerThread("service thread",
		// Thread.NORM_PRIORITY);
		// thread.start();
		//
		// mServiceLooper = thread.getLooper();
		// mServiceHandler = new ServiceHandler(mServiceLooper);
		startForeground();

		// IntentFilter filter = new IntentFilter();
		// filter.addAction(Intent.ACTION_MEDIA_BUTTON);
		//
		// registerReceiver(receiver, filter);

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("osama", "service started");

		/*
		 * // For each start request, send a message to start a job and deliver
		 * the // start ID so we know which request we're stopping when we
		 * finish the // job. Message msg = mServiceHandler.obtainMessage();
		 * msg.arg1 = startId; mServiceHandler.sendMessage(msg);
		 */

		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.e("osama", "Destroyed");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// we won't support bound service
		return null;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		Sensor mySensor = sensorEvent.sensor;

		if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			long curTime = System.currentTimeMillis();
			if ((curTime - lastUpdate) > 100) {
				// float x = sensorEvent.values[0];
				// float y = sensorEvent.values[1];
				float x = 0;
				float y = 0;
				float z = sensorEvent.values[2];

				long diffTime = curTime - lastUpdate;
				lastUpdate = curTime;

				float speed = Math.abs(x + y + z - last_x - last_y - last_z)
						/ diffTime * 1000;

				if (speed > SHAKE_THRESHOLD) {
					vibrate(500);
					Log.d("osama", "Posting to Facebook");

					Intent intent = new Intent(this, Post.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(intent);
				}

				last_x = x;
				last_y = y;
				last_z = z;
			}
		}
	}

	private void startForeground() {

		// Notification notification = new Notification(R.drawable.ic_launcher,
		// "shake-in running", System.currentTimeMillis());

		Intent notificationIntent = new Intent(this, Main.class)
				.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		Notification notification = new Notification.Builder(this)
				.setContentTitle("shake-in")
				.setContentText("I'm ready to shake-in :D")
				.setContentIntent(pendingIntent)
				.setSmallIcon(R.drawable.location_64x64_white)
				.setLargeIcon(
						BitmapFactory.decodeResource(getResources(),
								R.drawable.shake_in)).setAutoCancel(true)
				// .addAction(R.drawable.ic_launcher, "content I", pIntent)
				// .addAction(R.drawable.ic_launcher, "content II", pIntent)
				.build();

		// notification.setLatestEventInfo(this, "shake-in",
		// "I'm ready just shake-in", pendingIntent);
		startForeground(ONGOING_NOTIFICATION_ID, notification);
	}

	public void vibrate(long timeInMillis) {
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(timeInMillis);
	}

}
