package com.example.healthinspector.Cache;

import android.content.Context;
import android.nfc.Tag;
import android.util.Log;
import android.widget.Toast;

import com.example.healthinspector.Constants;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.Models.Additive;
import com.example.healthinspector.Models.RecommendedProduct;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class CachedLists{

    private static CachedLists cachedLists = null;
    private static HashMap<String, String> additives = null;
    private static HashMap<String, String> allergens = null;
    private ArrayList<RecommendedProduct> homeRecommendedProducts = null;
    public static final String TAG = "CachedLists";

    private CachedLists() throws JSONException, JsonProcessingException {}
    public static CachedLists getInstance() throws JSONException, JsonProcessingException {
        if (cachedLists == null){
            cachedLists = new CachedLists();
        }
        return cachedLists;
    }

    public static HashMap<String, String> getAdditives(Context context) throws JSONException, IOException {
        if(additives == null){
            additives = readJsonFromFile(context, Constants.ADDITIVES_FILE_NAME);
        }
        return additives;
    }

    public static HashMap<String, String> getAllergens(Context context) throws JSONException, IOException {
        if(allergens == null){
            allergens = readJsonFromFile(context, Constants.ALLERGENS_FILE_NAME);
        }
        return allergens;
    }
    public void setHomeRecommendedProducts(ArrayList<RecommendedProduct> homeRecommendedProducts) {
        this.homeRecommendedProducts = homeRecommendedProducts;
    }

    public ArrayList<RecommendedProduct> getHomeRecommendedProducts() {
        return homeRecommendedProducts;
    }
    public static HashMap<String,String> readJsonFromFile(Context context, String fileName) throws IOException {
        File file = new File(context.getFilesDir(), fileName);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        StringBuilder stringBuilder = new StringBuilder();
        String line = bufferedReader.readLine();
        while (line != null){
            stringBuilder.append(line).append("\n");
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        String jsonString = stringBuilder.toString();
        return new ObjectMapper().readValue(jsonString, HashMap.class);
    }

    public static void loadMostPopularAdditives(Context context){
        File file = new File(context.getFilesDir(),Constants.ADDITIVES_FILE_NAME);
        ParseQuery<Additive> additivesQuery = ParseQuery.getQuery(Additive.class);
        additivesQuery.setLimit(50);
        additivesQuery.addDescendingOrder(Additive.USES_KEY);
        additivesQuery.findInBackground((objects, e) -> {
            if(e != null){
                Log.e(TAG, "Error querying for most common additives: " + e);
                return;
            }
            JSONObject popularAdditives = new JSONObject();
            try{
                for(int i = 0; i < objects.size(); i++){
                    popularAdditives.put(objects.get(i).getAdditiveKey(), objects.get(i).getAdditiveValue());
                }
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
                bufferedWriter.write(popularAdditives.toString());
                bufferedWriter.close();
            } catch(IOException | JSONException ex){
                ex.printStackTrace();
                Log.e(TAG, "JSONException encountered while querying for most common additives: " + ex);
            }
        });
    }

    public ArrayList<String> additivesInProduct(ArrayList<String> productAdditiveTags, Context context) throws JSONException, IOException {
        ArrayList<String> additivesInProduct = new ArrayList<>();
        if(productAdditiveTags.size() == 0){
            return additivesInProduct;
        }
        for(int i = 0; i < productAdditiveTags.size(); i++){
            String additiveFromCache = null;
            additiveFromCache = getAdditives(context).get(productAdditiveTags.get(i));
            if(additiveFromCache != null){
                additivesInProduct.add(additiveFromCache);
            }
        }
        return additivesInProduct;
    }

    public ArrayList<String> itemsNotInUser(ArrayList<String> userItems, FragmentSwitch fragmentSwitch, Context context) throws JSONException, IOException{
        Collection<String> allValues;
        if(fragmentSwitch.equals(FragmentSwitch.ALLERGEN_SEARCH)){
            allValues = CachedLists.getInstance().getAllergens(context).values();
        }
        else{
            allValues = CachedLists.getInstance().getAdditives(context).values();
        }
        ArrayList<String> additivesNotInUser = new ArrayList<>(allValues);
        additivesNotInUser.removeAll(userItems);
        return additivesNotInUser;
    }
}
