package es.moodbox.txy.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import es.moodbox.txy.app.services.TXYFinderService;

/**
 * Created by suarvic on 10/02/14.
 */
public class TXYReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("@@ TXYReceiver", " starting service, phone just send the BOOT_COMPLETED! ");
        Intent service = new Intent(context, TXYFinderService.class);
        context.startService(service);
    }
}
