package com.example.healthinspector.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import org.json.JSONObject;

import java.util.ArrayList;


public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private static final String TAG = "CartFragment";

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
                JSONObject cartItem = userCart.getCartItems().getJSONObject(i);
                JSONArray nutrientLevelsJSON = cartItem.getJSONArray(Constants.NUTRIENT_LEVELS);
                ArrayList<String> nutrientLevels = new ArrayList<>();
                for(int x = 0; x < nutrientLevelsJSON.length(); x++){
                    nutrientLevels.add((String) nutrientLevelsJSON.get(x));
                }
                cartItems.add(new RecommendedProduct(cartItem.getString(Constants.KEYWORDS), cartItem.getString(Constants.BRAND),
                        cartItem.getString(Constants.PRODUCT_NAME), cartItem.getString(Constants.PRODUCT_IMAGE_URL), nutrientLevels));
            }
            binding.cartRecyclerView.setAdapter(new CartItemAdapter(requireContext(), cartItems, FragmentSwitch.CART));
            binding.cartRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        } catch (ParseException | JSONException e) {
            Toast.makeText(requireContext(), getString(R.string.error_displaying_cart), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error retrieving user's cart" + e);
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}