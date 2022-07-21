package com.example.healthinspector.Services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.healthinspector.Cache.KrogerLocationCacher;
import com.example.healthinspector.Constants;
import com.example.healthinspector.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class LocationService extends Service {
    private LocationManager locationManager;
    private static Location lastLocation;
    private static final String TAG = "LocationService";
    private static final int LOCATION_UPDATE_INTERVAL = 1000;
    private static final int DISTANCE_TRAVELLED_FOR_UPDATE = 1000;
    private static final String CHANNEL_NAME = "location_notification";
    private static final String CHANNEL_DESCRIPTION = "nearby_stores";
    private static final int DELAY = 10000;
    private static final int ONGOING_NOTIFICATION_ID = 1;
    private static final int NEARBY_PLACES_RADIUS = 2000;
    private static final String GOOGLE_PLACES_REQUEST_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=grocery+store&location=%f,%f&radius=%d&region=us&type=supermarket&key=%s";
    private static final String RESULTS = "results";
    private static final String GEOMETRY = "geometry";
    private static final String PLACE_ADDRESS = "formatted_address";
    private static final String PLACE_LATITUDE = "lat";
    private static final String PLACE_LONGITUDE = "lng";
    private final Handler handler = new Handler();
    private NotificationCompat.Builder builder;
    private Notification notification;
    private FusedLocationProviderClient fusedLocationClient;
    private static ArrayList<JSONObject> nearbyGroceryLocations;
    private Context context;
    private static final String NOTIFICATION_MESSAGE = "Closest grocery store %s is: %.2f meters away, remember to checkout items in your cart!";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            createNotificationChannel();
            startForeground();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this.getBaseContext();
        KrogerLocationCacher.getInstance().getToken(context);
        if (intent != null) {
            final String action = intent.getAction();
            if(action != null && action == Constants.PERMISSIONS_GRANTED){
                if(lastLocation == null){
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
                    fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                        lastLocation = location;
                        findNearbyGroceryStores(context, location);
                    });
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateLocation(context);
                        try {
                            notifyClosestStore();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        handler.postDelayed(this, DELAY);
                    }
                }, DELAY);
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTask();
    }

    @SuppressLint("MissingPermission")
    public void updateLocation(Context context){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_INTERVAL, DISTANCE_TRAVELLED_FOR_UPDATE, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (lastLocation == null || lastLocation.distanceTo(location) > Constants.SIGNIFICANT_LOCATION_CHANGE) {
                    lastLocation = location;
                    KrogerLocationCacher.getInstance().makeTokenRequest(context);
                    KrogerLocationCacher.getInstance().getNearbyKrogerLocations(location.getLatitude(), location.getLongitude(), context);
                    findNearbyGroceryStores(context, lastLocation);
                }
            }
        });
    }
    public void stopTask() {
        if (handler != null) {
            lastLocation = null;
            handler.removeCallbacksAndMessages(null);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startForeground() throws JSONException {
        builder = new NotificationCompat.Builder(this, Constants.CHANNEL_ID);
        PendingIntent pendingQuitIntent = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), new Intent(Constants.QUIT_ACTION), PendingIntent.FLAG_MUTABLE);
        notification = builder.setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_default_content))
                .setSmallIcon(R.drawable.health_inspector_logo_1)
                .addAction(R.drawable.health_inspector_logo_1, getText(R.string.notification_quit_button),
                        pendingQuitIntent)
                .setAutoCancel(true)
                .build();
        startForeground(ONGOING_NOTIFICATION_ID, notification);
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

    public static Location getLastLocation() {
        return lastLocation;
    }
    public static ArrayList<JSONObject> getNearbyGroceryLocations(){
        return nearbyGroceryLocations;
    }

    public void findNearbyGroceryStores(Context context, Location location){
        if(location == null){
            Log.i(TAG, "could not get nearby grocery stores");
            Toast.makeText(context, context.getString(R.string.enable_locations_prompt), Toast.LENGTH_SHORT).show();
            return;
        }
        nearbyGroceryLocations = new ArrayList<>();
        String url = String.format(GOOGLE_PLACES_REQUEST_URL, location.getLatitude(),  location.getLongitude(), NEARBY_PLACES_RADIUS, context.getString(R.string.maps_key));
        Log.i(TAG, url);
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray nearbyLocations = response.getJSONArray(RESULTS);
                        Log.i(TAG, nearbyLocations.toString());
                        for(int i = 0; i < nearbyLocations.length(); i++){
                            JSONObject place = createNewLocationObject(nearbyLocations.getJSONObject(i));
                            nearbyGroceryLocations.add(place);
                        }
                        sortLocations(nearbyGroceryLocations, context);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "JSON Exception tying to retrieve nearby locations: " + e);
                    }
                }, error -> {
            Toast.makeText(context, context.getString(R.string.error_retrieving_nearby_places), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Google places request error: " + error);
        });
        queue.add(stringRequest);
    }
    public JSONObject createNewLocationObject(JSONObject place) throws JSONException {
        JSONObject newLocation = new JSONObject();
        newLocation.put(Constants.STORE_NAME, place.getString(Constants.NAME));
        newLocation.put(Constants.ADDRESS, place.getString(PLACE_ADDRESS));
        JSONObject placeLocation = place.getJSONObject(GEOMETRY).getJSONObject(Constants.LOCATION);
        newLocation.put(Constants.LATITUDE, placeLocation.getDouble(PLACE_LATITUDE));
        newLocation.put(Constants.LONGITUDE,  placeLocation.getDouble(PLACE_LONGITUDE));
        return newLocation;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESCRIPTION);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
    public void notifyClosestStore() throws JSONException {
        if(nearbyGroceryLocations.size() > 0){
            JSONObject closestLocationObject = nearbyGroceryLocations.get(0);
            Location closestLocation = new Location("");
            closestLocation.setLongitude(closestLocationObject.getDouble(Constants.LONGITUDE));
            closestLocation.setLatitude(closestLocationObject.getDouble(Constants.LATITUDE));
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.CHANNEL_ID);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent pendingQuitIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), new Intent(Constants.QUIT_ACTION), PendingIntent.FLAG_MUTABLE);
            mNotificationManager.notify(1, builder.setContentTitle(getText(R.string.notification_title))
                    .setContentText(String.format(NOTIFICATION_MESSAGE, closestLocationObject.getString(Constants.STORE_NAME), lastLocation.distanceTo(closestLocation)))
                    .setSmallIcon(R.drawable.health_inspector_logo_1)
                    .addAction(R.drawable.health_inspector_logo_1, getText(R.string.notification_quit_button),
                            pendingQuitIntent)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .build());
        }
    }
}