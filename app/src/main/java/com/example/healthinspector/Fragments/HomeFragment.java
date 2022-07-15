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

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.healthinspector.Adapters.CartItemAdapter;
import com.example.healthinspector.Adapters.KrogerLocationAdapter;
import com.example.healthinspector.CachedLists;
import com.example.healthinspector.Cart;
import com.example.healthinspector.Constants;
import com.example.healthinspector.CreateRecommendations;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.Models.RecommendedProduct;
import com.example.healthinspector.Models.ScannedProduct;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.FragmentHomeBinding;
import com.example.healthinspector.databinding.FragmentSearchBinding;
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
import java.util.Random;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    public static final String TAG = "HomeFragment";
    private ArrayList<String> categories;
    private static final int CATEGORIES_COUNT = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //generating preset recommendations
        categories = new ArrayList<>();
        categories.add("snacks");
        categories.add("nuts");

        String url = Constants.URL_REQUEST_PRODUCTS;
        for(int i = 0; i < CATEGORIES_COUNT; i++){
            url+= String.format(Constants.CATEGORY_PARAMS, i, i, i, categories.get(i));
        }
        url += String.format("&tagtype_%d=nutrition_grades&tag_contains_%d=contains&tag_%d=A&additives=without&ingredients_from_palm_oil=without&json=true", CATEGORIES_COUNT, CATEGORIES_COUNT, CATEGORIES_COUNT);
        CreateRecommendations.getRecommendedProducts(new ScannedProduct(), url, requireContext(), FragmentSwitch.HOME_FRAGMENT, view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

