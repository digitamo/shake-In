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
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

// XXX: you can use placePickerFragment instead of making a listView.

public class Post extends ListActivity implements
		GoogleApiClient.OnConnectionFailedListener,
		GoogleApiClient.ConnectionCallbacks, LocationListener,
		CreateNdefMessageCallback, OnNdefPushCompleteCallback {
	private LocationRequest locationRequest;
	private String id;
	private String userName;
	private String friendId;
	private NfcAdapter nfcAdapter;
	private GoogleApiClient googleApiClient;
	// private TextView countText;
	// private CountDownTimer counter;
	private boolean locationDetected;
	private boolean myPlaces = false;
	private Location location;
	private ProgressBar progressBar;
	private UiLifecycleHelper uiHelper;
	private JSONArray data;
	private JSONArray myPlacesData;
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

		getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.post);
		locationDetected = false;

		setUserId();
		// checking for Internet connection.
		if (!isInternetActive()) {
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
		googleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

			if (sharedPreferences.getBoolean("GPSWarning", true)) {
				buildAlertMessageNoGps();
			}
		}
		if (sharedPreferences.getBoolean("NFC", true)) {
			prepareNFC();
		}
		uiHelper = new UiLifecycleHelper(this, callBack);
		uiHelper.onCreate(savedInstanceState);

		progressBar = (ProgressBar) findViewById(R.id.progress);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// super.onNewIntent(intent);
		setIntent(intent);
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

		Intent intent = getIntent();
		String action = intent.getAction();
		if (action != null) {
			if (action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
				Parcelable[] parcelables = intent
						.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
				NdefMessage inNdefMessage = (NdefMessage) parcelables[0];
				NdefRecord[] inNdefRecords = inNdefMessage.getRecords();
				NdefRecord NdefRecord_0 = inNdefRecords[0];
				friendId = new String(NdefRecord_0.getPayload());
			}
		} else {
			Log.d("osama", "the intent is not from the NFC!!");
		}

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

		// handle the share dialog result here
		// uiHelper.onActivityResult(requestCode, resultCode, data,
		// new FacebookDialog.Callback() {
		// // XXX:you can handle the shareDialog response here
		// @Override
		// public void onError(FacebookDialog.PendingCall pendingCall,
		// Exception error, Bundle data) {
		// Log.e("Post",
		// String.format("Error: %s", error.toString()));
		// }
		//
		// @Override
		// public void onComplete(
		// FacebookDialog.PendingCall pendingCall, Bundle data) {
		// Toast.makeText(
		// getBaseContext(),
		// String.valueOf(FacebookDialog
		// .getNativeDialogDidComplete(data)),
		// Toast.LENGTH_SHORT).show();
		//
		// Log.i("Activity", "Success!");
		// finish();
		//
		// }
		// });
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// create a Location Request
		locationRequest = LocationRequest.create()
				.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
				.setInterval(1000);

		LocationServices.FusedLocationApi.requestLocationUpdates(
				googleApiClient, locationRequest, this);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.e("osama", "connection Failed");
	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.i("osama", "Connection suspended");
	}

	@Override
	public void onLocationChanged(Location location) {
		if (!locationDetected) {
			Log.d("osama", "location is: " + location.getLatitude() + ", "
					+ location.getLongitude());
			locationDetected = true;
			this.location = location;
			if (Session.getActiveSession().isOpened()) {
				// make a call to retrieve the page ID

				// XXX: the fql is not available at v2.1 :'(

				Bundle params = new Bundle();
				params.putString("type", "place");
				params.putString(
						"center",
						String.valueOf(location.getLatitude() + ","
								+ String.valueOf(location.getLongitude())));
				params.putString("distance", "100");

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

												// countText = (TextView)
												// findViewById(R.id.timer);
												// counter = new CountDownTimer(
												// 10000, 1000) {
												//
												// @Override
												// public void onTick(
												// long millisUntilFinished) {
												// countText
												// .setText("00:"
												// + millisUntilFinished
												// / 1000);
												// }
												//
												// @Override
												// public void onFinish() {
												// if (data != null) {
												// try {
												// String placeId = data
												// .getJSONObject(
												// 0)
												// .getString(
												// "id");
												// countText
												// .setText("--:--");
												// counter.cancel();
												// post(placeId);
												// } catch (JSONException e) {
												// Log.e("osama",
												// e.toString());
												// }
												// }
												//
												// }
												// }.start();
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

				new AlertDialog.Builder(this).setTitle("Warning")
						.setMessage("please login with facebook")
						.setPositiveButton("login", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent(getBaseContext(),
										Main.class);
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
		}

	}

	@Override
	public void onNdefPushComplete(NfcEvent event) {
		finish();
	}

	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		if (friendId != null) {
			id = id + "," + friendId;
		}

		final String stringOut = id;
		// final String stringOut = "this is ID";
		// runOnUiThread(new Runnable() {
		//
		// @Override
		// public void run() {
		//
		// Toast.makeText(getApplicationContext(), stringOut,
		// Toast.LENGTH_LONG).show();
		// }
		// });

		byte[] bytesOut = stringOut.getBytes();

		NdefRecord ndefRecordOut = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				"text/plain".getBytes(), new byte[] {}, bytesOut);
		NdefRecord[] ndefRecords = { ndefRecordOut };

		NdefMessage ndefMessageout = new NdefMessage(ndefRecords);

		Log.d("osama", ndefMessageout.toString());
		return ndefMessageout;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (myPlaces) {
			if (myPlacesData != null) {
				try {
					// String objectId = myPlacesData.getJSONObject(position)
					// .getString("id");

					String placeId = myPlacesData.getJSONObject(position)
							.getString("id");

					postOldPlace(placeId);
					// counter.cancel();
					finish();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Log.e("osama", "myPlacesData was null!!");
			}
		} else {
			try {
				if (data != null) {
					String placeId = data.getJSONObject(position).getString(
							"id");
					post(placeId);
					Toast.makeText(
							this,
							"posting check-in at: "
									+ data.getJSONObject(position).getString(
											"name"), Toast.LENGTH_SHORT).show();
					// counter.cancel();
					finish();
				} else {
					Log.e("osama", "the data was null!!");
				}
			} catch (JSONException e) {
				Log.e("osama", "error parsing JSON!!");
			}
		}
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

	private boolean isInternetActive() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnectedOrConnecting();
	}

	private void prepareNFC() {

		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (nfcAdapter == null) {
			// TODO: try to enable NFC!!

			Toast.makeText(getApplicationContext(), "please enable NFC!",
					Toast.LENGTH_LONG).show();
		} else {
			nfcAdapter.setNdefPushMessageCallback(this, this);
			nfcAdapter.setOnNdefPushCompleteCallback(this, this);

			Toast.makeText(getApplicationContext(),
					"use NFC to tag your friend", Toast.LENGTH_LONG).show();
		}
	}

	private void buildAlertMessageNoGps() {
		View checkBoxView = View.inflate(this, R.layout.checkbox, null);
		CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {

				if (isChecked) {
					SharedPreferences sharedPreferences = PreferenceManager
							.getDefaultSharedPreferences(getBaseContext());
					sharedPreferences.edit().putBoolean("GPSWarning", false)
							.apply();
				}
			}
		});

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Your GPS seems to be disabled, do you want to enable it?")
				.setCancelable(true)
				.setView(checkBoxView)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
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

	private void setUserId() {
		final Session session = Session.getActiveSession();
		if (session != null && session.isOpened()) {
			// send a request to get the user id

			Request request = Request.newMeRequest(session,
					new Request.GraphUserCallback() {
						@Override
						public void onCompleted(GraphUser user,
								Response response) {
							// If the response is successful
							if (session == Session.getActiveSession()) {
								if (user != null) {
									id = user.getId();// user id
									userName = user.getName();
								}
							}
						}
					});

			request.executeAsync();
			// return id;
		}

		// return null;
	}

	/**
	 * @author osama </br> this method is used to post a check-in
	 * 
	 * @param placeId
	 */
	private void post(String placeId) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		String message = sharedPreferences.getString("message",
				"I was there :)");

		if (getIntent().getAction() != null
				&& getIntent().getAction().equals(
						NfcAdapter.ACTION_NDEF_DISCOVERED)) {
			// tag friend in the post.

			Bundle params = new Bundle();
			params.putString("message", message);
			params.putString("place", placeId);
			params.putString("tags", friendId);

			new Request(Session.getActiveSession(), "/me/feed", params,
					HttpMethod.POST, new Request.Callback() {

						@Override
						public void onCompleted(Response response) {
							Log.d("osama", "posting with a friend");
						}
					}).executeAsync();
		} else {
			// post without tags.

			Bundle params = new Bundle();
			params.putString("message", message);
			params.putString("place", placeId);

			new Request(Session.getActiveSession(), "/me/feed", params,
					HttpMethod.POST, new Request.Callback() {

						@Override
						public void onCompleted(Response response) {
							Log.d("osama", "posting alone");
							Log.d("osama",
									"the post response was: "
											+ response.toString());
						}
					}).executeAsync();
		}
		finish();
	}

	private void postNewPlace(String placeName) throws JSONException {
		if (getIntent().getAction() != null
				&& getIntent().getAction().equals(
						NfcAdapter.ACTION_NDEF_DISCOVERED)) {

			RequestBatch requestBatch = new RequestBatch();

			JSONObject place = new JSONObject();
			place.put("image", "http://shake-in.parseapp.com/shakeIn.png");
			place.put("title", placeName);
			place.put("url", null);
			place.put("description", userName + " was at " + placeName
					+ " using shake-in.");
			// place.put("scrape", "true");
			JSONObject data = new JSONObject();
			// JSONObject book = new JSONObject();
			JSONObject location = new JSONObject();
			location.put("latitude",
					String.valueOf(this.location.getLatitude()));
			location.put("longitude",
					String.valueOf(this.location.getLongitude()));
			data.put("location", location);
			// book.put("isbn", "0-553-57340-3");
			// data.put("book", book);
			place.put("data", data);

			Bundle objectParams = new Bundle();
			objectParams.putString("object", place.toString());

			Request objectRequest = new Request(Session.getActiveSession(),
					"me/objects/place", objectParams, HttpMethod.POST,
					new Request.Callback() {

						@Override
						public void onCompleted(Response response) {
							Log.d("osama",
									"object creation ->" + response.toString());

						}
					});
			//
			objectRequest.setBatchEntryName("objectCreate");
			requestBatch.add(objectRequest);

			// String message = "post from objectAPI";

			// ========================================================================
			SharedPreferences sharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());
			String message = sharedPreferences.getString("message",
					"I was there :)");

			Bundle params = new Bundle();
			params.putString("generic_place", "{result=objectCreate:$.id}");
			params.putString("fb:explicitly_shared", "true");

			params.putString("message", message);
			params.putString("tags", friendId);

			Request postRequest = new Request(Session.getActiveSession(),
					"me/places.saves", params, HttpMethod.POST,
					new Request.Callback() {

						@Override
						public void onCompleted(Response response) {
							Log.d("osama",
									"post response --> " + response.toString());

						}
					});

			requestBatch.add(postRequest);
			// ========================================================================

			requestBatch.executeAsync();
		} else {
			RequestBatch requestBatch = new RequestBatch();

			JSONObject place = new JSONObject();
			place.put("image", "http://shake-in.parseapp.com/shakeIn.png");
			place.put("title", placeName);
			place.put("url", null);
			place.put("description", userName + " was at " + placeName
					+ " using shake-in.");
			// place.put("scrape", "true");
			JSONObject data = new JSONObject();
			// JSONObject book = new JSONObject();
			JSONObject location = new JSONObject();
			location.put("latitude",
					String.valueOf(this.location.getLatitude()));
			location.put("longitude",
					String.valueOf(this.location.getLongitude()));
			data.put("location", location);
			// book.put("isbn", "0-553-57340-3");
			// data.put("book", book);
			place.put("data", data);

			Bundle objectParams = new Bundle();
			objectParams.putString("object", place.toString());

			Request objectRequest = new Request(Session.getActiveSession(),
					"me/objects/place", objectParams, HttpMethod.POST,
					new Request.Callback() {

						@Override
						public void onCompleted(Response response) {
							Log.d("osama",
									"object creation ->" + response.toString());

						}
					});
			//
			objectRequest.setBatchEntryName("objectCreate");
			requestBatch.add(objectRequest);

			// String message = "post from objectAPI";

			// ========================================================================
			SharedPreferences sharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());
			String message = sharedPreferences.getString("message",
					"I was there :)");

			Bundle params = new Bundle();
			params.putString("generic_place", "{result=objectCreate:$.id}");
			params.putString("fb:explicitly_shared", "true");

			params.putString("message", message);

			Request postRequest = new Request(Session.getActiveSession(),
					"me/places.saves", params, HttpMethod.POST,
					new Request.Callback() {

						@Override
						public void onCompleted(Response response) {
							Log.d("osama",
									"post response --> " + response.toString());

						}
					});

			requestBatch.add(postRequest);
			// ========================================================================

			requestBatch.executeAsync();
		}
		finish();
	}

	private void postOldPlace(String placeId) {
		if (getIntent().getAction() != null
				&& getIntent().getAction().equals(
						NfcAdapter.ACTION_NDEF_DISCOVERED)) {

			SharedPreferences sharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());
			String message = sharedPreferences.getString("message",
					"I was there :)");

			Bundle params = new Bundle();
			params.putString("generic_place", placeId);
			params.putString("fb:explicitly_shared", "true");
			params.putString("message", message);
			params.putString("tags", friendId);

			Request postRequest = new Request(Session.getActiveSession(),
					"me/places.saves", params, HttpMethod.POST,
					new Request.Callback() {

						@Override
						public void onCompleted(Response response) {
							Log.d("osama",
									"post response --> " + response.toString());

						}
					});
			postRequest.executeAsync();
		} else {
			SharedPreferences sharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());
			String message = sharedPreferences.getString("message",
					"I was there :)");

			Bundle params = new Bundle();
			params.putString("generic_place", placeId);
			params.putString("fb:explicitly_shared", "true");
			params.putString("message", message);

			Request postRequest = new Request(Session.getActiveSession(),
					"me/places.saves", params, HttpMethod.POST,
					new Request.Callback() {

						@Override
						public void onCompleted(Response response) {
							Log.d("osama",
									"post response --> " + response.toString());

						}
					});
			postRequest.executeAsync();
		}
		finish();
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

	public void addPlaceOnClick(View view) {
		// cancel the counter
		// if (counter != null) {
		// counter.cancel();
		// }

		// Display a dialog to get the place name from user.

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("New place");
		alert.setMessage("Where are you?");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						String placeName = input.getText().toString();

						// show the shared-Dialog
						// displayShareDialogIII(placeName);
						try {
							postNewPlace(placeName);
						} catch (JSONException e) {
							Toast.makeText(getApplicationContext(),
									"please try again", Toast.LENGTH_LONG)
									.show();
						}
					}

				});
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();

	}

	public void myPlacesOnClick(View view) {
		myPlaces = !myPlaces;
		// XXX: when you use a custom action don't forget to change what's here
		if (myPlaces) {
			new Request(Session.getActiveSession(), "me/objects/place", null,
					null, new Request.Callback() {

						@Override
						public void onCompleted(Response response) {

							try {
								ArrayList<String> places = new ArrayList<String>();

								myPlacesData = response.getGraphObject()
										.getInnerJSONObject()
										.getJSONArray("data");

								for (int i = 0; i < myPlacesData.length(); i++) {
									places.add(myPlacesData.getJSONObject(i)
											.optString("title"));

								}

								ArrayAdapter<String> adapter = new ArrayAdapter<>(
										getBaseContext(), R.layout.simple_list,
										places);

								setListAdapter(adapter);

							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}).executeAsync();
		} else {
			populateListView();
		}
	}

}
