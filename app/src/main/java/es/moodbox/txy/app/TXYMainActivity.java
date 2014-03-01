package es.moodbox.txy.app;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import es.moodbox.txy.app.domain.Meetup;
import es.moodbox.txy.app.domain.User;
import es.moodbox.txy.app.services.TXYFinderService;

public class TXYMainActivity extends ActionBarActivity {

	public static final String ES_MOODBOX_TXY_USER_FOUND = "es.moodbox.txy.UserFound";

	private final static int REQUEST_ENABLE_BT = 1;

	private BluetoothAdapter mBluetoothAdapter;

	private User user;

	private List<Meetup> mMeetups;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_txy_main);

		mMeetups = new ArrayList<Meetup>();

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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

		//get the user name and address
		user = new User(mBluetoothAdapter.getName(), mBluetoothAdapter.getAddress());

		// start the service
		// use this to start and trigger a service
		Intent intentService = new Intent(getApplicationContext(), TXYFinderService.class);
		startService(intentService);
	}

	private BroadcastReceiver broadCastServiceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			Log.d("@@ BR Received from service has user: " + extras.containsKey(TXYFinderService.USER_KEY), "");
			//the other user
			String brUserNameB = extras.getString(TXYFinderService.USER_KEY);
			String userBName = brUserNameB.split("-")[0];
			String userBAddress = brUserNameB.split("-")[1];

			Meetup meetup = new Meetup(user, new User(userBName, userBAddress));

			//if not already added
			if (!mMeetups.contains(meetup)) {
				mMeetups.add(meetup);
			}
			addToScreen(mMeetups);
			Toast.makeText(context, "User found: " + brUserNameB, Toast.LENGTH_LONG).show();

			// Vibrate the mobile phone
			//Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
			//vibrator.vibrate(500);
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.txymain, menu);
		return true;
	}


	private void addToScreen(List<Meetup> users) {
		TextView usersTextView = (TextView) findViewById(R.id.usersTextView);
		StringBuffer sb = new StringBuffer("");
		for (Meetup meetup : users) {
			sb.append(meetup.toString());
			sb.append("\n");
		}
		usersTextView.setText(sb.toString());
	}

}
