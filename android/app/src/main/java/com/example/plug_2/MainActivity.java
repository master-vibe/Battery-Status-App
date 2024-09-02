package com.example.plug_2;

import java.util.List;

import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.content.ComponentName;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCodec;

public class MainActivity extends FlutterActivity {
    private static final String KEY_AUTO_START_PROMPTED = "autoStartPrompted";
    private static final String CHANNEL_FOREGROUND_SERVICE = "android_platform_channel";
    private static final String PREFS_NAME = "ServicePrefs";
    private static SharedPreferences prefs;
    private static final String KEY_SERVICE_RUNNING = "ServiceRunning";
    private FlutterEngine flutterEngine;


    @Override
    public void configureFlutterEngine(FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        this.flutterEngine = flutterEngine;
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL_FOREGROUND_SERVICE)
                .setMethodCallHandler(new MethodCallHandler() {
                    @Override
                    public void onMethodCall(MethodCall call, Result result) {
                        if (call.method.equals("startForegroundService")) {
                            String jsonString = call.arguments.toString();
                            startForegroundService(jsonString);
                            result.success(true);
                        } else if (call.method.equals("stopForegroundService")) {
                            stopForegroundService();
                            result.success(true);
                        } else if (call.method.equals("isServiceRunning")) {
                            boolean isRunning = isServiceRunning();
                            result.success(isRunning);
                        } else if (call.method.equals("getList")) {
                            String jsonString = getList();
                            result.success(jsonString);
                        } else {
                            result.notImplemented();
                        }
                    }
                });
    }

    private void startForegroundService(String jsonString) {
        if (!hasPromptedAutoStart()) {
            promptAutoStart();
        } else {
            // Start the foreground service here
            Intent serviceIntent = new Intent(this, MyForegroundService.class);
            startForegroundService(serviceIntent);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_SERVICE_RUNNING, true);
            editor.putString("configurationList", jsonString);
            editor.apply();
        }
    }

    private void stopForegroundService() {
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        stopService(serviceIntent);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_SERVICE_RUNNING, false);
        editor.apply();

    }

    private String getList() {
        return prefs.getString("configurationList", "[]");
    }

    private boolean isServiceRunning() {
        return prefs.getBoolean(KEY_SERVICE_RUNNING, false);
    }


    //For AutoStart Prompt
    private boolean hasPromptedAutoStart() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return preferences.getBoolean(KEY_AUTO_START_PROMPTED, false);
    }

    private void setPromptedAutoStart(boolean prompted) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_AUTO_START_PROMPTED, prompted).apply();
    }

    public void promptAutoStart() {
        String manufacturer = android.os.Build.MANUFACTURER.toLowerCase();
        Intent intent = new Intent();

        switch (manufacturer) {
            case "xiaomi":
                intent.setComponent(new ComponentName("com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                break;
            case "huawei":
                intent.setComponent(new ComponentName("com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"));
                break;
            case "oppo":
                // Iterate through potential Oppo components
                String[] oppoComponents = {
                        "com.coloros.safecenter/com.coloros.safecenter.permission.startup.StartupAppListActivity",
                        "com.coloros.safecenter/com.coloros.safecenter.startupapp.StartupAppListActivity",
                        "com.coloros.safecenter/com.coloros.safecenter.permission.startup.FakeActivity",
                        "com.coloros.safecenter/com.coloros.safecenter.permission.startupapp.StartupAppListActivity",
                        "com.coloros.safecenter/com.coloros.safecenter.permission.startupmanager.StartupAppListActivity",
                        "com.coloros.safe/com.coloros.safe.permission.startup.StartupAppListActivity",
                        "com.coloros.safe/com.coloros.safe.permission.startupapp.StartupAppListActivity",
                        "com.coloros.safe/com.coloros.safe.permission.startupmanager.StartupAppListActivity",
                        "com.coloros.safecenter/com.coloros.safecenter.permission.startsettings",
                        "com.coloros.safecenter/com.coloros.safecenter.permission.startupapp.startupmanager",
                        "com.coloros.safecenter/com.coloros.safecenter.permission.startupmanager.startupActivity",
                        "com.coloros.safecenter/com.coloros.safecenter.permission.startup.startupapp.startupmanager",
                        "com.coloros.safecenter/com.coloros.privacypermissionsentry.PermissionTopActivity.Startupmanager",
                        "com.coloros.safecenter/com.coloros.privacypermissionsentry.PermissionTopActivity",
                        "com.coloros.safecenter/com.coloros.safecenter.FakeActivity"
                };

                for (String component : oppoComponents) {
                    try {
                        String[] parts = component.split("/");
                        intent.setComponent(new ComponentName(parts[0], parts[1]));
                        startActivity(intent);
                        setPromptedAutoStart(true);
                        return;
                    } catch (Exception e) {
                        Log.d("AutoStart", "Failed with component: " + component + " - " + e.getMessage());
                    }
                }
                break;
            case "vivo":
                intent.setComponent(new ComponentName("com.vivo.permissionmanager",
                        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                break;
            case "samsung":
                Toast.makeText(this, "Please check battery optimization settings.", Toast.LENGTH_LONG).show();
                setPromptedAutoStart(true);
                return;
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                    } else {
                        setPromptedAutoStart(true);
                        return;
                    }
                } else {
                    Toast.makeText(this, "Please enable Auto Start for this app in your device settings.", Toast.LENGTH_LONG).show();
                    setPromptedAutoStart(true);
                    return;
                }
                break;
        }

        try {
            // Check if the intent has a valid action or component set
            if (intent.getComponent() != null || intent.getAction() != null) {
                // Use startActivity with a FLAG_ACTIVITY_NEW_TASK since we are in a Service
                Toast.makeText(this, "Please enable Auto Start for this app in your device settings.", Toast.LENGTH_LONG).show();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                setPromptedAutoStart(true); // Only set if the intent was successful
                Log.d("ForegroundService", "Successfully launched settings intent.");
            } else {
                Log.d("ForegroundService", "Intent action or component is null.");
            }
        } catch (Exception e) {
            Log.d("ForegroundService", "Failed to start settings intent: " + e.getMessage());
            Toast.makeText(this, "Please enable Auto Start for this app in your device settings.", Toast.LENGTH_LONG).show();
            setPromptedAutoStart(true); // Mark as prompted even if not successful
        }
    }
}