package com.example.healthinspector;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.healthinspector.Adapters.CartItemAdapter;
import com.example.healthinspector.Cache.CachedLists;
import com.example.healthinspector.Models.Cart;
import com.example.healthinspector.Models.RecommendedProduct;
import com.example.healthinspector.Models.ScannedProduct;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CreateRecommendations extends Application {
    public static final String TAG = "CreateRecommendations";
    private static final Integer KEYWORD_LIMIT = 2;
    private static final Integer RECOMMENDED_ITEMS_LIMIT = 7;
    private static final Integer INITIAL_TIMEOUT = 7000;
    private static final Integer MAX_TRIES = 3;
    private static final String BRAND = "brands";
    private static final String KEYWORDS = "_keywords";
    private static ProgressBar progressBar;

    public static void getRecommendedProducts(ScannedProduct scannedProduct, String url, Context context, FragmentSwitch fragmentSwitch, View v) throws JSONException, JsonProcessingException, ParseException {
        ArrayList<RecommendedProduct> homeRecommendedProducts = CachedLists.getInstance().getHomeRecommendedProducts();
        if (homeRecommendedProducts != null && fragmentSwitch.equals(FragmentSwitch.HOME_FRAGMENT)) {
            loadRecommendationsIntoView(homeRecommendedProducts, fragmentSwitch, v, context, scannedProduct);
            progressBar = v.findViewById(R.id.homeProgressBar);
            progressBar.setVisibility(View.GONE);
            return;
        }
        ArrayList<RecommendedProduct> recommendedProducts = new ArrayList<>();
        //new volley request to get product recommendations
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest recommendedProductsRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    int count = 0;
                    try {
                        count = response.getInt(Constants.COUNT);
                    } catch (JSONException e) {
                        Toast.makeText(context, context.getString(R.string.error_finding_recommendations), Toast.LENGTH_SHORT).show();
                        Log.e(TAG,"JSON Exception tying to retrieve recommendedProducts", e);
                        e.printStackTrace();
                    }
                    if (count == 0) {
                        Toast.makeText(context, context.getString(R.string.error_finding_recommendations), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (int i = 0; i < Integer.min(RECOMMENDED_ITEMS_LIMIT, count); i++) {
                        String productName = "";
                        String brand = "";
                        String keywords = "";
                        String imageUrl = "";
                        ArrayList<String> nutrientLevels = new ArrayList<>();
                        try {
                            JSONObject recommendedItemJson = response.getJSONArray(Constants.PRODUCTS).getJSONObject(i);
                            productName = recommendedItemJson.getString(Constants.PRODUCT_NAME);

                            if (recommendedItemJson.has(BRAND)) {
                                brand = recommendedItemJson.getString(BRAND);
                            }

                            StringBuilder keywordsBuilder = new StringBuilder();
                            JSONArray productKeywords = recommendedItemJson.getJSONArray(KEYWORDS);
                            for (int x = 0; x < Integer.min(KEYWORD_LIMIT, productKeywords.length()); x++) {
                                keywordsBuilder.append(productKeywords.getString(x)).append("%20");
                            }
                            keywords = keywordsBuilder.toString();

                            imageUrl = recommendedItemJson.getString(Constants.PRODUCT_IMAGE);
                            if (imageUrl == null) {
                                imageUrl = "";
                            }
                            if (recommendedItemJson.has(Constants.NUTRIENT_LEVELS)) {
                                JSONObject nutrientLevelsJSON = recommendedItemJson.getJSONObject(Constants.NUTRIENT_LEVELS);
                                Iterator iterator = nutrientLevelsJSON.keys();
                                while (iterator.hasNext()) {
                                    String key = iterator.next().toString();
                                    nutrientLevels.add(key + ": " + nutrientLevelsJSON.getString(key));
                                }
                            }
                        } catch (JSONException e) {
                            //no need to toast here
                            Log.e(TAG,"JSON Exception tying to retrieve recommendedProducts: " + e);
                            e.printStackTrace();
                        }
                        recommendedProducts.add(new RecommendedProduct(keywords, brand, productName, imageUrl, nutrientLevels));
                    }

                    if (fragmentSwitch.equals(FragmentSwitch.RECOMMENDATIONS)) {
                        progressBar = v.findViewById(R.id.progressBar);
                    } else {
                        progressBar = v.findViewById(R.id.homeProgressBar);
                    }
                    progressBar.setVisibility(View.GONE);
                    try {
                        CachedLists.getInstance().setHomeRecommendedProducts(recommendedProducts);
                        loadRecommendationsIntoView(recommendedProducts, fragmentSwitch, v, context, scannedProduct);
                    } catch (ParseException | JsonProcessingException  | JSONException e) {
                        //if there is an exception above, the exception will also get caught here, don't want to toast again
                        Log.e(TAG, "ParseException trying to get recommended products " + e);
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.d(TAG, "Error requesting recommendations=> " + error.toString());
                    Toast.makeText(context, context.getString(R.string.error_finding_recommendations), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put(Constants.PARAM_KEY, Constants.PARAM_VALUE);
                return params;
            }
        };
        //allowing the Volley request for recommended products to retry after a timeout error
        recommendedProductsRequest.setRetryPolicy(new DefaultRetryPolicy(INITIAL_TIMEOUT, MAX_TRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(recommendedProductsRequest);
    }

    public static void loadRecommendationsIntoView(ArrayList<RecommendedProduct> recommendedProducts, FragmentSwitch fragmentSwitch, View v, Context context, ScannedProduct scannedProduct) throws ParseException, JSONException {
        ParseQuery<Cart> cartQuery = ParseQuery.getQuery(Cart.class);
        Cart userCart = cartQuery.get(ParseUser.getCurrentUser().getParseObject(Constants.CART).getObjectId());
        //cache query
        //removing any recommended items that are already in the user's cart
        for(int i = 0; i < userCart.getCartItems().length(); i++){
            for(int x = 0; x < recommendedProducts.size(); x++) {
                if (userCart.getCartItems().getJSONObject(i).getString(Constants.PRODUCT_NAME).equals(recommendedProducts.get(x).getProductName())){
                    recommendedProducts.remove(x);
                }
            }
        }
        RecyclerView recommendedProductsRecyclerView;
        LinearLayoutManager linearLayoutManager = null;
        if(fragmentSwitch.equals(FragmentSwitch.RECOMMENDATIONS)){
            recommendedProductsRecyclerView = v.findViewById(R.id.recommendedProductsRecyclerView);
            linearLayoutManager = new LinearLayoutManager(context);
        }
        else{
            recommendedProductsRecyclerView = v.findViewById(R.id.homeRecommendationsRecyclerView);
            linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        }
        recommendedProductsRecyclerView.setLayoutManager(linearLayoutManager);
        recommendedProductsRecyclerView.setAdapter(new CartItemAdapter(context, recommendedProducts, scannedProduct, fragmentSwitch));
    }
}
