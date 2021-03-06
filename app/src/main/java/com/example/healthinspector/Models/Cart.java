package com.example.healthinspector.Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONArray;

@ParseClassName("Cart")
public class Cart extends ParseObject{

    public static final String ITEMS_KEY = "cartItems";
    public Cart(){}

    public JSONArray getCartItems(){
        return getJSONArray(ITEMS_KEY);
    }
    public void setCartItems(JSONArray array){
        put(ITEMS_KEY, array);
    }
}

