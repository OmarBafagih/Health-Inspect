package com.example.healthinspector.Fragments;

import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.healthinspector.Adapters.KrogerLocationAdapter;
import com.example.healthinspector.Constants;
import com.example.healthinspector.KrogerLocationCacher;
import com.example.healthinspector.LocationService;
import com.example.healthinspector.Models.RecommendedProduct;
import com.example.healthinspector.Models.ScannedProduct;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.FragmentProductFinderBinding;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ProductFinderFragment extends Fragment implements OnMapReadyCallback {

    private FragmentProductFinderBinding binding;
    public static final String TAG = "ProductFinderFragment";
    private int count;
    private ArrayList<JSONObject> locations;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private LocationManager locationManager;
    private Location lastLocation;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductFinderBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        locations = KrogerLocationCacher.getInstance().getKrogerLocations();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.homeMap);
        mapFragment.getMapAsync(this);

        Bundle bundle = getArguments();
        RecommendedProduct recommendedProduct = Parcels.unwrap(bundle.getParcelable(Constants.RECOMMENDED_PRODUCT));
        ScannedProduct scannedProduct = Parcels.unwrap(bundle.getParcelable(Constants.SCANNED_PRODUCT));
        count = 0;

        if(locations == null || locations.size() == 0){
            Toast.makeText(requireContext(), getString(R.string.no_nearby_locations), Toast.LENGTH_LONG).show();
            //navigate user back to product details fragment and finish this fragment
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction =  fragmentManager.beginTransaction();
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentManager.popBackStack();
            fragmentTransaction.commit();
            return null;
        }
        //API request to check all locations for the recommended product's availability
        for(int i = 0; i < locations.size(); i++){
            count = i;
            OkHttpClient client = new OkHttpClient();
            Request request = null;
            try {
                Log.i(TAG, recommendedProduct.getBrand());
                request = new Request.Builder()
                        .url("https://api.kroger.com/v1/products?filter.brand=" + recommendedProduct.getBrand() + "&filter.term=" + recommendedProduct.getKeyWords() + "&filter.fulfillment=ais" + "&filter.locationId=" + locations.get(count).getString(Constants.LOCATION_ID))
                        .get()
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Bearer " + KrogerLocationCacher.getInstance().getToken(getContext()))
                        .build();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText(requireContext(), getString(R.string.error_finding_locations), Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        Log.i(TAG, jsonResponse.toString());
                        if(jsonResponse.has(Constants.DATA)){
                            if(jsonResponse.getJSONArray(Constants.DATA).length() > 0){
                                locations.get(count).put(Constants.IN_STOCK, true);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //wait for API request to complete, then populate recycler view

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.productFinderProgressBar.setVisibility(View.GONE);
                //sort the json array by stores that have the item in stock, then by nearest location
                Location currentLocation = LocationService.getLastLocation();
                Collections.sort(locations, new Comparator<JSONObject>() {
                    @Override
                    public int compare(JSONObject object1, JSONObject object2) {
                        Location location1 = new Location("");
                        Location location2 = new Location("");
                        try {
                            location1.setLatitude(object1.getDouble(Constants.LATITUDE));
                            location1.setLongitude(object1.getDouble(Constants.LONGITUDE));

                            location2.setLatitude(object2.getDouble(Constants.LATITUDE));
                            location2.setLongitude(object2.getDouble(Constants.LONGITUDE));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(object1.has(Constants.IN_STOCK) && object2.has(Constants.IN_STOCK)){
                            //then sort by location
                            return Float.compare(currentLocation.distanceTo(location1), currentLocation.distanceTo(location2));
                        }
                        else if(object1.has(Constants.IN_STOCK) && !object2.has(Constants.IN_STOCK)){
                            return -1;
                        }
                        else if(!object1.has(Constants.IN_STOCK) && object2.has(Constants.IN_STOCK)){
                            return 1;
                        }
                        else{
                            //just sort by location
                            return Float.compare(currentLocation.distanceTo(location1), currentLocation.distanceTo(location2));
                        }
                    }
                });
                KrogerLocationAdapter locationsAdapter = new KrogerLocationAdapter(requireContext(), locations);
                binding.locationsRecyclerView.setAdapter(locationsAdapter);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
                binding.locationsRecyclerView.setLayoutManager(linearLayoutManager);
            }
        }, Constants.DELAY_SLOW);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        //map zoom to current location
        googleMap.setMyLocationEnabled(true);
        Location lastLocation = LocationService.getLastLocation();

        //pin all nearby locations on map
        ArrayList<JSONObject> locations = KrogerLocationCacher.getInstance().getKrogerLocations();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));


        for(int i = 0; i < locations.size(); i++){
            try {
                LatLng storeLocation = new LatLng(locations.get(i).getDouble(Constants.LATITUDE), locations.get(i).getDouble(Constants.LONGITUDE));
                builder.include(storeLocation);
                googleMap.addMarker(new MarkerOptions()
                        .position(storeLocation)
                        .title("Marker"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
        googleMap.animateCamera(cameraUpdate);


    }
}