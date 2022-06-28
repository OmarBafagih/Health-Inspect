package com.example.healthinspector;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class CachedLists{

    private static CachedLists cachedLists = null;
    private JSONObject additives = null;
    private JSONObject allergens = null;

    private CachedLists(Context context) throws JSONException {
        additives = loadJSONObject(context, "additives.json");
        allergens = loadJSONObject(context, "allergens.json");

    }
    public static CachedLists getInstance(Context context) throws JSONException {
        if (cachedLists == null){
            cachedLists = new CachedLists(context);
        }
        return cachedLists;
    }

    public JSONObject getAdditives() {
        return additives;
    }

    public JSONObject getAllergens() {
        return allergens;
    }

    public JSONObject loadJSONObject(Context context, String fileName) throws JSONException {
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
        return new JSONObject(json);
    }

}
