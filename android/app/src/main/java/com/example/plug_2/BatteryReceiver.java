package com.example.plug_2;

import android.content.SharedPreferences;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.util.Log;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.Map;


public class BatteryReceiver extends BroadcastReceiver {
    private static final String TAG = "BatteryReceiver";
    private static final String PREFS_NAME = "ServicePrefs";
    private static final String KEY_SERVICE_RUNNING = "ServiceRunning";
    private static boolean isCharging = false;
    private final List<Map<String,Object>> list;
    private MediaPlayer mediaPlayer;
    BatteryReceiver(List<Map<String,Object>> list){
        this.list=list;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        Log.d(TAG,list.toString());
        if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
            Log.d(TAG, "Device shutting down. Saving service state.");
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_SERVICE_RUNNING, true); // Assuming service is running at shutdown or else BatteryReceiver won't be registered.
            editor.apply();
        }
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
        int percentage = level*100/scale;
//        Log.d(TAG,String.valueOf(percentage));
        boolean isEnabled;
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                boolean isEnabled_plugIn = Boolean.parseBoolean(list.get(0).get("enabled").toString());
                boolean isEnabled_chargeAt = Boolean.parseBoolean(list.get(3).get("enabled").toString());
                if(!isCharging && isEnabled_plugIn ){
                    String file_path = list.get(0).get("file_path").toString();
                    playMusic(context,file_path);
                }
                if(isEnabled_chargeAt){
                    int currentPercentage = Integer.parseInt(list.get(3).get("value").toString());
                    if(percentage==currentPercentage && isCharging) {
                        String file_path = list.get(3).get("file_path").toString();
                        playMusic(context,file_path);
                    }
                }
                isCharging=true;
                Log.d(TAG, "Charging Triggered:"+isCharging);

                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                isEnabled = Boolean.parseBoolean(list.get(1).get("enabled").toString());
                if (isCharging && isEnabled){
                    String file_path = list.get(1).get("file_path").toString();
                    playMusic(context,file_path);
                }
                isCharging=false;
//                Log.d(TAG, "Discharging Triggered:"+isCharging);
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                isEnabled = Boolean.parseBoolean(list.get(2).get("enabled").toString());
                if(isEnabled){
                    String file_path = list.get(2).get("file_path").toString();
                    playMusic(context,file_path);
                }
                break;
        }
    }

    private void playMusic(Context context,String file_path) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release(); // Release any existing MediaPlayer instance
            mediaPlayer = null;
        }
        try {
            // Use the resource ID instead of a file path
            mediaPlayer = new MediaPlayer();
            Uri audioUri = Uri.parse(file_path);
            mediaPlayer.setDataSource(context,audioUri);
            if (mediaPlayer != null) {
                mediaPlayer.prepare();
                mediaPlayer.start();
            } else {
                Log.e("BatteryReceiver", "MediaPlayer could not be created.");
            }
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null; // Ensure MediaPlayer is cleaned up
                }
            }, 5000);
        } catch (Exception e) {
            Log.e("BatteryReceiver", "Error while playing music: ", e);
        }
    }
}