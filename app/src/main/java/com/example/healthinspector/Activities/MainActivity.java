package com.example.healthinspector.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.healthinspector.Fragments.CartFragment;
import com.example.healthinspector.Fragments.HomeFragment;
import com.example.healthinspector.Fragments.SearchFragment;
import com.example.healthinspector.Fragments.UserProfileFragment;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private static String lastFragment = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //using view binding to reduce boilerplate code
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        final UserProfileFragment userProfileFragment = new UserProfileFragment();
        final HomeFragment homeFragment = new HomeFragment();
        final SearchFragment searchFragment = new SearchFragment();

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    //navigate to profile fragment
                    case R.id.miProfile:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, userProfileFragment).commit();
                        return true;
                    //navigate to home fragment
                    case R.id.miHome:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
                        return true;
                    //navigate to search fragment
                    case R.id.miSearch:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, searchFragment).commit();
                        return true;
                }
                return false;
            }
        });

        //setting the selected item for the nav bar as defaulting to the "home" page
        binding.bottomNavigation.setSelectedItemId(R.id.miHome);


        //onClick listener for cart icon within toolbar
        binding.cartImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "In OnClick listener for cart iv");

                //set the cart icon to be filled
                binding.cartImageView.setImageResource(R.drawable.cart_icon_filled);

                //navigate to the cart fragment
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CartFragment()).commit();

            }
        });



    }

}