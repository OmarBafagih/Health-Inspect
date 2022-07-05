package com.example.healthinspector.Fragments.ScanFlow;

import android.nfc.Tag;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.example.healthinspector.Activities.MainActivity;
import com.example.healthinspector.CachedLists;
import com.example.healthinspector.Constants;
import com.example.healthinspector.Models.ScannedProduct;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.FragmentProductDetailsBinding;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class ProductDetailsFragment extends Fragment {

    private FragmentProductDetailsBinding binding;
    private static final String TAG = "ProductDetailsFragment";
    private HashMap<String, String> allAdditives = null;
    private HashMap<String, Integer> productRatings = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        ScannedProduct scannedProduct = (ScannedProduct) Parcels.unwrap(bundle.getParcelable(Constants.SCANNED_PRODUCT));

        binding.productNameTextView.setText(scannedProduct.getProductName());
        Glide.with(requireContext()).load(scannedProduct.getImageUrl()).into(binding.productImageView);

        productRatings = new HashMap<>();
        productRatings.put("A", 5);
        productRatings.put("B", 4);
        productRatings.put("C", 3);
        productRatings.put("D", 2);
        productRatings.put("E", 1);
        productRatings.put("", 0);
        binding.ratingBar.setRating(productRatings.get(scannedProduct.getHealthInspectorScore().toUpperCase(Locale.ROOT)));

        ArrayAdapter warningsAdapter = new ArrayAdapter<String>(requireContext(),
               android.R.layout.simple_list_item_1,scannedProduct.getNutrientLevels());
        binding.warningsListView.setAdapter(warningsAdapter);

        try {
            ArrayAdapter harmfulIngredientsAdapter = new ArrayAdapter<String>(requireContext(),
                    android.R.layout.simple_list_item_1,CachedLists.getInstance().additivesInProduct(scannedProduct.getProductAdditives(), requireContext()));
            binding.additivesListView.setAdapter(harmfulIngredientsAdapter);
        } catch (JSONException | JsonProcessingException e) {
            e.printStackTrace();
        }

        binding.btnRecommendations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                RecommendProductsFragment recommendProductsFragment = new RecommendProductsFragment();
                Bundle bundleSend = new Bundle();
                bundleSend.putParcelable(Constants.SCANNED_PRODUCT, Parcels.wrap(scannedProduct));
                recommendProductsFragment.setArguments(bundleSend);

                fragmentTransaction.replace(R.id.fragment_container, recommendProductsFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}