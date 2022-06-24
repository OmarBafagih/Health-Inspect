package com.example.healthinspector.Fragments.ScanFlow;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.example.healthinspector.Models.ScannedProduct;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.FragmentScanBinding;
import com.example.healthinspector.databinding.FragmentUserProfileBinding;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;


public class ScanFragment extends Fragment {

    private FragmentScanBinding binding;
    private CodeScanner codeScannerView;
    private final String URL = "https://world.openfoodfacts.org/api/v2/product/";
    private static final String TAG = "ScanFragment";
    private HashMap<Integer, String> novaGroups = new HashMap<>();
    @Override
    public void onStart() {
        super.onStart();
        //initialize Hashmap with novagroups
        novaGroups.put(1,"unprocessed or minimally processed food");
        novaGroups.put(2,"includes processed culinary ingredient");
        novaGroups.put(3,"processed food");
        novaGroups.put(4,"ultra processed food or drink product");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        binding = FragmentScanBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        final Activity activity = getActivity();
        CodeScannerView scannerView = binding.scannerView;
        codeScannerView = new CodeScanner(getActivity(), scannerView);

        //resets the scannerView to look for a new barcode
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                codeScannerView.startPreview();
            }
        });
        //if the camera permissions were granted, check the CodeScanner view for a barcode
        //initialize CodeScanner view
        codeScannerView.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //barcode is stored in result
                        //making request to OpenFoodFacts API here
                        Toast.makeText(getContext(), result.getText(), Toast.LENGTH_SHORT).show();
                        // Instantiate the RequestQueue.
                        RequestQueue queue = Volley.newRequestQueue(getContext());
                        // Request a JSON response from OpenFoodFacts API
                        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, "https://world.openfoodfacts.org/api/v2/product/7622210449283", null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                //Log.i(TAG, response.toString());
                                try {
                                    //URL + result.getText()
                                    //getting desired details from obtained product

                                    //getting product name
                                    String productName = response.getJSONObject("product").getString("product_name");
                                    //getting "nutriscore_grade"
                                    String healthInspectorScore = response.getJSONObject("product").getString("nutriscore_grade");
                                    //getting the list of ingredients
                                    ArrayList<String> ingredients = new ArrayList<>(Arrays.asList(response.getJSONObject("product").getString("ingredients_text_en").split(" ,")));
                                    ingredients.set(ingredients.size()-1, ingredients.get(ingredients.size()-1).replace(".", ""));
                                    //getting the ingredients analysis
                                    JSONArray ingredientsAnalysisJSON = response.getJSONObject("product").getJSONArray("ingredients_analysis_tags");
                                    ArrayList<String> ingredientsAnalysis = new ArrayList<>();
                                    //convert JSONarray to String arraylist
                                    for (int i = 0; i < ingredientsAnalysisJSON.length(); i++){
                                        ingredientsAnalysis.add(ingredientsAnalysisJSON.getString(i));
                                    }
                                    //parsing the string to remove the "en:" prefix
                                    for(int i = 0; i < ingredientsAnalysis.size(); i++){
                                        String analysis = "";
                                        for(int x = 3; x < ingredientsAnalysis.get(i).length(); x++){
                                            analysis += ingredientsAnalysis.get(i).charAt(x);
                                        }
                                        ingredientsAnalysis.set(i, analysis);
                                    }
                                    //getting novaGroup of product
                                    int novaGroupNumber = response.getJSONObject("product").getInt("nova_group");
                                    String novaGroup = novaGroups.get(novaGroupNumber);
                                    //  Log.i(TAG, novaGroup);

                                    //getting "nutrient_levels" for warning
                                    ArrayList<String> nutrientLevels = new ArrayList<>();
                                    JSONObject nutrientLevelsJSON = response.getJSONObject("product").getJSONObject("nutrient_levels");
                                    Iterator iterator =  nutrientLevelsJSON.keys();
                                    while (iterator.hasNext()){
                                        nutrientLevels.add(nutrientLevelsJSON.getString(iterator.next().toString()));
                                    }
                                    //send to next screen and populate views
                                    //creating a scannedProduct object for to wrap with
                                    ScannedProduct scannedProduct = new ScannedProduct(productName, healthInspectorScore, ingredients, ingredientsAnalysis, novaGroup, nutrientLevels);
                                    
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, "error finding product", error);
                            }
                        });
                        // Add the request to the RequestQueue.
                        queue.add(objectRequest);
                    }
                });
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onResume() {
        super.onResume();
        codeScannerView.startPreview();
    }

    @Override
    public void onPause() {
       codeScannerView.releaseResources();
        super.onPause();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}