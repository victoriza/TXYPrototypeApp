package es.moodbox.txy.app.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import es.moodbox.txy.app.TXYMainActivity;
import es.moodbox.txy.app.domain.Meetup;
import es.moodbox.txy.app.domain.User;

/**
 * Created by suarvic on 10/02/14.
 */
public class MeetupFinderService extends Service {

	private static final int DELAY_MINUTES = 1;

	public final static String USER_KEY = "user";

	private BluetoothAdapter mBluetoothAdapter;

	private static User user;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("@@ - MeetupFinderService", "onStartCommand #extras: ");

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		user = new User(mBluetoothAdapter.getName(), mBluetoothAdapter.getAddress());
		//TODO: check if not duplicated, broadcast
		//findPairedDevices();

		// Register the BroadcastReceiver
		registerBluetoothActionFoundReceiver();

		//discovers devices
		mBluetoothAdapter.startDiscovery();

		scheduleNextUpdate(DELAY_MINUTES);

		//Similar to Service.START_STICKY but the original Intent is re-delivered to the onStartCommand method.
		return Service.START_REDELIVER_INTENT;
	}

	private void scheduleNextUpdate(int delayedMinutes) {
		Log.d("@@ - MeetupFinderService", "scheduleNextUpdate");
		Intent intent = new Intent(this, this.getClass());
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// The update frequency should often be user configurable. This is not.
		long nextUpdateTimeMillis = System.currentTimeMillis() + delayedMinutes * DateUtils.MINUTE_IN_MILLIS;
		Time nextUpdateTime = new Time();
		nextUpdateTime.set(nextUpdateTimeMillis);

		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC, nextUpdateTimeMillis, pendingIntent);
	}

	private void begingDiscoberable() {
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivity(discoverableIntent);
	}


	private void userFoundBroadcast(String user) {
		Intent sendBroadcast = new Intent(TXYMainActivity.ES_MOODBOX_TXY_USER_FOUND);
		sendBroadcast.putExtra(USER_KEY, user);

		sendBroadcast(sendBroadcast);
		Log.d("@@ - MeetupFinderService", "Broadcast sent #meetups: " + TXYMainActivity.mMeetups.size());
	}

	private void registerBluetoothActionFoundReceiver() {
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
	}

	private void findPairedDevices() {
		Log.d(this.getClass().getName(), "@@ -- Starting finding Paired Devices");
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {

				Log.d("@@ - findPairedDevices", device == null ? " null name " : device.getName());

				// Add the name and address to an array adapter to show in a ListView
				Meetup meetup = new Meetup(user, new User(device.getName(), device.getAddress()));
				if (!TXYMainActivity.mMeetups.contains(meetup)) {
					TXYMainActivity.mMeetups.add(meetup);
				}
			}
		}
		Log.d(this.getClass().getName(), "@@ -- End finding Paired Devices");
	}

	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String intentAction = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(intentAction)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				Log.d("@@ ------------> BroadcastReceiver", device == null ? " null name " : device.getName() + " #mMeetups: "+TXYMainActivity.mMeetups);

				//If not already added
				Meetup meetup = new Meetup(user, new User(device.getName(), device.getAddress()));
				if (!TXYMainActivity.mMeetups.contains(meetup)) {
					Log.d("@@ ---> BroadcastReceiver", " added");
					TXYMainActivity.mMeetups.add(meetup);
					//send the broadcast to do the update
					userFoundBroadcast(device.getName() + "-" + device.getAddress());
				}
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("@@ - MeetupFinderService", "onBind!");
		return null;
	}

	public void onDestroy() {
		super.onDestroy();

		unregisterReceiver(mReceiver);

		Log.e("@@ Destroy", " system is shutting down the service");
	}
}
