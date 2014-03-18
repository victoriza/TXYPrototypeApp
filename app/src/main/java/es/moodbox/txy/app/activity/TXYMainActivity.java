package es.moodbox.txy.app.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import es.moodbox.txy.app.R;
import es.moodbox.txy.app.domain.Meetup;
import es.moodbox.txy.app.services.MeetupFinderService;
import es.moodbox.txy.app.task.PostAsyncTask;

public class TXYMainActivity extends ActionBarActivity {

	public static final String PREFS_NAME = TXYMainActivity.class.getName() + "File";
	public static final String ES_MOODBOX_TXY_USER_FOUND = "es.moodbox.txy.UserFound";
	private final static int REQUEST_ENABLE_BT = 1;
	private static final String ACTUAL_ACTIVITY = "ACTUAL_ACTIVITY";
	public static final String DEF_VALUE = "someOne";
	private BluetoothAdapter mBluetoothAdapter;
	private SharedPreferences settings = null;
	private static String meetupRelationship;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_txy_main);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		settings = getSharedPreferences(PREFS_NAME, 0);
		meetupRelationship = settings.getString(ACTUAL_ACTIVITY, DEF_VALUE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadCastServiceReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();

		//TODO: check what to do
		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
			Log.e(this.getLocalClassName(), "@@ --- BLUETOOTH NOT SUPPORTED :( ");

		} else if (!mBluetoothAdapter.isEnabled()) {
			Log.d(this.getLocalClassName(), "@@ --- BLUETOOTH IS NOT ENABLED, we ask the user to turn it on");
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		Log.d(this.getLocalClassName(), "@@ --- Starting the service!");
		// the filter to receive the changes
		IntentFilter filter = new IntentFilter(ES_MOODBOX_TXY_USER_FOUND);
		// Don't forget to unregister during onDestroy
		registerReceiver(broadCastServiceReceiver, filter);

		// use this to start and trigger a service
		Intent intentService = new Intent(getApplicationContext(), MeetupFinderService.class);
		startService(intentService);

		//add the onclick listener
		Button btn = (Button) findViewById(R.id.sendDataButton);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//what where is all about
				if (MeetupFinderService.getMeetups() != null && !MeetupFinderService.getMeetups().isEmpty()) {
					showWhatWhereAreYouDoing();
				}
			}
		});
	}

	private void showWhatWhereAreYouDoing() {
		// get prompts.xml view
		LayoutInflater li = LayoutInflater.from(this);
		View promptsView = li.inflate(R.layout.prompt, null);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		// set prompts.xml to alert dialog builder
		alertDialogBuilder.setView(promptsView);

		final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

		final TextView result = (TextView) findViewById(R.id.relationshipTextView);
		// set dialog message
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// get user input and set it to result
								// edit text
								result.setText(userInput.getText());
								//post it
								Toast.makeText(result.getContext(),
										"Sending data to our sloooow servers)",
										Toast.LENGTH_LONG).show();
								//and send the data
								addRelationshipAndSend(userInput.getText().toString());
							}
						}
				)
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						}
				);
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show it
		alertDialog.show();
	}

	private void addRelationshipAndSend(String meetupRelationship) {
		List<Meetup> meetupCopy = new ArrayList<Meetup>(MeetupFinderService.getMeetups());
		for (Meetup m : meetupCopy) {
			m.setRelationship(meetupRelationship);
		}
		new PostAsyncTask().execute(meetupCopy);
	}


	private BroadcastReceiver broadCastServiceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			Log.d("@@ BR Received from service has user: " + extras.containsKey(MeetupFinderService.USER_KEY), "");
			//the user found
			String brUserNameB = extras.getString(MeetupFinderService.USER_KEY);
			Log.d("@@ Current meetups", " #meetups: " + MeetupFinderService.getMeetups());

			//show the info
			addToScreen(MeetupFinderService.getMeetups());
			Toast.makeText(context, "User found: " + brUserNameB == null ? "update" : brUserNameB, Toast.LENGTH_LONG).show();
		}
	};

	/**
	 * We calculate the diference as
	 *
	 * @param oldMeetups
	 * @param currentMeetups
	 * @return
	 */
	private List<Meetup> compareMeetups(List<Meetup> oldMeetups, List<Meetup> currentMeetups) {
		List<Meetup> diference = null;

		if (oldMeetups != null && !oldMeetups.isEmpty()) {
			diference = new ArrayList<Meetup>(oldMeetups);
			boolean isSomeRemoved = diference.removeAll(currentMeetups);
			Log.d("@@ Current difference", " #isDifference: " + isSomeRemoved);
			Log.d("@@ Calculating", " #oldMeetups: " + oldMeetups);
			Log.d("@@ Calculating", " #currentMeetups: " + currentMeetups);
		}
		return diference;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.txymain, menu);
		return true;
	}


	private void addToScreen(List<Meetup> users) {
		if (users == null || users.isEmpty()) {
			return;
		}
		TextView usersTextView = (TextView) findViewById(R.id.usersTextView);
		StringBuffer sb = new StringBuffer("");
		for (Meetup meetup : users) {
			sb.append(meetup.toString());
			sb.append("\n");
		}
		usersTextView.setText(sb.toString());
	}
}
