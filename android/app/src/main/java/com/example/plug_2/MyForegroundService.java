package com.example.plug_2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Build;
import android.util.Log;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class MyForegroundService extends Service {
    private static final String CHANNEL_ID = "android_platform_channel";
    private static final String PREFS_NAME = "ServicePrefs";
    private static final int NOTIFICATION_ID = 1;

    private static final String ACTION_PLAY_MEDIA_CHARGING = "PLAY_MEDIA_CHARGING";
    private static final String ACTION_PLAY_MEDIA_DISCHARGING = "PLAY_MEDIA_DISCHARGING";
    private static final String ACTION_PLAY_MEDIA_FULL_CHARGE = "PLAY_MEDIA_FULL_CHARGE";
    private static final String ACTION_PLAY_MEDIA_CHARGE_AT = "PLAY_MEDIA_CHARGE_AT";

    private BatteryReceiver batteryReceiver;
    private List<Map<String, Object>> list;
    private MediaPlayer mediaPlayer;

    @Override
    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeReceiver();
        createNotificationChannel();
        Log.e("ForegroundService", "Foreground Service Started....");
        Log.e("BatteryReceiver", "Initialized and registered in onCreate....");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());
        handleAction(intent.getAction());
        return START_STICKY;
    }

    private void handleAction(String action) {
        if (action == null) return;

        String filePath = null;
        switch (action) {
            case ACTION_PLAY_MEDIA_CHARGING:
                filePath = list.get(0).get("file_path").toString();
                break;
            case ACTION_PLAY_MEDIA_DISCHARGING:
                filePath = list.get(1).get("file_path").toString();
                break;
            case ACTION_PLAY_MEDIA_FULL_CHARGE:
                filePath = list.get(2).get("file_path").toString();
                break;
            case ACTION_PLAY_MEDIA_CHARGE_AT:
                filePath = list.get(3).get("file_path").toString();
                break;
        }

        if (filePath != null) {
            playMusic(getApplicationContext(), filePath);
        }
    }

    private void playMusic(Context context, String filePath) {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        } else {
            mediaPlayer = new MediaPlayer();
        }

        try {
            Uri audioUri = Uri.parse(filePath);
            mediaPlayer.setDataSource(context, audioUri);
            mediaPlayer.prepare();
            mediaPlayer.start();

            // Stop playback after 5 seconds
            new Handler(Looper.getMainLooper()).postDelayed(() -> stopMediaPlayer(), 8000);

        } catch (Exception e) {
            Log.e("MyForegroundService", "Error while playing music: ", e);
        }
    }

    private void stopMediaPlayer() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void initializeReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_SHUTDOWN);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        this.list = jsonStringToList(preferences.getString("configurationList", "[]"));

        batteryReceiver = new BatteryReceiver(this.list);
        registerReceiver(batteryReceiver, filter);
    }

    private List<Map<String, Object>> jsonStringToList(String jsonString) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Map<String, Object> map = new HashMap<>();
                Iterator<String> keys = jsonObject.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    map.put(key, jsonObject.get(key));
                }
                list.add(map);
            }
        } catch (Exception e) {
            Log.e("MyForegroundService", "JSON parsing failed", e);
        }
        return list;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ForegroundService", "Stopped....");
        unregisterReceiverSafely();
        stopMediaPlayer();
    }

    private void unregisterReceiverSafely() {
        if (batteryReceiver != null) {
            try {
                unregisterReceiver(batteryReceiver);
            } catch (IllegalArgumentException e) {
                Log.e("MyForegroundService", "Receiver not registered", e);
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "My App", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Plug Notification")
                .setContentText("Service is active. Click for more info..")
                .setSmallIcon(R.raw.plug)
                .setContentIntent(pendingIntent)
                .build();
    }
}
