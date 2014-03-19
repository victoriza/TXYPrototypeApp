package es.moodbox.txy.app.task;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.moodbox.txy.app.activity.TXYMainActivity;
import es.moodbox.txy.app.domain.Meetup;

/**
 * Created by victor on 18/03/14.
 */
public class PostAsyncTask extends AsyncTask<List<Meetup>, Integer, Integer> {

	private final static String POST_URL = "http://txyplatform.victoriza.cloudbees.net/meetup/addMeetup";

	@Override
	protected void onPostExecute(Integer result) {
		Log.d("@@ Meetups posted, sending finished task ", "meetups: "+result);
	}

	@Override
	protected Integer doInBackground(List<Meetup>... meetups) {

		for (Meetup meetup : meetups[0]) {
			sendMeetupHttpPost(meetup);
		}
		return meetups.length;
	}

	private static void sendMeetupHttpPost(Meetup meetup) {
		HttpClient httpclient = new DefaultHttpClient();

		Log.d("@@ HttpClient ", " post start");

		// specify the URL you want to post to
		HttpPost httppost = new HttpPost(POST_URL);
		try {
			// create a list to store HTTP variables and their values
			List postParams = new ArrayList();

			// add an HTTP variable and value pair
			postParams.add(new BasicNameValuePair("userA", meetup.getUserA().getUserName()));
			postParams.add(new BasicNameValuePair("userB", meetup.getUserB().getUserName()));
			postParams.add(new BasicNameValuePair("relationshipType", meetup.getRelationship()));
			postParams.add(new BasicNameValuePair("elapsedTime",
					String.valueOf(meetup.getMeetingEnd())));

			httppost.setEntity(new UrlEncodedFormEntity(postParams));

			// send the variable and value, in other words post, to the URL
			HttpResponse response = httpclient.execute(httppost);
			Log.d("@@ HttpClient ended with response", "status: "
					+ response.getStatusLine().getStatusCode());

		} catch (ClientProtocolException e) {
			// process execption
		} catch (IOException e) {
			// process execption
		}
	}
}