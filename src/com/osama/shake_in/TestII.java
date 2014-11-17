package com.osama.shake_in;

import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.OpenGraphAction;
import com.facebook.model.OpenGraphObject;

/**
 * 
 * @author osama: </br> this class uses <b>objectAPI</b> to create openGrah
 *         objects and also publishes programmatically
 */
public class TestII extends Activity {
	private UiLifecycleHelper uihHelper;
	private TextView text;
	private Session.StatusCallback callBack = new Session.StatusCallback() {

		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.testii);

		text = (TextView) findViewById(R.id.testText);

		uihHelper = new UiLifecycleHelper(this, callBack);
		uihHelper.onCreate(savedInstanceState);

		try {
//			testVisit();
			 testWithJSON();
			// post();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		uihHelper.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
		uihHelper.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		uihHelper.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uihHelper.onSaveInstanceState(outState);
	}

	protected void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (state.isOpened()) {
			Log.d("osama", "opened session -> TestII");
		} else if (state.isClosed()) {
			Log.e("osama", "closed session -> TestII");
		}
	}

	// --------------------------------------------------------------------

	private void testWithJSON() throws JSONException {
		RequestBatch requestBatch = new RequestBatch();

		JSONObject place = new JSONObject();
		// TODO : try to do the same but with place
		place.put("image",
				"https://furious-mist-4378.herokuapp.com/books/a_game_of_thrones.png");
		place.put("title", "O,O");
		place.put("url", null);
		place.put(
				"description",
				"In the frozen wastes to the north of Winterfell, sinister and supernatural forces are mustering.");
		// place.put("scrape", "true");
		JSONObject data = new JSONObject();
		// JSONObject book = new JSONObject();
		JSONObject location = new JSONObject();
		location.put("latitude", "65.9667");
		location.put("longitude", "-18.5333");
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

						text.setText("object creation ->" + response.toString());
					}
				});
		//
		objectRequest.setBatchEntryName("objectCreate");
		requestBatch.add(objectRequest);

		// String message = "post from objectAPI";

		// ========================================================================
		// JSONObject post = new JSONObject();
		// // post.put("message", message);
		// post.put("book", "{result=objectCreate:$.id}");
		// // post.put("object_attachment", "716409398450073");

		Bundle params = new Bundle();
		params.putString("generic_place", "{result=objectCreate:$.id}");
		params.putString("fb:explicitly_shared", "true");

		// params.putString("message", message);
		// params.putString("book", "{result=objectCreate:$.id}");
		// params.putString("object", post.toString());

		Request postRequest = new Request(Session.getActiveSession(),
				"me/places.saves", params, HttpMethod.POST,
				new Request.Callback() {

					@Override
					public void onCompleted(Response response) {
						Log.d("osama",
								"post response --> " + response.toString());

						text.setText(text.getText().toString() + " \n \n "
								+ response.toString());
					}
				});

		requestBatch.add(postRequest);
		// ========================================================================

		requestBatch.executeAsync();
	}

	private void testVisit() throws JSONException {
		// TODO: share using the visit action

		RequestBatch requestBatch = new RequestBatch();

		JSONObject place = new JSONObject();
		// TODO : try to do the same but with place
		place.put("image",
				"https://furious-mist-4378.herokuapp.com/books/a_game_of_thrones.png");
		place.put("title", "o,O");
		place.put("url", null);
		place.put(
				"description",
				"In the frozen wastes to the north of Winterfell, sinister and supernatural forces are mustering.");
		// place.put("scrape", "true");
		JSONObject data = new JSONObject();
		// JSONObject book = new JSONObject();
		JSONObject location = new JSONObject();
		location.put("latitude", "65.9667");
		location.put("longitude", "-18.5333");
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

						text.setText("object creation ->" + response.toString());
					}
				});
		//
		objectRequest.setBatchEntryName("objectCreate");
		requestBatch.add(objectRequest);

		// String message = "post from objectAPI";

		// ========================================================================
		// JSONObject post = new JSONObject();
		// // post.put("message", message);
		// post.put("book", "{result=objectCreate:$.id}");
		// // post.put("object_attachment", "716409398450073");

		Bundle params = new Bundle();
		params.putString("place", "{result=objectCreate:$.id}");
		params.putString("fb:explicitly_shared", "true");

		// params.putString("message", message);
		// params.putString("book", "{result=objectCreate:$.id}");
		// params.putString("object", post.toString());

		Request postRequest = new Request(Session.getActiveSession(),
				"graph.facebook.com/me/shake-in:visit", params,
				HttpMethod.POST, new Request.Callback() {

					@Override
					public void onCompleted(Response response) {
						Log.d("osama",
								"post response --> " + response.toString());

						text.setText(text.getText().toString() + " \n \n "
								+ response.toString());
					}
				});

		requestBatch.add(postRequest);
		// ========================================================================

		requestBatch.executeAsync();
	}

	private void post() throws JSONException {
		RequestBatch requestBatch = new RequestBatch();

		// Request: Object request
		// --------------------------------------------

		// Set up the OpenGraphObject representing the book.
		OpenGraphObject book = OpenGraphObject.Factory
				.createForPost("books.book");

		String imageUrl = "https://furious-mist-4378.herokuapp.com/books/a_game_of_thrones.png";
		book.setImageUrls(Arrays.asList(imageUrl));
		book.setTitle("A Game of Thrones");
		book.setUrl("https://YOUR_APP_DOMAIN/books/a_game_of_thrones/");
		book.setDescription("In the frozen wastes to the north of Winterfell, sinister and supernatural forces are mustering.");
		// books.book-specific properties go under "data"
		book.getData().setProperty("isbn", "0-553-57340-3");

		// Set up the object request callback
		Request.Callback objectCallback = new Request.Callback() {

			@Override
			public void onCompleted(Response response) {
				// Log any response error
				FacebookRequestError error = response.getError();
				if (error != null) {
					Log.i("osama", error.getErrorMessage());
				}
			}
		};

		// Create the request for object creation
		Request objectRequest = Request.newPostOpenGraphObjectRequest(
				Session.getActiveSession(), book, objectCallback);

		// Set the batch name so you can refer to the result
		// in the follow-on publish action request
		objectRequest.setBatchEntryName("objectCreate");

		// Add the request to the batch
		requestBatch.add(objectRequest);

		// =================================================================================

		// Request: Publish action request
		// --------------------------------------------
		OpenGraphAction readAction = OpenGraphAction.Factory
				.createForPost("books.reads");
		// Refer to the "id" in the result from the previous batch request
		readAction.setProperty("book", "{result=objectCreate:$.id}");

		// Set up the action request callback
		Request.Callback actionCallback = new Request.Callback() {

			@Override
			public void onCompleted(Response response) {
				FacebookRequestError error = response.getError();
				if (error != null) {
					Toast.makeText(getApplicationContext(),
							error.getErrorMessage(), Toast.LENGTH_LONG).show();
				} else {
					String actionId = null;
					try {
						JSONObject graphResponse = response.getGraphObject()
								.getInnerJSONObject();
						actionId = graphResponse.getString("id");
					} catch (JSONException e) {
						Log.i("osama", "JSON error " + e.getMessage());
					}
					Toast.makeText(getApplicationContext(), actionId,
							Toast.LENGTH_LONG).show();
				}
			}
		};

		// Create the publish action request
		Request actionRequest = Request.newPostOpenGraphActionRequest(
				Session.getActiveSession(), readAction, actionCallback);

		// Add the request to the batch
		requestBatch.add(actionRequest);
	}
}