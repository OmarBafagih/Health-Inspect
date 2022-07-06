package com.example.healthinspector.Fragments.ScanFlow;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.healthinspector.Adapters.CartItemAdapter;
import com.example.healthinspector.Adapters.ItemAdapter;
import com.example.healthinspector.Constants;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.Models.RecommendedProduct;
import com.example.healthinspector.Models.ScannedProduct;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.FragmentProductDetailsBinding;
import com.example.healthinspector.databinding.FragmentRecommendProductsBinding;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class RecommendProductsFragment extends Fragment {

    private FragmentRecommendProductsBinding binding;
    private static final String TAG = "RecommendProductsFragment";
    private static final String URL_REQUEST_PRODUCTS = "https://us.openfoodfacts.org/cgi/search.pl?action=process";
    private static final String PRODUCTS = "products";
    private static final String COUNT = "count";
    private static final String BRAND = "brands";
    private static final String KEYWORDS = "_keywords";
    private static final String PARAM_KEY = "User-Agent";
    private static final String PARAM_VALUE = "Health Inspect - Android - Version 1.0";

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
        ScannedProduct scannedProduct = (ScannedProduct) Parcels.unwrap(bundle.getParcelable(Constants.SCANNED_PRODUCT));
        getRecommendedProducts(scannedProduct);
    }

    public void getRecommendedProducts(ScannedProduct scannedProduct){
        ArrayList<RecommendedProduct> recommendedProducts = new ArrayList<>();
        String url = "";
        url += URL_REQUEST_PRODUCTS;
        //limiting the categories to 4, since 5 is too much and most of the time won't give back results
        int categoriesCount = scannedProduct.getCategories().size();
        if(categoriesCount == 0){returnToProductDetails(scannedProduct);} //if there are no categories, then no recommendations can be made for this scanned product
        if(categoriesCount > 4){categoriesCount = 4;}
        for(int i = 0; i < categoriesCount; i++){
            String category = "&" + "tagtype_" + i + "=categories&tag_contains_" + i + "=contains&tag_" + i + "=" + scannedProduct.getCategories().get(i);
            url+= category;
        }

        String healthyProductParams = "&tagtype_" + (categoriesCount) + "=nutrition_grades&tag_contains_" + (categoriesCount) + "=contains&tag_" + (categoriesCount) + "=A&additives=without&ingredients_from_palm_oil=without&json=true";
        url += healthyProductParams;
        //new volley request to get product recommendations
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest recommendedProductsRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int count = 0;
                        try {
                            count = response.getInt(COUNT);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //if the response has 0 products, return to details
                        if(count == 0){
                            returnToProductDetails(scannedProduct);
                            return;
                        }
                        //limiting to 10 different recommendations
                        if(count > 10){count = 10;}
                        for(int i = 0; i < count; i++){
                            String productName = "";
                            String brand = "";
                            String keywords = "";
                            String imageUrl = "";
                            ArrayList<String> nutrientLevels = new ArrayList<>();
                            try {
                                productName = response.getJSONArray(PRODUCTS).getJSONObject(i).getString(Constants.PRODUCT_NAME);

                                if(response.getJSONArray(PRODUCTS).getJSONObject(i).has(BRAND)){
                                    brand = response.getJSONArray(PRODUCTS).getJSONObject(i).getString(BRAND);
                                }

                                StringBuilder keywordsBuilder =new StringBuilder();
                                JSONArray productKeywords = response.getJSONArray(PRODUCTS).getJSONObject(i).getJSONArray(KEYWORDS);
                                ArrayList<String> keywordsArray = new ArrayList<>();
                                int keywordCount = productKeywords.length();
                                //kroger API filter for product terms cannot be more than 8 words
                                if(keywordCount > 8){
                                    keywordCount = 8;
                                }
                                for (int x = 0; x < keywordCount; x++){
                                    keywordsArray.add(productKeywords.getString(x));
                                }
                                for(String str : keywordsArray) {
                                    keywordsBuilder.append(str);
                                    keywordsBuilder.append(" ");
                                }
                                keywords = keywordsBuilder.toString();

                                if(response.getJSONArray(PRODUCTS).getJSONObject(i).has(Constants.PRODUCT_IMAGE)){
                                    imageUrl = response.getJSONArray(PRODUCTS).getJSONObject(i).getString(Constants.PRODUCT_IMAGE);
                                }

                                if(response.getJSONArray(PRODUCTS).getJSONObject(i).has(Constants.NUTRIENT_LEVELS)){
                                    JSONObject nutrientLevelsJSON = response.getJSONArray(PRODUCTS).getJSONObject(i).getJSONObject(Constants.NUTRIENT_LEVELS);
                                    Iterator iterator =  nutrientLevelsJSON.keys();
                                    while (iterator.hasNext()){
                                        String key = iterator.next().toString();
                                        nutrientLevels.add(key + ": " + nutrientLevelsJSON.getString(key));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            recommendedProducts.add(new RecommendedProduct(keywords, brand, productName, imageUrl, nutrientLevels));
                        }
                        binding.progressBar.setVisibility(View.GONE);
                        loadRecommendationsIntoView(recommendedProducts);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "error => " + error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put(PARAM_KEY, PARAM_VALUE);
                return params;
            }
        };
        recommendedProductsRequest.setRetryPolicy(new DefaultRetryPolicy(7000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(recommendedProductsRequest);
    }

    public void loadRecommendationsIntoView(ArrayList<RecommendedProduct> recommendedProducts){
        //load products into recycler view
        CartItemAdapter recommendationsAdapter = new CartItemAdapter(requireContext(), recommendedProducts);
        binding.recommendedProductsRecyclerView.setAdapter(recommendationsAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        binding.recommendedProductsRecyclerView.setLayoutManager(linearLayoutManager);
    }

    public void returnToProductDetails(ScannedProduct scannedProduct){
        Toast.makeText(requireContext(), getString(R.string.no_recommendations), Toast.LENGTH_SHORT).show();
        FragmentTransaction fragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.SCANNED_PRODUCT, Parcels.wrap(scannedProduct));
        productDetailsFragment.setArguments(bundle);

        fragmentTransaction.replace(R.id.fragment_container, productDetailsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}