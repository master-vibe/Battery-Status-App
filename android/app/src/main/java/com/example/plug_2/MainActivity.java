package com.example.plug_2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Bundle;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCodec;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL_FOREGROUND_SERVICE = "android_platform_channel";
    private FlutterEngine flutterEngine;

    @Override
    public void configureFlutterEngine(FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        this.flutterEngine = flutterEngine;

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL_FOREGROUND_SERVICE)
                .setMethodCallHandler(new MethodCallHandler() {
                    @Override
                    public void onMethodCall(MethodCall call, Result result) {
                        if (call.method.equals("startForegroundService")) {
                            startForegroundService();
                            result.success(true);
                        } else if (call.method.equals("stopForegroundService")) {
                            stopForegroundService();
                            result.success(true);
                        } else {
                            result.notImplemented();
                        }
                    }
                });
    }

    private void startForegroundService() {
        // Start the foreground service here
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        startForegroundService(serviceIntent);

    }
    private  void stopForegroundService(){
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        stopService(serviceIntent);
    }


}