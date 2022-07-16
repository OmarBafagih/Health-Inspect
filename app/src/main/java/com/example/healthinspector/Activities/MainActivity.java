package com.example.healthinspector.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.healthinspector.Constants;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.Fragments.CartFragment;
import com.example.healthinspector.Fragments.HomeFragment;
import com.example.healthinspector.Fragments.SearchFragment;
import com.example.healthinspector.Fragments.UserProfileFragment;
import com.example.healthinspector.LocationService;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private UserProfileFragment userProfileFragment;
    private HomeFragment homeFragment;
    private SearchFragment searchFragment;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                startLocationService();
            } else {
                Toast.makeText(this, getString(R.string.location_permissions_toast), Toast.LENGTH_SHORT).show();
            }
        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationService();
        }
        else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                binding.bottomNavigation.getMenu().setGroupCheckable(0, true, true);
                binding.cartImageView.setImageResource(R.drawable.cart_icon);
                switch (item.getItemId()){
                    case R.id.miProfile:
                        if(userProfileFragment == null) {
                            userProfileFragment = new UserProfileFragment();
                        }
                        openFragment(userProfileFragment);
                        return true;
                    case R.id.miHome:
                        if(homeFragment == null) {
                            homeFragment = new HomeFragment();
                        }
                        openFragment(homeFragment);
                        return true;
                    case R.id.miSearch:
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
                openFragment(new CartFragment());
            }
        });
    }

    public void startLocationService(){
        final Intent intent = new Intent(this, LocationService.class);
        intent.setAction(Constants.PERMISSIONS_GRANTED);
        startService(intent);
    }

    @Override
    public void onBackPressed() {
        if (this.getSupportFragmentManager().getBackStackEntryCount() > 0){
            this.getSupportFragmentManager().popBackStack();
        }
        else{
            openFragment(new HomeFragment());
            binding.bottomNavigation.setSelectedItemId(R.id.miHome);
        }
    }

    public void openFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}