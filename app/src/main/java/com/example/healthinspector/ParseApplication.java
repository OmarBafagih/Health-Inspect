package com.example.healthinspector;

import android.app.Application;

import com.example.healthinspector.Models.Additive;
import com.example.healthinspector.Models.Allergen;
import com.example.healthinspector.Models.Cart;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {
    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(Cart.class);
        ParseObject.registerSubclass(Additive.class);
        ParseObject.registerSubclass(Allergen.class);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.parse_application_id))
                .clientKey(getString(R.string.parse_client_key))
                .server(getString(R.string.parse_server))
                .build()
        );
    }
}
