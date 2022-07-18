package com.example.healthinspector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.example.healthinspector.Services.LocationService;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String TAG = "NotificationReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Location Service Restarted");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, LocationService.class));
        } else {
            context.startService(new Intent(context, LocationService.class));
        }
    }
}
