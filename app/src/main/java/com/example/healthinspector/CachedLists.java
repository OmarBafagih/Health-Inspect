package com.example.healthinspector;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class CachedLists{

    private static CachedLists cachedLists = null;
    private HashMap<String, String> additives = null;
    private HashMap<String, String> allergens = null;

    private CachedLists() throws JSONException, JsonProcessingException {
    }
    public static CachedLists getInstance() throws JSONException, JsonProcessingException {
        if (cachedLists == null){
            cachedLists = new CachedLists();
        }
        return cachedLists;
    }

    public HashMap<String, String> getAdditives(Context context) throws JSONException, JsonProcessingException {
        if(additives == null){
            additives = loadJSONObject(context, Constants.ADDITIVES_FILE_NAME);
        }
        return additives;
    }

    public HashMap<String, String> getAllergens(Context context) throws JSONException, JsonProcessingException {
        if(allergens == null){
            allergens = loadJSONObject(context, Constants.ALLERGENS_FILE_NAME);
        }
        return allergens;
    }

    public HashMap<String,String> loadJSONObject(Context context, String fileName) throws JSONException, JsonProcessingException {
        String json = "";
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(json, HashMap.class);
    }

    public ArrayList<String> additivesInProduct(ArrayList<String> productAdditiveTags, Context context) throws JSONException, JsonProcessingException {
        ArrayList<String> additivesInProduct = new ArrayList<>();
        if(productAdditiveTags.size() == 0){
            return additivesInProduct;
        }
        for(int i = 0; i < productAdditiveTags.size(); i++){
            if(getAdditives(context).containsKey(productAdditiveTags.get(i))){
                additivesInProduct.add(getAdditives(context).get(productAdditiveTags.get(i)));
            }
        }
        return additivesInProduct;
    }

    public ArrayList<String> itemsNotInUser(ArrayList<String> items, Context context, SearchFragmentSwitch s) throws JSONException, JsonProcessingException {
        if(s.equals(SearchFragmentSwitch.ADDITIVE_SEARCH)){
            Collection<String> values = this.getInstance().getAdditives(context).values();
            ArrayList<String> additivesNotInUser = new ArrayList<>(values);
            additivesNotInUser.removeAll(items);
            return additivesNotInUser;
        }
        else{
            Collection<String> values = this.getInstance().getAllergens(context).values();
            ArrayList<String> allergiesNotInUser = new ArrayList<>(values);
            allergiesNotInUser.removeAll(items);
            return allergiesNotInUser;
        }
    }

}
