package com.example.healthinspector.Fragments.ScanFlow;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.healthinspector.Constants;
import com.example.healthinspector.CreateRecommendations;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.Models.ScannedProduct;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.FragmentRecommendProductsBinding;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.parse.ParseException;

import org.json.JSONException;
import org.parceler.Parcels;


public class RecommendProductsFragment extends Fragment {

    private FragmentRecommendProductsBinding binding;
    private static final String TAG = "RecProductFragment";
    private ScannedProduct scannedProduct;
    private static final Integer CATEGORY_LIMIT = 4;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRecommendProductsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        scannedProduct = (ScannedProduct) Parcels.unwrap(bundle.getParcelable(Constants.SCANNED_PRODUCT));
        String url = Constants.URL_REQUEST_PRODUCTS;
        int categoriesCount = Integer.min(CATEGORY_LIMIT, scannedProduct.getCategories().size());
        //if there are no categories, then no recommendations can be made for this scanned product
        if(categoriesCount == 0){
            returnToProductDetails(scannedProduct);
        }
        for(int i = 0; i < categoriesCount; i++){
            url += String.format(Constants.CATEGORY_PARAMS, i, i, i, scannedProduct.getCategories().get(i));
        }
        url += String.format("&tagtype_%d=nutrition_grades&tag_contains_%d=contains&tag_%d=A&additives=without&ingredients_from_palm_oil=without&json=true", categoriesCount, categoriesCount, categoriesCount);
        try {
            CreateRecommendations.getRecommendedProducts(scannedProduct, url, requireContext(), FragmentSwitch.RECOMMENDATIONS, view);
        } catch (JSONException | JsonProcessingException | ParseException e) {
            //toast already gets called for these exceptions when creatingProductRecommendations, do not want to show the toast twice
            e.printStackTrace();
            Log.e(TAG,"JSON Exception tying to retrieve recommendedProducts", e);
        }
    }

    public void returnToProductDetails(ScannedProduct scannedProduct){
        Toast.makeText(requireContext(), getString(R.string.no_recommendations), Toast.LENGTH_SHORT).show();
        FragmentTransaction fragmentTransaction =  requireActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.SCANNED_PRODUCT, Parcels.wrap(scannedProduct));
        productDetailsFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.fragment_container, productDetailsFragment);
        fragmentTransaction.commit();
    }
}