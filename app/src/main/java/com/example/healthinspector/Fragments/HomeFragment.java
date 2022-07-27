package com.example.healthinspector.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.healthinspector.Adapters.KrogerLocationAdapter;
import com.example.healthinspector.Constants;
import com.example.healthinspector.CreateRecommendations;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.Models.ScannedProduct;
import com.example.healthinspector.R;
import com.example.healthinspector.Services.LocationService;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private FragmentHomeBinding binding;
    public static final String TAG = "HomeFragment";
    private ArrayList<String> categories;
    private static final int CATEGORIES_COUNT = 2;
    private LatLngBounds.Builder builder;
    private Location currentLocation;
    private ArrayList<JSONObject> nearbyGroceryLocations;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.homeMap);
        mapFragment.getMapAsync(this);
        builder = new LatLngBounds.Builder();
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
        googleMap.clear();
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    googleMap.setMyLocationEnabled(true);
                    if(nearbyGroceryLocations != null && nearbyGroceryLocations.size() > 0 && nearbyGroceryLocations.equals(LocationService.getNearbyGroceryLocations())){
                        populateLocations(googleMap);
                    }

                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(() -> {
                        currentLocation = LocationService.getLastLocation();
                        populateLocations(googleMap);
                    }, Constants.DELAY_SLOW);
                }
            }
        });
    }

    public void populateLocations(@NonNull GoogleMap googleMap){
        if(this.binding != null){
            if(currentLocation != null){
                builder = new LatLngBounds.Builder();
                builder.include(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                nearbyGroceryLocations = LocationService.getNearbyGroceryLocations();
                if(nearbyGroceryLocations.size() > 0){
                    for(int i = 0; i < nearbyGroceryLocations.size(); i++){
                        JSONObject nearbyGroceryStore = nearbyGroceryLocations.get(i);
                        try {
                            LatLng storeLocation = new LatLng(nearbyGroceryStore.getDouble(Constants.LATITUDE), nearbyGroceryStore.getDouble(Constants.LONGITUDE));
                            builder.include(storeLocation);
                            googleMap.addMarker(new MarkerOptions().position(storeLocation).title(nearbyGroceryStore.getString(Constants.STORE_NAME))).showInfoWindow();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if(binding.locationAnimationView != null){
                        binding.locationAnimationView.setVisibility(View.GONE);
                    }
                    LocationService.sortLocations(nearbyGroceryLocations, requireContext());
                    binding.nearbyLocationsRecyclerView.setAdapter(new KrogerLocationAdapter(requireContext(), nearbyGroceryLocations, FragmentSwitch.HOME_FRAGMENT));
                    binding.nearbyLocationsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                    //pin all local locations
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), Constants.MAP_PADDING);
                    googleMap.animateCamera(cameraUpdate);
                }
                return;
            }
        }
    }
}

