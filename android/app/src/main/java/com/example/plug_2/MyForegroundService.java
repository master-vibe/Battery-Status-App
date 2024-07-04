package com.example.plug_2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.*;

import androidx.annotation.Nullable;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

import java.io.IOException;

public class MyForegroundService extends Service {
    private static final String CHANNEL_ID = "android_platform_channel";
    private static final int NOTIFICATION_ID = 1;

    private boolean isFirst = true;
    private boolean alreadyCharging = false;
    private MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
        mediaPlayer = MediaPlayer.create(this, R.raw.d);

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());

        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryReceiver);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
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
                .setContentTitle("App is working")
                .setContentText("I am hacking in background beware. Click for more info..")
                .setSmallIcon(R.raw.plug)
                .setContentIntent(pendingIntent)
                .build();

        return notification;
    }
    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;

            if(isCharging!=alreadyCharging ){
                alreadyCharging=isCharging;
                if(isCharging && !isFirst){
                    playMusic();
                } else if (!isCharging && !isFirst) {
                    playMusic();
                }
                isFirst=false;
            }
        }
    };
    private void playMusic() {
        if (mediaPlayer == null) {
            return;
        }
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        } else {
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            mediaPlayer.start();
        }
    }
}