package com.example.healthinspector;

import android.app.Application;

import com.parse.Parse;

public class ParseApplication extends Application {
    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("mURCAkz3BdUIDaMdLNCU3FiEnSk1fA3oePGXCoAL")
                .clientKey("b7JVHGGnOUcbyaXpwcAi7tRGVDZSJOQP7uASYu3H")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
