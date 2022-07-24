package com.example.healthinspector.Fragments.ScanFlow;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.SimpleExpandableListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.healthinspector.Cache.CachedLists;
import com.example.healthinspector.Constants;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.Models.ScannedProduct;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.FragmentProductDetailsBinding;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.json.JSONException;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;


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
        int layoutID = android.R.layout.simple_list_item_1;
        if((getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES){
            binding.harmfulIngredientsTextView.setTextColor(Color.WHITE);
            binding.productNameTextView.setTextColor(Color.WHITE);
            binding.warningsTextView.setTextColor(Color.WHITE);
            binding.healthInspectorScoreTextView.setTextColor(Color.WHITE);
            binding.productBreakdownTextView.setTextColor(Color.WHITE);
            layoutID = R.layout.text_list_item;
        }
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
        //TODO: create expandable list adapters for better experience
        try {
            ArrayAdapter harmfulIngredientsAdapter = new ArrayAdapter<>(requireContext(),
                    layoutID,CachedLists.getInstance().warningsInProduct(scannedProduct.getProductAdditives(), requireContext(), FragmentSwitch.ADDITIVE_SEARCH));
            binding.additivesListView.setAdapter(harmfulIngredientsAdapter);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        binding.btnRecommendations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction =  requireActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                RecommendProductsFragment recommendProductsFragment = new RecommendProductsFragment();
                Bundle bundleSend = new Bundle();
                bundleSend.putParcelable(Constants.SCANNED_PRODUCT, Parcels.wrap(scannedProduct));
                recommendProductsFragment.setArguments(bundleSend);
                fragmentTransaction.replace(R.id.fragment_container, recommendProductsFragment);
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