package com.example.healthinspector;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;

public class LocationService extends Service {
    private LocationManager locationManager;
    private static Location lastLocation;
    private static final String TAG = "LocationService";
    private static final int LOCATION_UPDATE_INTERVAL = 10000;
    private static final int DISTANCE_TRAVELLED_FOR_UPDATE = 2;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = this.getBaseContext();
        KrogerLocationCacher.getInstance().makeTokenRequest(this.getBaseContext());
        //delay for token variable to have new value on return
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (intent != null) {
                    final String action = intent.getAction();
                    if(action != null && action == Constants.PERMISSIONS_GRANTED){
                        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_INTERVAL, DISTANCE_TRAVELLED_FOR_UPDATE, new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                if (lastLocation == null || lastLocation.distanceTo(location) > Constants.SIGNIFICANT_LOCATION_CHANGE) {
                                    lastLocation = location;
                                    KrogerLocationCacher.getInstance().makeTokenRequest(context);
                                    KrogerLocationCacher.getInstance().getNearbyKrogerLocations(location.getLatitude(), location.getLongitude(), getBaseContext()).toString();
                                }
                            }
                        });
                    }
                }
            }
        }, Constants.DELAY_FAST);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static Location getLastLocation() {
        return lastLocation;
    }
}