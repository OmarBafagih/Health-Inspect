package com.example.healthinspector.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.healthinspector.Adapters.CartItemAdapter;
import com.example.healthinspector.Cart;
import com.example.healthinspector.Constants;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.Models.RecommendedProduct;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.FragmentCartBinding;
import com.example.healthinspector.databinding.FragmentSearchBinding;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;


public class CartFragment extends Fragment {

    private FragmentCartBinding binding;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayList<RecommendedProduct> cartItems = new ArrayList<>();
        ParseQuery<Cart> parseQuery = ParseQuery.getQuery(Cart.class);
        try {
            Cart userCart = parseQuery.get(ParseUser.getCurrentUser().getParseObject(Constants.CART).getObjectId());
            for(int i = 0; i < userCart.getCartItems().length(); i++){
                //create new recommendedProduct object and add to arraylist
                String keyWords = userCart.getCartItems().getJSONObject(i).getString(Constants.KEYWORDS);
                String productImageUrl = userCart.getCartItems().getJSONObject(i).getString(Constants.PRODUCT_IMAGE_URL);
                String brand = userCart.getCartItems().getJSONObject(i).getString(Constants.BRAND);
                String productName = userCart.getCartItems().getJSONObject(i).getString(Constants.PRODUCT_NAME);
                JSONArray nutrientLevelsJSON = userCart.getCartItems().getJSONObject(i).getJSONArray(Constants.NUTRIENT_LEVELS);
                ArrayList<String> nutrientLevels = new ArrayList<>();
                for(int x = 0; x < nutrientLevelsJSON.length(); x++){
                    nutrientLevels.add((String) nutrientLevelsJSON.get(x));
                }
                cartItems.add(new RecommendedProduct(keyWords, brand, productName, productImageUrl, nutrientLevels));
            }
            CartItemAdapter cartAdapter = new CartItemAdapter(requireContext(), cartItems, FragmentSwitch.CART);
            binding.cartRecyclerView.setAdapter(cartAdapter);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
            binding.cartRecyclerView.setLayoutManager(linearLayoutManager);
        } catch (ParseException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}