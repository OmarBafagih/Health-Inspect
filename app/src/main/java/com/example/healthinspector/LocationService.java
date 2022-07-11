package com.example.healthinspector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
//import com.android.volley.Request;
import com.android.volley.RequestQueue;
//import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LocationService extends Service {
    private LocationManager locationManager;
    private Location lastLocation;
    private static final String TAG = "LocationService";


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        KrogerLocationCacher.getInstance().makeTokenRequest(this.getBaseContext());
        //delay for token variable to have new value on return
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (intent != null) {
                    final String action = intent.getAction();
                    if (action != null) {
                        if(action == Constants.PERMISSIONS_GRANTED){
                            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    //Initially make nearbyLocations request and if location difference to the last location is significant, make a new Kroger API request for new nearby locations
                                    if (lastLocation == null) {
                                        lastLocation = location;
                                        KrogerLocationCacher.getInstance().getNearbyKrogerLocations(location.getLatitude(), location.getLongitude(), getBaseContext()).toString();
                                        return;
                                    }
                                    if (lastLocation.distanceTo(location) > 3000) {
                                        KrogerLocationCacher.getInstance().getNearbyKrogerLocations(location.getLatitude(), location.getLongitude(), getBaseContext()).toString();
                                    }
                                }
                            });
                        }
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
}