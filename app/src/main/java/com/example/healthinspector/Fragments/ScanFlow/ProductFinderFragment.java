package com.example.healthinspector.Fragments.ScanFlow;

import android.os.Bundle;

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
import android.widget.TextView;
import android.widget.Toast;

import com.example.healthinspector.Adapters.CartItemAdapter;
import com.example.healthinspector.Adapters.KrogerLocationAdapter;
import com.example.healthinspector.Constants;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.KrogerLocationCacher;
import com.example.healthinspector.Models.RecommendedProduct;
import com.example.healthinspector.Models.ScannedProduct;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.FragmentProductFinderBinding;
import com.example.healthinspector.databinding.FragmentScanBinding;

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


public class ProductFinderFragment extends Fragment {

    private FragmentProductFinderBinding binding;
    public static final String TAG = "ProductFinderFragment";
    private int count;
    private ArrayList<JSONObject> locations = KrogerLocationCacher.getInstance().getKrogerLocations();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductFinderBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

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
                        if(jsonResponse.getJSONArray(Constants.DATA).length() > 0){
                            locations.get(count).put(Constants.IN_STOCK, true);
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
                KrogerLocationAdapter locationsAdapter = new KrogerLocationAdapter(requireContext(), locations);
                binding.locationsRecyclerView.setAdapter(locationsAdapter);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
                binding.locationsRecyclerView.setLayoutManager(linearLayoutManager);
            }
        }, Constants.DELAY_SLOW);
    }

}