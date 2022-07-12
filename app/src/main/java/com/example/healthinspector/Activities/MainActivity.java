package com.example.healthinspector.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.healthinspector.Constants;
import com.example.healthinspector.Fragments.CartFragment;
import com.example.healthinspector.Fragments.HomeFragment;
import com.example.healthinspector.Fragments.SearchFragment;
import com.example.healthinspector.Fragments.UserProfileFragment;
import com.example.healthinspector.LocationService;
import com.example.healthinspector.R;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private static String lastFragment = "";
    private UserProfileFragment userProfileFragment;
    private HomeFragment homeFragment;
    private SearchFragment searchFragment;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //using view binding to reduce boilerplate code
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                startLocationService();
            } else {
                Toast.makeText(this, Constants.LOCATION_PERMISSIONS_TOAST, Toast.LENGTH_SHORT).show();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //camera permissions are already granted
            startLocationService();
        }
        else {
            //launch the request to the user, since permissions have not been granted yet
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                binding.bottomNavigation.getMenu().setGroupCheckable(0, true, true);
                switch (item.getItemId()){
                    //navigate to profile fragment
                    case R.id.miProfile:
                        binding.cartImageView.setImageResource(R.drawable.cart_icon);
                        if(userProfileFragment == null) {
                            userProfileFragment = new UserProfileFragment();
                        }
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, userProfileFragment).commit();
                        return true;
                    //navigate to home fragment
                    case R.id.miHome:
                        binding.cartImageView.setImageResource(R.drawable.cart_icon);
                        if(homeFragment == null) {
                            homeFragment = new HomeFragment();
                        }
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
                        return true;
                    //navigate to search fragment
                    case R.id.miSearch:
                        binding.cartImageView.setImageResource(R.drawable.cart_icon);
                        if(searchFragment == null){
                            searchFragment = new SearchFragment();
                        }
                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Constants.FRAGMENT_SWITCH, FragmentSwitch.MAIN_ACTIVITY);
                        searchFragment.setArguments(bundle);

                        fragmentTransaction.replace(R.id.fragment_container, searchFragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                        return true;
                    case R.id.cartImageView:

                }
                return false;
            }
        });

        //setting the selected item for the nav bar as defaulting to the "home" page
        binding.bottomNavigation.setSelectedItemId(R.id.miHome);

        binding.cartImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.bottomNavigation.getMenu().setGroupCheckable(0, false, true);
                binding.cartImageView.setImageResource(R.drawable.cart_icon_filled);
                //navigate to the cart fragment
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CartFragment()).commit();
            }
        });
    }

    public void startLocationService(){
        final Intent intent = new Intent(this, LocationService.class);
        intent.setAction(Constants.PERMISSIONS_GRANTED);
        startService(intent);
    }
}