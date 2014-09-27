package com.osama.shake_in;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ArrayAdapter;
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

// TODO: you can use placePickerFragment instead of making a listView.

public class Post extends ListActivity implements
		GoogleApiClient.OnConnectionFailedListener,
		GoogleApiClient.ConnectionCallbacks, LocationListener {
	private LocationRequest locationRequest;
	private GoogleApiClient googleApiClient;
	private boolean loggedIn = false;
	private boolean locationDetected;
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

		// TODO make the layout with a dialog theme
		googleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();
		uiHelper = new UiLifecycleHelper(this, callBack);
		uiHelper.onCreate(savedInstanceState);

		if (!loggedIn) {
			// TODO: display a warning dialog
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if (!locationDetected) {
			locationDetected = true;
			Log.d("osama", "location is: " + location.getLatitude() + ", "
					+ location.getLongitude());
			if (Session.getActiveSession().isOpened()) {
				// TODO: post the new Check-In and close the dialog
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
								JSONObject result = response.getGraphObject()
										.getInnerJSONObject();
								try {
									final JSONArray JSONdata = result
											.getJSONArray("data");

									runOnUiThread(new Runnable() {

										@Override
										public void run() {
											data = JSONdata;
											populateListView();
										}
									});
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								// Log.d("osama", response.toString());
							}
						}).executeAsync();

				// TODO: after posting you must dismiss this dialog Activity to
				// stop
				// continuous posting

			} else {
				Log.e("osama", "the session was closed");
			}
		}

		// TODO" unregister listeners and disconnect the client
		// googleApiClient.unregisterConnectionCallbacks(this);
		// googleApiClient.unregisterConnectionFailedListener(this);
		// googleApiClient.disconnect();
		// LocationServices.FusedLocationApi.removeLocationUpdates(
		// googleApiClient, this);
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
			loggedIn = true;
		} else {
			loggedIn = false;
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
		// TODO: dismiss the list after clicking

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
			} else {
				Log.e("osama", "the data was null!!");
			}
		} catch (JSONException e) {
			Log.e("osama", "error parsing JSON!!");
		}
	}

	private void post(String placeId) {
		Bundle params = new Bundle();
		params.putString("message", "I was there :D");
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
