package com.example.plug_2;

import android.content.SharedPreferences;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

import java.util.List;
import java.util.Map;

public class BatteryReceiver extends BroadcastReceiver {
    private static final String TAG = "BatteryReceiver";
    private static final String PREFS_NAME = "ServicePrefs";
    private static final String KEY_SERVICE_RUNNING = "ServiceRunning";
    private static boolean isCharging = false;
    private static boolean charAtPlayed = false;
    private final List<Map<String, Object>> list;

    public BatteryReceiver(List<Map<String, Object>> list) {
        this.list = list;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
            Log.d(TAG, "Device shutting down. Saving service state.");
            prefs.edit().putBoolean(KEY_SERVICE_RUNNING, true).apply(); // Assuming service is running
        }

        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int percentage = (level * 100) / scale;
//        Log.d(TAG, "Battery Status: " + status);

        // Handle battery status
        handleBatteryStatus(context, status, percentage);
    }

    private void handleBatteryStatus(Context context, int status, int percentage) {
        boolean isEnabled;

        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                handleCharging(context, percentage);
                break;

            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                isEnabled = Boolean.parseBoolean(list.get(1).get("enabled").toString());
                if (isCharging && isEnabled) {
                    startServiceWithAction(context, "PLAY_MEDIA_DISCHARGING");
                }
                isCharging = false;
                charAtPlayed = false;
//                Log.d(TAG, "Discharging Triggered: " + isCharging);
                break;

            case BatteryManager.BATTERY_STATUS_FULL:
                isEnabled = Boolean.parseBoolean(list.get(2).get("enabled").toString());
                if (isEnabled) {
                    startServiceWithAction(context, "PLAY_MEDIA_FULL_CHARGE");
                }
                break;

            default:
//                Log.d(TAG, "Unhandled Battery Status: " + status);
                break;
        }
    }

    private void handleCharging(Context context, int percentage) {
        boolean isEnabledPlugIn = Boolean.parseBoolean(list.get(0).get("enabled").toString());
        boolean isEnabledChargeAt = Boolean.parseBoolean(list.get(3).get("enabled").toString());

        if (!isCharging && isEnabledPlugIn) {
            startServiceWithAction(context, "PLAY_MEDIA_CHARGING");
        }

        if (!charAtPlayed && isEnabledChargeAt) {
            int targetPercentage = Integer.parseInt(list.get(3).get("value").toString());
            if (percentage == targetPercentage && isCharging) {
                startServiceWithAction(context, "PLAY_MEDIA_CHARGE_AT");
                charAtPlayed=true;
            }
        }
        isCharging = true;
//        Log.d(TAG, "Charging Triggered: " + percentage);
    }

    private void startServiceWithAction(Context context, String action) {
        Intent serviceIntent = new Intent(context, MyForegroundService.class);
        serviceIntent.setAction(action);
        context.startService(serviceIntent);
    }
}
