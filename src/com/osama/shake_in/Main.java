package com.osama.shake_in;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.widget.LoginButton;

//	TODO: check if Google play services and facebook are installed on this device
//  TODO: add <uses-feature > and the final key hash using the app-key element for publishing purposes
//  XXX: save the user's profile picture locally.  --> v.2
//	TODO: use fragment instead of activities.

//	FIXME: openSession vs  reverseSessionState!!!

public class Main extends Activity {
	private UiLifecycleHelper uiHelper;
	private ImageView profilePic;
	private ImageView login;
	private LoginButton authButton;
	private boolean hasImage = false;
	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
	private boolean pendingPublishReauthorization = false;
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
		setContentView(R.layout.main);
		uiHelper = new UiLifecycleHelper(this, callBack);
		uiHelper.onCreate(savedInstanceState);


		profilePic = (ImageView) findViewById(R.id.profilePic);
		login = (ImageView) findViewById(R.id.login);
		profilePic.setVisibility(View.GONE);
		login.setImageBitmap(getCircularBitmapII(BitmapFactory.decodeResource(
				getResources(), R.drawable.login)));
		login.setVisibility(View.VISIBLE);

		ImageButton btnPost = (ImageButton) findViewById(R.id.btnPost);
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.wiggle);
		btnPost.startAnimation(anim);

		// XXX: edited check if it's valid
		// FIXME: logs in automatically no matter what!!
		if (autoLogin() && Session.getActiveSession().getState().isClosed()) {
			// openSession();
			reverseSessionState();
			Log.d("osama", "auto login = true");
		}

		// getHasKey();

		if (serviceEnabled()) {
			startService(new Intent(Main.this, Listener.class));
			// the service is in a separate private process
		}

		if (savedInstanceState != null) {
			pendingPublishReauthorization = savedInstanceState.getBoolean(
					PENDING_PUBLISH_KEY, false);
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
		if (serviceEnabled()) {
			startService(new Intent(Main.this, Listener.class));
			// the service is in a separate private process
		}
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			onSessionStateChange(session, session.getState(), null);
		}

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
		if (serviceEnabled()) {
			startService(new Intent(Main.this, Listener.class));
			// the service is in a separate private process
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(PENDING_PUBLISH_KEY, pendingPublishReauthorization);
		uiHelper.onSaveInstanceState(outState);
	}

	protected void onSessionStateChange(Session session, SessionState state,
			Exception exception) {

		if (state.isOpened()) {
			Log.d("osama", "logged in");

			login.setVisibility(View.GONE);
			setProfilePic();
			if (pendingPublishReauthorization
					&& state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
				pendingPublishReauthorization = false;
				openSession();
			}
		} else if (state.isClosed()) {
			Log.d("osama", "logged out");

			profilePic.setVisibility(View.GONE);
			login.setVisibility(View.VISIBLE);
		}
	}

	private boolean autoLogin() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		return sharedPref.getBoolean("auto_login", true);
	}

	private boolean serviceEnabled() {
		// This method should say if the user prefer to activate the service or
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		return sharedPref.getBoolean("gesture", true);
	}

	private void openSession() {
		Session session = Session.getActiveSession();

		if (session != null && session.isClosed()) {
			// check for permissions and set the permissions if missing
			List<String> permissions = session.getPermissions();

			final List<String> PERMISSIONS = Arrays.asList("publish_actions");

			if (!isSubsetOf(PERMISSIONS, permissions)) {
				pendingPublishReauthorization = true;
				Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
						this, PERMISSIONS);
				session.requestNewPublishPermissions(newPermissionsRequest);
			}
		}

	}

	private void reverseSessionState() {
		Session session = Session.getActiveSession();

		if (session != null) {
			// check for permissions and set the permissions if missing
			List<String> permissions = session.getPermissions();

			final List<String> PERMISSIONS = Arrays.asList("publish_actions");

			if (!isSubsetOf(PERMISSIONS, permissions)) {
				// authButton = (LoginButton) findViewById(R.id.authButton);
				authButton = new LoginButton(this);
				authButton.setPublishPermissions(Arrays
						.asList("publish_actions"));
				authButton.performClick();
			} else if (session.isClosed()) {
				Session.openActiveSession(this, true, callBack);
			} else if (session.isOpened()) {
				session.close();
			}
		}
	}

	private boolean isSubsetOf(Collection<String> subset,
			Collection<String> superset) {
		for (String string : subset) {
			if (!superset.contains(string)) {
				return false;
			}
		}
		return true;
	}

	private void setProfilePic() {
		profilePic.setVisibility(View.VISIBLE);
		if (!hasImage) {
			Bundle params = new Bundle();
			params.putString("fields", "picture");

			new Request(Session.getActiveSession(), "me", params,
					HttpMethod.GET, new Request.Callback() {

						@Override
						public void onCompleted(Response response) {
							GraphObject graphObject = response.getGraphObject();
							if (graphObject != null) {
								try {
									JSONObject JObject = graphObject
											.getInnerJSONObject();
									JSONObject obj = JObject.getJSONObject(
											"picture").getJSONObject("data");
									final String StringUrl = obj
											.getString("url");
									new Thread(new Runnable() {

										@Override
										public void run() {
											try {
												final Bitmap bitmap = BitmapFactory
														.decodeStream(new java.net.URL(
																StringUrl)
																.openStream());

												Log.d("osama",
														"Image request sent");

												/*
												 * try { URL url = new
												 * URL(StringUrl);
												 * HttpURLConnection con =
												 * (HttpURLConnection)
												 * url.openConnection();
												 * InputStream is =
												 * con.getInputStream(); final
												 * Bitmap bitmap =
												 * BitmapFactory.
												 * decodeStream(is); } catch
												 * (Exception e) { }
												 */

												runOnUiThread(new Runnable() {

													@Override
													public void run() {
														if (bitmap != null) {
															profilePic
																	.setImageBitmap(getCircularBitmapII(bitmap));
															hasImage = true;
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
		} else {
			profilePic.setVisibility(View.VISIBLE);
		}
	}

	public static Bitmap getCircularBitmapII(Bitmap bitmap) {
		Bitmap output;

		if (bitmap.getWidth() > bitmap.getHeight()) {
			output = Bitmap.createBitmap(bitmap.getHeight(),
					bitmap.getHeight(), Config.ARGB_8888);
		} else {
			output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(),
					Config.ARGB_8888);
		}

		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

		float r = 0;

		if (bitmap.getWidth() > bitmap.getHeight()) {
			r = bitmap.getHeight() / 2;
		} else {
			r = bitmap.getWidth() / 2;
		}

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawCircle(r, r, r, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	public void settingsOnClick(View view) {
		startActivity(new Intent(this, Settings.class));
	}

	public void onClickPost(View view) {
		Intent intent = new Intent(this, Post.class);
		startActivity(intent);
	}

	public void btnProfilePicOnClick(View view) {
		// if (Session.getActiveSession().isOpened()) {
		// Session.getActiveSession().close();
		// } else {
		// Session.openActiveSession(this, true, callBack);
		// }

		reverseSessionState();
	}

	// public void testObjectAPIOnClick(View view) {
	// startActivity(new Intent(this, TestII.class));
	// }

	// public void testShareDialogOnClick(View view) {
	// Intent intent = new Intent(this, Test.class);
	// startActivity(intent);
	// }

	// void getHasKey() { // Get Has Key
	// try {
	// PackageInfo info = getPackageManager().getPackageInfo(
	// "com.osama.shake_in", PackageManager.GET_SIGNATURES);
	// for (Signature signature : info.signatures) {
	// MessageDigest md = MessageDigest.getInstance("SHA");
	// md.update(signature.toByteArray());
	// Log.e("KeyHash:",
	// Base64.encodeToString(md.digest(), Base64.DEFAULT));
	// }
	// } catch (NameNotFoundException e) {
	// e.printStackTrace();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	/*
	 * @Override public void onBackPressed() { int count =
	 * getFragmentManager().getBackStackEntryCount();
	 * 
	 * if (count == 0) { super.onBackPressed(); // additional code
	 * FragmentTransaction fragmentTransaction = getFragmentManager()
	 * .beginTransaction(); fragmentTransaction.add(android.R.id.content,
	 * mainFragment) .commit(); } else { getFragmentManager().popBackStack(); //
	 * mainFragment.onCreate(null); //
	 * mainFragment.onCreateView(getLayoutInflater(), null, null);
	 * 
	 * // getFragmentManager().beginTransaction().remove(settingsFragment) //
	 * .commit(); // getFragmentManager().popBackStack(); //
	 * getFragmentManager().beginTransaction().add(android.R.id.content, //
	 * mainFragment); } }
	 */
}
