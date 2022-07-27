package com.example.healthinspector.Fragments.ScanFlow;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.healthinspector.Adapters.WarningExpandableAdapter;
import com.example.healthinspector.Cache.CachedLists;
import com.example.healthinspector.Constants;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.Models.ScannedProduct;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.FragmentProductDetailsBinding;
import com.parse.ParseUser;

import org.json.JSONException;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;


public class ProductDetailsFragment extends Fragment {

    private FragmentProductDetailsBinding binding;
    private static final String TAG = "ProductDetailsFragment";
    private HashMap<String, Integer> productRatings = null;
    LinkedHashMap<String, List<String>> productDetailsList;
    ArrayList<String> additivesInProduct;
    ArrayList<String> allergensInProduct;
    ArrayList<String> warningsInProduct;
    public static final String NUTRIENT_LEVELS = "Nutrient Levels";
    public static final String ADDITIVES_IN_PRODUCT = "Additives in this product";
    public static final String PRODUCT_INGREDIENTS = "Ingredients";
    public static final String INGREDIENTS_ANALYSIS = "Ingredients Analysis";

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
        warningsInProduct = new ArrayList<>();
        productDetailsList = new LinkedHashMap<>();
        productRatings = new HashMap<>();
        productRatings.put("A", 5);
        productRatings.put("B", 4);
        productRatings.put("C", 3);
        productRatings.put("D", 2);
        productRatings.put("E", 1);
        productRatings.put("", 0);

        if((requireContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES){
            binding.productNameTextView.setTextColor(Color.WHITE);
            binding.healthInspectorScoreTextView.setTextColor(Color.WHITE);
            binding.productBreakdownTextView.setTextColor(Color.WHITE);
            binding.novaGroupTextView.setTextColor(Color.WHITE);
        }
        binding.productNameTextView.setText(scannedProduct.getProductName());
        binding.novaGroupTextView.setText(scannedProduct.getNovaGroup());
        Glide.with(requireContext()).load(scannedProduct.getImageUrl()).into(binding.productImageView);
        binding.ratingBar.setRating(productRatings.get(scannedProduct.getHealthInspectorScore().toUpperCase(Locale.ROOT)));

        try {
            allergensInProduct = CachedLists.getInstance().warningsInProduct(scannedProduct.getAllergens(), requireContext(), FragmentSwitch.ALLERGEN_SEARCH);
            additivesInProduct = CachedLists.getInstance().warningsInProduct(scannedProduct.getAdditives(), requireContext(), FragmentSwitch.ADDITIVE_SEARCH);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        warningsInProduct.addAll(allergensInProduct);
        warningsInProduct.addAll(additivesInProduct);
        productDetailsList.put(Constants.WARNINGS_IN_PRODUCT, userWarningsInProduct(warningsInProduct));
        productDetailsList.put(NUTRIENT_LEVELS, scannedProduct.getNutrientLevels());
        productDetailsList.put(PRODUCT_INGREDIENTS, scannedProduct.getIngredients());
        productDetailsList.put(INGREDIENTS_ANALYSIS, scannedProduct.getIngredientsAnalysis());
        productDetailsList.put(ADDITIVES_IN_PRODUCT, additivesInProduct);
        binding.additivesListView.setAdapter(new WarningExpandableAdapter(requireContext(), new ArrayList<>(productDetailsList.keySet()), productDetailsList));

        binding.btnRecommendations.setOnClickListener(v -> {
            FragmentTransaction fragmentTransaction =  requireActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            RecommendProductsFragment recommendProductsFragment = new RecommendProductsFragment();
            Bundle bundleSend = new Bundle();
            bundleSend.putParcelable(Constants.SCANNED_PRODUCT, Parcels.wrap(scannedProduct));
            recommendProductsFragment.setArguments(bundleSend);
            fragmentTransaction.replace(R.id.fragment_container, recommendProductsFragment);
            fragmentTransaction.commit();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public ArrayList<String> userWarningsInProduct(ArrayList<String> productWarnings){
        ArrayList<String> userWarningsInProduct = new ArrayList<>();
        ArrayList<String> userWarnings = new ArrayList<>();
        userWarnings.addAll((ArrayList) ParseUser.getCurrentUser().get(Constants.PARSE_USER_WARNINGS));
        userWarnings.addAll((ArrayList) ParseUser.getCurrentUser().get(Constants.PARSE_USER_ALLERGIES));
        for(int i = 0; i < productWarnings.size(); i++){
            String productWarning = productWarnings.get(i);
            if(userWarnings.contains(productWarning)){
                userWarningsInProduct.add(productWarning);
            }
        }
        return userWarningsInProduct;
    }
}