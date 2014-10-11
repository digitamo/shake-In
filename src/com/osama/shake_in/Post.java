package com.osama.shake_in;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

// XXX: you can use placePickerFragment instead of making a listView.

public class Post extends ListActivity implements
		GoogleApiClient.OnConnectionFailedListener,
		GoogleApiClient.ConnectionCallbacks, LocationListener {
	private LocationRequest locationRequest;
	private GoogleApiClient googleApiClient;
	private TextView countText;
	private CountDownTimer counter;
	private boolean locationDetected;
	private ProgressBar progressBar;
	private UiLifecycleHelper uiHelper;
	private JSONArray data;
	private Session.StatusCallback callBack = new Session.StatusCallback() {

		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post);
		locationDetected = false;

		// TODO: check for Internet, GPS and NFC.

		// checking for Internet connection.
		if (!isInternetActive()) {
			// TODO: run this dialog separately or stop the activity life cycle
			// or unregister listeners when this dialog is shown
			new AlertDialog.Builder(this).setTitle("Warning!")
					.setMessage("you don't have Internet Connrectivity :(")
					.setPositiveButton("Ok", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).setIcon(R.drawable.error).show();
		}

		// check for GPS
		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}

		googleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();
		uiHelper = new UiLifecycleHelper(this, callBack);
		uiHelper.onCreate(savedInstanceState);

		progressBar = (ProgressBar) findViewById(R.id.progress);
	}

	private boolean isInternetActive() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnectedOrConnecting();
	}

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Your GPS seems to be disabled, do you want to enable it?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(
									final DialogInterface dialog,
									final int id) {
								startActivity(new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {
						dialog.cancel();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public void onLocationChanged(Location location) {
		if (!locationDetected) {
			Log.d("osama", "location is: " + location.getLatitude() + ", "
					+ location.getLongitude());
			locationDetected = true;
			// TODO: how to detect that the user is logged out
			// if (Session.getActiveSession().is) {
			// // this didn't make a difference
			// Toast.makeText(this, "you're logged out", Toast.LENGTH_SHORT)
			// .show();
			// }
			if (Session.getActiveSession().isOpened()) {
				// make a call to retrieve the page ID

				/*
				 * // I will use the PlacePickerFragment FragmentManager
				 * fragmentManager = getSupportFragmentManager();
				 * PlacePickerFragment placePickerFragment =
				 * (PlacePickerFragment) fragmentManager
				 * .findFragmentById(R.id.PlacePickerFragment); if
				 * (placePickerFragment != null) {
				 * placePickerFragment.setLocation(location);
				 * placePickerFragment.setRadiusInMeters(100); }
				 */

				// the fql is not available at v2.1 :'(

				Bundle params = new Bundle();
				// params.putString(
				// "q",
				// "SELECT page_id,latitude,longitude FROM place WHERE distance(latitude, longitude, "
				// + location.getLatitude()
				// + ", "
				// + location.getLongitude() + ") < 250");
				params.putString("type", "place");
				params.putString(
						"center",
						String.valueOf(location.getLatitude() + ","
								+ String.valueOf(location.getLongitude())));
				params.putString("distance", "100");

				// Bundle params = new Bundle();
				// params.putString("q", "ritual");
				// params.putString("type", "place");
				// params.putString("center", "37.76,-122.427");
				// params.putString(
				// "center",
				// String.valueOf(location.getLatitude() + ", "
				// + String.valueOf(location.getLongitude())));
				// params.putString("distance", "100");
				new Request(Session.getActiveSession(), "/search", params,
						HttpMethod.GET, new Request.Callback() {

							@Override
							public void onCompleted(Response response) {

								try {
									JSONObject result = response
											.getGraphObject()
											.getInnerJSONObject();
									try {
										final JSONArray JSONdata = result
												.getJSONArray("data");

										runOnUiThread(new Runnable() {

											@Override
											public void run() {
												data = JSONdata;
												populateListView();
												progressBar
														.setVisibility(View.GONE);
												progressBar = null;
												getListView().setVisibility(
														View.VISIBLE);

												countText = (TextView) findViewById(R.id.timer);
												counter = new CountDownTimer(
														9000, 1000) {

													@Override
													public void onTick(
															long millisUntilFinished) {
														countText
																.setText("00:"
																		+ millisUntilFinished
																		/ 1000);
													}

													@Override
													public void onFinish() {
														if (data != null) {
															try {
																String placeId = data
																		.getJSONObject(
																				0)
																		.getString(
																				"id");
																post(placeId);
																countText
																		.setText("--:--");
																counter.cancel();
																finish();
															} catch (JSONException e) {
																Log.e("osama",
																		e.toString());
															}
														}

													}
												}.start();
											}
										});
									} catch (JSONException e) {
										Log.e("osama", "error parsing JSON!!");
										e.printStackTrace();
									}
								} catch (NullPointerException ex) {
									Log.e("osama",
											"no Internet connection or null response from FB");
								}
							}
						}).executeAsync();

			} else {
				Log.e("osama", "you don't have an open session!!");

				// TODO: display a warning dialog to login and exit
				new AlertDialog.Builder(this).setTitle("Warning")
						.setMessage("please login with facebook")
						.setPositiveButton("login", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent(getBaseContext(),
										Main.class);
								// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
								// | Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);

							}
						}).setNegativeButton("Cancel", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						}).setIcon(R.drawable.error).show();
			}
		} else {
			// googleApiClient.disconnect();
			// googleApiClient.unregisterConnectionCallbacks(this);
			// googleApiClient.unregisterConnectionFailedListener(this);
			// LocationServices.FusedLocationApi.removeLocationUpdates(
			// googleApiClient, this);
		}

	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// create a Location Request
		locationRequest = LocationRequest.create()
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setInterval(1000);

		LocationServices.FusedLocationApi.requestLocationUpdates(
				googleApiClient, locationRequest, this);
	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.i("osama", "Connection suspended");
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.e("osama", "connection Failed");
	}

	protected void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (state.isOpened()) {
			Toast.makeText(getBaseContext(), "logged in :)", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(getBaseContext(), "you're logged out :(",
					Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		googleApiClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		googleApiClient.disconnect();
		uiHelper.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
		if (googleApiClient.isConnected()) {
			googleApiClient.disconnect();
			googleApiClient.unregisterConnectionCallbacks(this);
			googleApiClient.unregisterConnectionFailedListener(this);
			LocationServices.FusedLocationApi.removeLocationUpdates(
					googleApiClient, this);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		try {
			if (data != null) {
				String placeId = data.getJSONObject(position).getString("id");
				post(placeId);
				Toast.makeText(
						this,
						"posting check-in at: "
								+ data.getJSONObject(position)
										.getString("name"), Toast.LENGTH_SHORT)
						.show();
				counter.cancel();
				finish();
			} else {
				Log.e("osama", "the data was null!!");
			}
		} catch (JSONException e) {
			Log.e("osama", "error parsing JSON!!");
		}
	}

	private void post(String placeId) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		String message = sharedPreferences.getString("message",
				"I was there :)");
		Bundle params = new Bundle();
		params.putString("message", message);
		params.putString("place", placeId);

		new Request(Session.getActiveSession(), "/me/feed", params,
				HttpMethod.POST, new Request.Callback() {

					@Override
					public void onCompleted(Response response) {
						Log.d("osama",
								"the post response was: " + response.toString());
					}
				}).executeAsync();
	}

	private void populateListView() {
		if (data != null) {
			ArrayList<String> places = new ArrayList<>();
			for (int i = 0; i < data.length(); i++) {
				try {
					places.add(data.getJSONObject(i).optString("name"));
				} catch (JSONException e) {
					Log.e("osama", "there was an error in parsing JSON");
				}
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
					R.layout.simple_list, places);
			setListAdapter(adapter);
		} else {
			Log.e("osama", "the data or JSONdata was null!!");
		}
	}

}
