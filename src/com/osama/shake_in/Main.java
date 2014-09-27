package com.osama.shake_in;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;

//	TODO: check if Google play services and facebook is installed on this device
//  TODO: add <uses-feature > and the final key hash using the app-key element for publishing purposes
//  TODO: save the user's profile picture locally.
public class Main extends Activity {
	private MainFragment mainFragment;
	private ImageButton btnProfilePic;
	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callBack = new Session.StatusCallback() {

		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChanged(session, state, exception);

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		uiHelper = new UiLifecycleHelper(this, callBack);
		uiHelper.onCreate(savedInstanceState);

		ActionBar actionbar = getActionBar();
		actionbar.hide();
		mainFragment = new MainFragment();
		FragmentTransaction fragmentTransaction = getFragmentManager()
				.beginTransaction();
		fragmentTransaction.add(android.R.id.content, mainFragment).commit();
		// TODO: use Session instead of authButton.
		btnProfilePic = (ImageButton) findViewById(R.id.btnProfilePic);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		if (serviceEnabled()) {
			startService(new Intent(Main.this, Listener.class));
			// the service is in a separate private process
		}

		setProfilePic();
	}

	@Override
	public void onBackPressed() {
		int count = getFragmentManager().getBackStackEntryCount();

		if (count == 0) {
			super.onBackPressed();
			// additional code
			FragmentTransaction fragmentTransaction = getFragmentManager()
					.beginTransaction();
			fragmentTransaction.add(android.R.id.content, mainFragment)
					.commit();
		} else {
			getFragmentManager().popBackStack();
			// mainFragment.onCreate(null);
			// mainFragment.onCreateView(getLayoutInflater(), null, null);

			// getFragmentManager().beginTransaction().remove(settingsFragment)
			// .commit();
			// getFragmentManager().popBackStack();
			// getFragmentManager().beginTransaction().add(android.R.id.content,
			// mainFragment);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
		uiHelper.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	private boolean serviceEnabled() {
		// This method should say if the user prefer to activate the service or
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		return sharedPref.getBoolean("gesture", true);
	}

	public void settingsOnClick(View view) {
		startActivity(new Intent(this, Settings.class));
	}

	public void btnProfilePicOnClick(View view) {
		if (Session.getActiveSession().isOpened()) {
			Session.getActiveSession().close();
			Toast.makeText(this, "logged out :(", Toast.LENGTH_SHORT).show();
		} else {
			Session.openActiveSession(this, true, callBack);
			Toast.makeText(this, "logged In :D", Toast.LENGTH_SHORT).show();
		}
	}

	public void onClickPost(View view) {
		Intent intent = new Intent(this, Post.class);
		startActivity(intent);
	}

	protected void onSessionStateChanged(Session session, SessionState state,
			Exception exception) {
		// TODO: make something due to the change of the session state.
		if (session.isOpened()) {
			Toast.makeText(this, "logged in", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "logged out", Toast.LENGTH_SHORT).show();
		}
	}

	private void setProfilePic() {
		Bundle params = new Bundle();
		params.putString("fields", "picture");

		new Request(Session.getActiveSession(), "me", params, HttpMethod.GET,
				new Request.Callback() {

					@Override
					public void onCompleted(Response response) {
						GraphObject graphObject = response.getGraphObject();
						if (graphObject != null) {
							try {
								JSONObject JObject = graphObject
										.getInnerJSONObject();
								JSONObject obj = JObject.getJSONObject(
										"picture").getJSONObject("data");
								final String StringUrl = obj.getString("url");
								new Thread(new Runnable() {

									@Override
									public void run() {
										try {
											final Bitmap bitmap = BitmapFactory
													.decodeStream(new java.net.URL(
															StringUrl)
															.openStream());

											Log.d("osama", "Image request sent");

											/*
											 * try { URL url = new
											 * URL(StringUrl); HttpURLConnection
											 * con = (HttpURLConnection)
											 * url.openConnection(); InputStream
											 * is = con.getInputStream(); final
											 * Bitmap bitmap =
											 * BitmapFactory.decodeStream(is); }
											 * catch (Exception e) { }
											 */

											runOnUiThread(new Runnable() {

												@Override
												public void run() {
													if (bitmap != null) {
														btnProfilePic
																.setImageBitmap(bitmap);
													} else {
														Log.e("osama",
																"the image was null");
													}

												}
											});

										} catch (IOException e) {
										}
									}
								}).start();
							} catch (JSONException ex) {

							}

						}
					}
				}).executeAsync();
	}
}
