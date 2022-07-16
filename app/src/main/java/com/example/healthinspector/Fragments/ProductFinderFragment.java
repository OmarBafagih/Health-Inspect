package com.example.healthinspector.Fragments;

import android.Manifest;
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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.healthinspector.Adapters.KrogerLocationAdapter;
import com.example.healthinspector.Constants;
import com.example.healthinspector.KrogerLocationCacher;
import com.example.healthinspector.LocationService;
import com.example.healthinspector.Models.RecommendedProduct;
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
    private static final String PRODUCT_REQUEST_URL = "https://api.kroger.com/v1/products?filter.brand=";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductFinderBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.productFinderMap);
        mapFragment.getMapAsync(this);

        locations = new ArrayList<>();
        ArrayList<JSONObject> storedLocations = KrogerLocationCacher.getInstance().getKrogerLocations();
        Bundle bundle = getArguments();
        RecommendedProduct recommendedProduct = Parcels.unwrap(bundle.getParcelable(Constants.RECOMMENDED_PRODUCT));
        count = 0;

        if(storedLocations == null || storedLocations.size() == 0){
            Toast.makeText(requireContext(), getString(R.string.no_nearby_locations), Toast.LENGTH_LONG).show();
            //navigate user back to product details fragment and finish this fragment
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction =  fragmentManager.beginTransaction();
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentManager.popBackStack();
            fragmentTransaction.commit();
            return null;
        }

        for(int i = 0; i < storedLocations.size(); i++){
            JSONObject location = null;
            try {
                location = new JSONObject(storedLocations.get(i).toString());
            } catch (JSONException e) {
                Toast.makeText(requireContext(), getString(R.string.error_finding_locations), Toast.LENGTH_SHORT).show();
                Log.e(TAG,"Error adding location to locations Arraylist", e);
            }
            locations.add(location);
        }

        //API request to check all locations for the recommended product's availability
        checkProductAvailability(recommendedProduct);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //wait for API request to complete, then populate recycler view

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            binding.productFinderProgressBar.setVisibility(View.GONE);
            //sort the json array by stores that have the item in stock, then by nearest location
            LocationService.sortLocations(locations, requireContext());
            binding.locationsRecyclerView.setAdapter(new KrogerLocationAdapter(requireContext(), locations));
            binding.locationsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        }, Constants.DELAY_SLOW);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        //map zoom to current location
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            googleMap.setMyLocationEnabled(true);
        }
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
                Log.e(TAG,"Error marking location on map ", e);
            }
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, Constants.MAP_PADDING);
        googleMap.animateCamera(cameraUpdate);
    }

    public void checkProductAvailability(RecommendedProduct recommendedProduct){
        for(int i = 0; i < locations.size(); i++){
            count = i;
            OkHttpClient client = new OkHttpClient();
            Request request = null;
            try {
                request = new Request.Builder()
                        .url(PRODUCT_REQUEST_URL + recommendedProduct.getBrand() + "&filter.term=" + recommendedProduct.getKeyWords() + "&filter.fulfillment=ais" + "&filter.locationId=" + locations.get(i).getString(Constants.LOCATION_ID))
                        .get()
                        .addHeader("Accept", "application/json")
                        .addHeader(Constants.AUTHORIZATION, "Bearer " + KrogerLocationCacher.getInstance().getToken(requireContext()))
                        .build();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText(requireContext(), getString(R.string.error_finding_locations), Toast.LENGTH_SHORT).show();
                    Log.e(TAG,"onFailure for product request ", e);
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        if(jsonResponse.has(Constants.DATA) && jsonResponse.getJSONArray(Constants.DATA).length() > 0){
                            locations.get(count).put(Constants.IN_STOCK, true);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(requireContext(), getString(R.string.error_finding_locations), Toast.LENGTH_SHORT).show();
                        Log.e(TAG,"Error retrieving response ", e);
                    }
                }
            });
        }
    }
}