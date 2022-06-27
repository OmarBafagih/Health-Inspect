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
import androidx.fragment.app.FragmentTransaction;

import android.os.Parcelable;
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
import com.example.healthinspector.Constants;
import com.example.healthinspector.Models.ScannedProduct;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.FragmentScanBinding;
import com.example.healthinspector.databinding.FragmentUserProfileBinding;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.lang.reflect.Array;
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
                        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, Constants.productRequestURL + result.getText(), null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String productName = response.getJSONObject(Constants.product).getString(Constants.productName);

                                    String healthInspectorScore = response.getJSONObject(Constants.product).getString(Constants.nutriScore);

                                    ArrayList<String> ingredients = new ArrayList<>(Arrays.asList(response.getJSONObject(Constants.product).getString(Constants.ingredientsList).split(" ,")));
                                    ingredients.set(ingredients.size()-1, ingredients.get(ingredients.size()-1).replace(".", ""));

                                    JSONArray ingredientsAnalysisJSON = response.getJSONObject(Constants.product).getJSONArray(Constants.ingredientsAnalysis);
                                    ArrayList<String> ingredientsAnalysis = new ArrayList<>();

                                    for (int i = 0; i < ingredientsAnalysisJSON.length(); i++){
                                        ingredientsAnalysis.add(ingredientsAnalysisJSON.getString(i));
                                    }
                                    for(int i = 0; i < ingredientsAnalysis.size(); i++){
                                        ingredientsAnalysis.set(i, ingredientsAnalysis.get(i).substring(3).replace("-", " "));
                                    }
                                    if(response.getJSONObject(Constants.product).has(Constants.novaGroup)){
                                        int novaGroupNumber = response.getJSONObject(Constants.product).getInt(Constants.novaGroup);
                                        novaGroup = novaGroups.get(novaGroupNumber);
                                    }

                                    ArrayList<String> nutrientLevels = new ArrayList<>();
                                    JSONObject nutrientLevelsJSON = response.getJSONObject(Constants.product).getJSONObject(Constants.nutrientLevels);
                                    Iterator iterator =  nutrientLevelsJSON.keys();
                                    while (iterator.hasNext()){
                                        String key = iterator.next().toString();
                                        nutrientLevels.add(key + ": " + nutrientLevelsJSON.getString(key));
                                    }
                                    ScannedProduct scannedProduct = new ScannedProduct(productName, healthInspectorScore, ingredients, ingredientsAnalysis, novaGroup, nutrientLevels);

                                    FragmentTransaction fragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
                                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                                    ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable(Constants.scannedProduct, Parcels.wrap(scannedProduct));
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

}