package com.example.healthinspector;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Date;

@ParseClassName("Cart")
public class Cart extends ParseObject{

    public static final String ITEMS_KEY = "cartItems";
    public static final String USER_KEY = "user";
    public Cart(){}

    public JSONArray getCartItems(){
        return getJSONArray(ITEMS_KEY);
    }
    public void setCartItems(JSONArray array){
        put(ITEMS_KEY, array);
    }
}

