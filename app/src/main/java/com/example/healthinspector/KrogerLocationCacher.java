package com.example.healthinspector;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

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
    private static final String ADDRESS_LINE = "addressLine1";
    private static final String TOKEN_REQUEST_URL = "https://api.kroger.com/v1/connect/oauth2/token";
    private static final String LOCATION_REQUEST_URL = "https://api.kroger.com/v1/locations?filter.latLong.near=";

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
                .url(TOKEN_REQUEST_URL)
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader(Constants.AUTHORIZATION, "Basic " + context.getString(R.string.kroger_key))
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
                    Log.e(TAG, "Error retrieving Kroger token " + e);
                    Toast.makeText(context, getString(R.string.error_retrieving_locations), Toast.LENGTH_SHORT).show();
                }
            }
        });
        return token;
    }

    public ArrayList<JSONObject> makeLocationRequest(Double latitude, Double longitude, Context context) {
        ArrayList<JSONObject> locations = new ArrayList<>();
        OkHttpClient locationsRequestClient = new OkHttpClient();

        Request requestLocations = new Request.Builder()
                .url(LOCATION_REQUEST_URL + (latitude) + "," + longitude)
                .get()
                .addHeader("Accept", "application/json")
                .addHeader(Constants.AUTHORIZATION, "Bearer " + getToken(context))
                .build();

        locationsRequestClient.newCall(requestLocations).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error retrieving locations " + e);
                Toast.makeText(context, getString(R.string.error_retrieving_locations), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response.body().string());
                    int limit = Constants.MAX_LOCATIONS;
                    for(int i = 0; i < Integer.min(limit, jsonResponse.length()); i++){
                        JSONObject location = new JSONObject();
                        if(jsonResponse.has(Constants.DATA)){
                            JSONObject data = jsonResponse.getJSONArray(Constants.DATA).getJSONObject(i);
                            location.put(Constants.LOCATION_ID, data.getString(Constants.LOCATION_ID));
                            location.put(Constants.STORE_NAME, data.getString(NAME));
                            location.put(Constants.ADDRESS, data.getJSONObject(Constants.ADDRESS).getString(ADDRESS_LINE));
                            location.put(Constants.LATITUDE, data.getJSONObject(GEOLOCATION).getDouble(Constants.LATITUDE));
                            location.put(Constants.LONGITUDE, data.getJSONObject(GEOLOCATION).getDouble(Constants.LONGITUDE));
                            locations.add(location);
                        }

                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error storing locations: " + e);
                }
            }
        });
        return locations;
    }

    public ArrayList<JSONObject> getKrogerLocations() {
        return krogerLocations;
    }
}
