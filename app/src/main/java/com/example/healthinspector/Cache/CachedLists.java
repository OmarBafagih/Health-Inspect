package com.example.healthinspector.Cache;

import android.content.Context;
import android.util.Log;

import com.example.healthinspector.Constants;
import com.example.healthinspector.Models.Additive;
import com.example.healthinspector.Models.Allergen;
import com.example.healthinspector.Models.RecommendedProduct;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class CachedLists{

    private static CachedLists cachedLists = null;
    private static HashMap<String, String> additives = null;
    private static HashMap<String, String> allergens = null;
    private ArrayList<RecommendedProduct> homeRecommendedProducts = null;
    public static final String TAG = "CachedLists";

    private CachedLists(){}
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

    public static void setAdditives(HashMap<String, String> additives) {
        CachedLists.additives = additives;
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
        return new ObjectMapper().readValue(jsonString, LinkedHashMap.class);
    }

    public static void loadMostPopularWarnings(Context context, String filename, String filter, int queryLimit, Class c){
        File file = new File(context.getFilesDir(), filename);
        ParseQuery<ParseObject> allergensQuery = ParseQuery.getQuery(c);
        allergensQuery.setLimit(queryLimit);
        allergensQuery.addDescendingOrder(filter);
        allergensQuery.findInBackground((objects, e) -> {
            if(e != null){
                Log.e(TAG, "Error querying for most common allergens: " + e);
                return;
            }
            JSONObject popularAdditives = new JSONObject();
            try{
                for(int i = 0; i < objects.size(); i++){
                    if(c.equals(Additive.class)){
                        popularAdditives.put(String.valueOf(objects.get(i).get(Additive.ADDITIVE_KEY)), objects.get(i).get(Additive.ADDITIVE_VALUE));
                    }
                    else{
                        popularAdditives.put(String.valueOf(objects.get(i).get(Allergen.ALLERGEN_KEY)), objects.get(i).get(Allergen.ALLERGEN_VALUE));
                    }
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
            else{
                ParseQuery<Additive> additiveQuery = new ParseQuery<>(Additive.class);
                additiveQuery.whereEqualTo(Additive.ADDITIVE_KEY, productAdditiveTags.get(i));
                additiveQuery.findInBackground((objects, e) -> {
                    for(Additive additive: objects){
                        additivesInProduct.add(additive.getAdditiveValue());
                    }
                });
            }
        }
        return additivesInProduct;
    }

    public ArrayList<String> itemsNotInUser(ArrayList<String> userItems, Collection<String> warnings) throws JSONException, IOException{
        ArrayList<String> warningsNotInUser = new ArrayList<>(warnings);
        warningsNotInUser.removeAll(userItems);
        return warningsNotInUser;
    }
}
