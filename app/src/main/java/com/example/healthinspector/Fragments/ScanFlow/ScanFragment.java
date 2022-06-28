package com.example.healthinspector.Fragments.ScanFlow;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
import com.android.volley.toolbox.Volley;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.example.healthinspector.Constants;
import com.example.healthinspector.Models.ScannedProduct;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.FragmentScanBinding;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;


public class ScanFragment extends Fragment {

    private FragmentScanBinding binding;
    private CodeScanner codeScannerView;
    private static final String TAG = "ScanFragment";
    private HashMap<Integer, String> novaGroups = new HashMap<>();
    private String novaGroup;
    @Override
    public void onStart() {
        super.onStart();
        novaGroups.put(1,"unprocessed or minimally processed food");
        novaGroups.put(2,"includes processed culinary ingredient");
        novaGroups.put(3,"processed food");
        novaGroups.put(4,"ultra processed food or drink product");
        novaGroup = "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentScanBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        final Activity activity = getActivity();
        CodeScannerView scannerView = binding.scannerView;
        codeScannerView = new CodeScanner(getActivity(), scannerView);

        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                codeScannerView.startPreview();
            }
        });
        codeScannerView.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(requireContext(), result.getText(), Toast.LENGTH_SHORT).show();
                        RequestQueue queue = Volley.newRequestQueue(getContext());
                        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, Constants.PRODUCT_REQUEST_URL + result.getText(), null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String productName = response.getJSONObject(Constants.PRODUCT).getString(Constants.PRODUCT_NAME);
                                    String healthInspectorScore = "";
                                    if(response.getJSONObject(Constants.PRODUCT).has(Constants.NUTRISCORE_GRADE)){
                                        healthInspectorScore = response.getJSONObject(Constants.PRODUCT).getString(Constants.NUTRISCORE_GRADE);
                                    }
                                    ArrayList<String> ingredients = new ArrayList<>();
                                    if(response.getJSONObject(Constants.PRODUCT).has(Constants.INGREDIENTS_LIST)){
                                        ingredients = new ArrayList<>(Arrays.asList(response.getJSONObject(Constants.PRODUCT).getString(Constants.INGREDIENTS_LIST).split(" ,")));
                                        ingredients.set(ingredients.size()-1, ingredients.get(ingredients.size()-1).replace(".", ""));
                                    }

                                    ArrayList<String> ingredientsAnalysis = new ArrayList<>();
                                    if(response.getJSONObject(Constants.PRODUCT).has(Constants.INGREDIENTS_ANALYSIS)){
                                        JSONArray ingredientsAnalysisJSON = response.getJSONObject(Constants.PRODUCT).getJSONArray(Constants.INGREDIENTS_ANALYSIS);
                                        for (int i = 0; i < ingredientsAnalysisJSON.length(); i++){
                                            ingredientsAnalysis.add(ingredientsAnalysisJSON.getString(i));
                                        }
                                        for(int i = 0; i < ingredientsAnalysis.size(); i++){
                                            ingredientsAnalysis.set(i, ingredientsAnalysis.get(i).substring(3).replace("-", " "));
                                        }
                                    }

                                    if(response.getJSONObject(Constants.PRODUCT).has(Constants.NOVA_GROUP)){
                                        int novaGroupNumber = response.getJSONObject(Constants.PRODUCT).getInt(Constants.NOVA_GROUP);
                                        novaGroup = novaGroups.get(novaGroupNumber);
                                    }

                                    ArrayList<String> nutrientLevels = new ArrayList<>();
                                    if(response.getJSONObject(Constants.PRODUCT).has(Constants.NUTRIENT_LEVELS)){
                                        JSONObject nutrientLevelsJSON = response.getJSONObject(Constants.PRODUCT).getJSONObject(Constants.NUTRIENT_LEVELS);
                                        Iterator iterator =  nutrientLevelsJSON.keys();
                                        while (iterator.hasNext()){
                                            String key = iterator.next().toString();
                                            nutrientLevels.add(key + ": " + nutrientLevelsJSON.getString(key));
                                        }
                                    }
                                    String imageUrl = "";
                                    if(response.getJSONObject(Constants.PRODUCT).has(Constants.PRODUCT_IMAGE)){
                                        imageUrl = response.getJSONObject(Constants.PRODUCT).getString(Constants.PRODUCT_IMAGE);
                                    }

                                    ArrayList<String> additives = new ArrayList<>();
                                    if(response.getJSONObject(Constants.PRODUCT).has(Constants.ALLERGENS)){
                                        JSONArray additivesJSON = response.getJSONObject(Constants.PRODUCT).getJSONArray(Constants.ADDITIVES);
                                        for (int i = 0; i < additivesJSON.length(); i++){
                                            ingredientsAnalysis.add(additivesJSON.getString(i));
                                        }
                                    }
                                    ArrayList<String> allergens = new ArrayList<>();
                                    if(response.getJSONObject(Constants.PRODUCT).has(Constants.ALLERGENS)){
                                       allergens = new ArrayList<>(Arrays.asList(response.getJSONObject(Constants.PRODUCT).getString(Constants.ALLERGENS).split(",")));
                                    }

                                    ScannedProduct scannedProduct = new ScannedProduct(productName, healthInspectorScore, ingredients, ingredientsAnalysis, novaGroup, nutrientLevels, imageUrl , additives, allergens);

                                    FragmentTransaction fragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
                                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                                    ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable(Constants.SCANNED_PRODUCT, Parcels.wrap(scannedProduct));
                                    productDetailsFragment.setArguments(bundle);

                                    fragmentTransaction.replace(R.id.fragment_container, productDetailsFragment);
                                    fragmentTransaction.addToBackStack(null);
                                    fragmentTransaction.commit();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, "error finding product", error);
                                Toast.makeText(requireContext(), "error finding product with barcode: " + result.getText(), Toast.LENGTH_SHORT).show();
                            }
                        });
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

    public JSONObject loadAdditives(){

        return null;
    }

    public JSONObject loadAllergens(){

        return null;
    }


}