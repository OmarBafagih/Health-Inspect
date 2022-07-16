package com.example.healthinspector.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.healthinspector.Adapters.KrogerLocationAdapter;
import com.example.healthinspector.Constants;
import com.example.healthinspector.CreateRecommendations;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.LocationService;
import com.example.healthinspector.Models.ScannedProduct;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.FragmentHomeBinding;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;


public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private FragmentHomeBinding binding;
    public static final String TAG = "HomeFragment";
    private ArrayList<String> categories;
    private static final int CATEGORIES_COUNT = 2;
    private static final int NEARBY_PLACES_RADIUS = 2000;
    private static final String GOOGLE_PLACES_REQUEST_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=grocery+store&location=%f,%f&radius=%d&region=us&type=cafe,bakery&key=%s";
    private static final String RESULTS = "results";
    private static final String GEOMETRY = "geometry";
    private static final String PLACE_ADDRESS = "formatted_address";
    private static final String PLACE_LATITUDE = "lat";
    private static final String PLACE_LONGITUDE = "lng";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.homeMap);
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //generating preset recommendations
        categories = new ArrayList<>();
        categories.add("snacks");
        categories.add("nuts");
        String url = Constants.URL_REQUEST_PRODUCTS;
        for(int i = 0; i < CATEGORIES_COUNT; i++){
            url+= String.format(Constants.CATEGORY_PARAMS, i, i, i, categories.get(i));
        }
        url += String.format("&tagtype_%d=nutrition_grades&tag_contains_%d=contains&tag_%d=A&additives=without&ingredients_from_palm_oil=without&json=true", CATEGORIES_COUNT, CATEGORIES_COUNT, CATEGORIES_COUNT);
        try {
            CreateRecommendations.getRecommendedProducts(new ScannedProduct(), url, requireContext(), FragmentSwitch.HOME_FRAGMENT, view);
        } catch (JSONException | JsonProcessingException | ParseException e) {
            //toast already gets called for these exceptions when creatingProductRecommendations, do not want to show the toast twice
            e.printStackTrace();
            Log.e(TAG,"JSON Exception trying to retrieve recommendedProducts: " + e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            AtomicReference<Location> currentLocation = new AtomicReference<>(LocationService.getLastLocation());
            if(currentLocation.get() != null){
                findNearbyGroceryStores(requireContext(), currentLocation.get(), googleMap);
                return;
            }
            //wait for location service to give lastLocation a value
            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                currentLocation.set(LocationService.getLastLocation());
                findNearbyGroceryStores(requireContext(), currentLocation.get(), googleMap);
            }, Constants.DELAY_SLOW);
        }
    }

    public void findNearbyGroceryStores(Context context, Location location, @NonNull GoogleMap googleMap){
        if(location == null){
            Log.i(TAG, "could not get nearby grocery stores");
            Toast.makeText(context, context.getString(R.string.enable_locations_prompt), Toast.LENGTH_SHORT).show();
            return;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(location.getLatitude(), location.getLongitude()));

        ArrayList<JSONObject> locations = new ArrayList<>();
        String url = String.format(GOOGLE_PLACES_REQUEST_URL, location.getLatitude(),  location.getLongitude(), NEARBY_PLACES_RADIUS, context.getString(R.string.maps_key));
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                try {
                    JSONArray nearbyLocations = response.getJSONArray(RESULTS);
                    for(int i = 0; i < nearbyLocations.length(); i++){
                        JSONObject place = createNewLocationObject(nearbyLocations.getJSONObject(i));
                        locations.add(place);
                        LatLng storeLocation = new LatLng(locations.get(i).getDouble(Constants.LATITUDE), locations.get(i).getDouble(Constants.LONGITUDE));
                        builder.include(storeLocation);
                        googleMap.addMarker(new MarkerOptions().position(storeLocation).title(place.getString(Constants.STORE_NAME))).showInfoWindow();
                    }
                    populateLocations(googleMap, locations, builder);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, context.getString(R.string.error_retrieving_nearby_places), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "JSON Exception tying to retrieve nearby locations: " + e);
                }
            }, error -> {
                Toast.makeText(context, context.getString(R.string.error_retrieving_nearby_places), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Google places request error: " + error);
            });
        queue.add(stringRequest);
    }

    public void populateLocations(@NonNull GoogleMap googleMap, ArrayList<JSONObject> locations, LatLngBounds.Builder builder){
        binding.homeLocationsProgressBar.setVisibility(View.GONE);
        LocationService.sortLocations(locations, requireContext());
        binding.nearbyLocationsRecyclerView.setAdapter(new KrogerLocationAdapter(requireContext(), locations, FragmentSwitch.HOME_FRAGMENT));
        binding.nearbyLocationsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        //pin all local locations
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), Constants.MAP_PADDING);
        googleMap.animateCamera(cameraUpdate);
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
}

