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
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

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

    public static void sortLocations(ArrayList<JSONObject> locations, Context context){
        //sort the json array by stores that have the item in stock, then by nearest location
        Location currentLocation = LocationService.getLastLocation();
        if(currentLocation == null){
            Toast.makeText(context, context.getString(R.string.error_retrieving_nearby_places), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error sorting locations with null user locations");
            return;
        }
        Collections.sort(locations, (object1, object2) -> {
            if (object1 == null || object2 == null){

            }
            Location location1 = new Location("");
            Location location2 = new Location("");
            try {
                location1.setLatitude(object1.getDouble(Constants.LATITUDE));
                location1.setLongitude(object1.getDouble(Constants.LONGITUDE));
                location2.setLatitude(object2.getDouble(Constants.LATITUDE));
                location2.setLongitude(object2.getDouble(Constants.LONGITUDE));
            } catch (JSONException e) {
                Log.e(TAG,"Error sorting locations in comparator ", e);
                return 0;
            }
            if(object1.has(Constants.IN_STOCK) && object2.has(Constants.IN_STOCK)){
                //then sort by location
                return Float.compare(currentLocation.distanceTo(location1), currentLocation.distanceTo(location2));
            } else if(object1.has(Constants.IN_STOCK) && !object2.has(Constants.IN_STOCK)){
                return -1;
            } else if(!object1.has(Constants.IN_STOCK) && object2.has(Constants.IN_STOCK)){
                return 1;
            } else{
                //only sort by location
                return Float.compare(currentLocation.distanceTo(location1), currentLocation.distanceTo(location2));
            }
        });
    }
}