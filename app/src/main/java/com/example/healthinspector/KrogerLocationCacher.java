package com.example.healthinspector;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.util.Log;

import com.android.volley.toolbox.JsonObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class KrogerLocationCacher extends Application{
    private static final String TAG = "KrogerLocationCacher";
    private static KrogerLocationCacher krogerLocationCacher = null;
    private String token = null;
    private ArrayList<JSONObject> krogerLocations = null;
    private static final String ACCESS_TOKEN = "access_token";
    private static final String GEOLOCATION = "geolocation";
    private static final String NAME = "name";

    private KrogerLocationCacher() {}
    public static KrogerLocationCacher getInstance() {
        if (krogerLocationCacher == null){
            krogerLocationCacher = new KrogerLocationCacher();
        }
        return krogerLocationCacher;
    }

    public String getToken(Context context){
        if(token == null){
            token = makeTokenRequest(context);
        }
        return token;
    }

    public ArrayList<JSONObject> getNearbyKrogerLocations(Double latitude, Double longitude, Context context){
        if(krogerLocations == null){
            krogerLocations = makeLocationRequest(latitude, longitude, context);
        }
        return krogerLocations;
    }

    public String makeTokenRequest(Context context){
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&scope=product.compact");
        Request request = new Request.Builder()
                .url("https://api.kroger.com/v1/connect/oauth2/token")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", "Basic " + context.getString(R.string.kroger_key))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                try {
                    JSONObject jsonResponse = new JSONObject(response.body().string());
                    token = jsonResponse.getString(ACCESS_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return token;
    }

    public ArrayList<JSONObject> makeLocationRequest(Double latitude, Double longitude, Context context) {
        ArrayList<JSONObject> locations = new ArrayList<>();
        // create api request to get json locations
        OkHttpClient locationsRequestClient = new OkHttpClient();

        Request requestLocations = new Request.Builder()
                .url("https://api.kroger.com/v1/locations?filter.latLong.near=" + (latitude) + "," + longitude)
                .get()
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + getToken(context))
                .build();

        locationsRequestClient.newCall(requestLocations).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "ERROR RETRIEVING LOCATIONS", e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response.body().string());
                    int limit = jsonResponse.length();
                    if(limit >= Constants.MAX_LOCATIONS){
                        limit = Constants.MAX_LOCATIONS;
                    }
                    for(int i = 0; i < Integer.min(limit, jsonResponse.length()); i++){
                        JSONObject location = new JSONObject();
                        JSONObject data = jsonResponse.getJSONArray(Constants.DATA).getJSONObject(i);
                        location.put(Constants.LOCATION_ID, data.getString(Constants.LOCATION_ID));
                        location.put(Constants.STORE_NAME, data.getString(NAME));
                        location.put(Constants.ADDRESS, data.getJSONObject(Constants.ADDRESS).getString("addressLine1"));
                        location.put(Constants.LATITUDE, data.getJSONObject(GEOLOCATION).getDouble(Constants.LATITUDE));
                        location.put(Constants.LONGITUDE, data.getJSONObject(GEOLOCATION).getDouble(Constants.LONGITUDE));
                        locations.add(location);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return locations;
    }

    public ArrayList<JSONObject> getKrogerLocations() {
        return krogerLocations;
    }
}
