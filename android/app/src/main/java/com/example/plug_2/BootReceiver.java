package com.example.plug_2;

import android.content.SharedPreferences;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    private static final String PREFS_NAME = "ServicePrefs";
    private static final String KEY_SERVICE_RUNNING = "ServiceRunning";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            Log.d(TAG, "Device booted. Checking service state.");

            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean wasServiceRunning = prefs.getBoolean(KEY_SERVICE_RUNNING, false);

            if (wasServiceRunning) {
                Log.d(TAG, "Service was running before shutdown. Restarting service.");
                Intent serviceIntent = new Intent(context, MyForegroundService.class);
                context.startForegroundService(serviceIntent);
            }
            else {
                Log.d(TAG, "Service was not running before shutdown");
            }
        }
    }

}