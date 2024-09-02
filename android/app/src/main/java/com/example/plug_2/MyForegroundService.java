package com.example.plug_2;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.SharedPreferences;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


import android.os.*;
import android.util.Log;

import androidx.annotation.Nullable;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

public class MyForegroundService extends Service {
    private static final String CHANNEL_ID = "android_platform_channel";
    private static final String PREFS_NAME = "ServicePrefs";

    private static final int NOTIFICATION_ID = 1;

    private BatteryReceiver batteryReceiver ;
    @Override

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("ForegroundService","Started....");
        if(batteryReceiver!=null){
            unregisterReceiver(batteryReceiver);
        }
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_SHUTDOWN);
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        List<Map<String,Object>> list = jsonStringToList(preferences.getString("configurationList","[]"));
        batteryReceiver=new BatteryReceiver(list);
        registerReceiver(batteryReceiver, filter);
        Log.e("BatteryReceiver","Started....");

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());

        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("BatteryReceiver", "Register is Destroyed");
        Log.d("ForegroundService","Stopped....");

        unregisterReceiver(batteryReceiver);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "My App", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);


        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Plug Notification")
                .setContentText("Service is active. Click for more info..")
                .setSmallIcon(R.raw.plug)
                .setContentIntent(pendingIntent)
                .build();

        return notification;
    }

    private List<Map<String,Object>> jsonStringToList(String jsonString){
        List<Map<String,Object>> list = new ArrayList<>();
        try {
            JSONArray jsonArray =   new JSONArray(jsonString);
            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject= jsonArray.getJSONObject(i);
                Map<String,Object> map = new HashMap<>();
                Iterator<String> keys = jsonObject.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = jsonObject.get(key);
                    map.put(key, value);
                }
                list.add(map);
            }
        }catch (Exception e){
            Log.d("BatteryReceiver","Json To List parsing failed");
        }
        return list;
    }
}