package com.example.healthinspector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.healthinspector.Fragments.CartFragment;
import com.example.healthinspector.Fragments.HomeFragment;
import com.example.healthinspector.Fragments.SearchFragment;
import com.example.healthinspector.Fragments.UserProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "TimelineActivity";

    private BottomNavigationView bottomNavigationView;
    private ImageView cartImageView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getting reference to xml elements
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        cartImageView = (ImageView) findViewById(R.id.ivCart);


        //bottom navigation view item listener
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    //navigate to profile fragment
                    case R.id.miProfile:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new UserProfileFragment()).commit();
                        return true;
                    //navigate to home fragment
                    case R.id.miHome:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                        return true;
                    //navigate to search fragment
                    case R.id.miSearch:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SearchFragment()).commit();
                        return true;
                }
                return false;
            }
        });
        //setting the selected item for the nav bar as defaulting to the "home" page
        bottomNavigationView.setSelectedItemId(R.id.miHome);


        //onClick listener for cart icon within toolbar
        cartImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //navigate to the cart fragment
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CartFragment()).commit();

            }
        });



    }
}