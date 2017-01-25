package test;

import java.security.MessageDigest;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.OpenGraphAction;
import com.facebook.model.OpenGraphObject;
import com.facebook.widget.FacebookDialog;
import com.osama.shake_in.R;
import com.osama.shake_in.R.layout;

/**
 * 
 * @author osama
 *
 *this class is used to test the ShareDialog using facebookSDK
 */

public class Test extends Activity {
	private UiLifecycleHelper uiHelper;
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
		setContentView(R.layout.test);
		uiHelper = new UiLifecycleHelper(this, callBack);

		displayShareDialogIII();
	}

	protected void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (state.isOpened()) {
			Toast.makeText(getBaseContext(), "logged in", Toast.LENGTH_SHORT)
					.show();
		} else if (state.isClosed()) {
			Toast.makeText(getBaseContext(), "loged out", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	protected void onResume() {
		super.onRestart();
		uiHelper.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
		uiHelper.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// handle the share dialog result here
		uiHelper.onActivityResult(requestCode, resultCode, data,
				new FacebookDialog.Callback() {
					// XXX:you can handle the shareDialog response here
					@Override
					public void onError(FacebookDialog.PendingCall pendingCall,
							Exception error, Bundle data) {
						Log.e("Activity",
								String.format("Error: %s", error.toString()));
					}

					@Override
					public void onComplete(
							FacebookDialog.PendingCall pendingCall, Bundle data) {
						Toast.makeText(
								getBaseContext(),
								String.valueOf(FacebookDialog
										.getNativeDialogDidComplete(data)),
								Toast.LENGTH_SHORT).show();

						Log.i("Activity", "Success!");
					}
				});
	}

	protected void displayShareDialogII() {
		// you need to close the session first.

		// only show the share dialog if the user has the app installed
		if (FacebookDialog.canPresentOpenGraphActionDialog(
				getApplicationContext(),
				FacebookDialog.OpenGraphActionDialogFeature.OG_ACTION_DIALOG)) {

			if (Session.getActiveSession().isOpened()) {
				Session.getActiveSession().closeAndClearTokenInformation();
			}

			OpenGraphAction action = GraphObject.Factory
					.create(OpenGraphAction.class);
			action.setProperty("some where",
					"http://shake-in.parseapp.com/home.html");

			FacebookDialog shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(
					this, action, "shake-in:visit", "some where").build();
			uiHelper.trackPendingDialogCall(shareDialog.present());
		}
	}

	protected void displayShareDialogIII() {
		// this one uses the user owned objects and the user is capable of
		// creating objects

		// only show the share dialog if the user has the app installed
		if (FacebookDialog.canPresentOpenGraphActionDialog(
				getApplicationContext(),
				FacebookDialog.OpenGraphActionDialogFeature.OG_ACTION_DIALOG)) {

			// you need to close the session first.
			if (Session.getActiveSession().isOpened()) {
				Session.getActiveSession().closeAndClearTokenInformation();
			}

			OpenGraphObject some_where = OpenGraphObject.Factory
					.createForPost("shake-in:some_where");
			some_where.setProperty("title", "homeII");
			some_where.setProperty("image",
					"http://shake-in.parseapp.com/home.png");
			some_where.setProperty("url", null);
			some_where.setProperty("description", "finally it runs");

			OpenGraphAction action = GraphObject.Factory
					.create(OpenGraphAction.class);
			action.setProperty("some_where", some_where);

			FacebookDialog shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(
					this, action, "shake-in:visit", "some_where").build();
			uiHelper.trackPendingDialogCall(shareDialog.present());
		}
	}

	@SuppressWarnings("unused")
	private void createPageI() {

		Bundle params = new Bundle();
		params.putString("type", "place");
		params.putString("fbsdk:create_object", "YES");
		params.putString("url", "http://shake-in.parseapp.com/place.html");
		params.putString("title", "Barcelona");
		params.putString("image", "http://shake-in.parseapp.com/location.png");
		params.putString("place:location:latitude", "41.40977583");
		params.putString("place:location:longitude", "2.19726562");
		/* make the API call */
		new Request(Session.getActiveSession(),
				"/graph.facebook.com/me/shake-in:visit", params,
				HttpMethod.POST, new Request.Callback() {
					public void onCompleted(Response response) {
						Log.d("osama", response.toString());

						Toast.makeText(getBaseContext(), "check logcat!!",
								Toast.LENGTH_SHORT).show();
					}
				}).executeAsync();
	}

	void getHasKey() { // Get Has Key
		try {
			PackageInfo info = getPackageManager().getPackageInfo(
					"com.osama.shake_in", PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.e("KeyHash:",
						Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
