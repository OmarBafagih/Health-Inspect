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

    public static JSONObject loadAdditives(Context context) throws JSONException {
        String json = "";
        try {
            InputStream is = context.getAssets().open("additives.json");
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

    public static JSONObject loadAllergens(Context context) throws JSONException {
        String json = "";
        try {
            InputStream is = context.getAssets().open("allergens.json");
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
