package com.example.healthinspector.Activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.example.healthinspector.R;
import com.example.healthinspector.Services.LocationService;
import com.example.healthinspector.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private UserProfileFragment userProfileFragment;
    private HomeFragment homeFragment;
    private SearchFragment searchFragment;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                binding.bottomNavigation.getMenu().setGroupCheckable(0, true, true);
                binding.cartImageView.setImageResource(R.drawable.cart_icon);
                if(this != null){
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
                            fragmentTransaction.commit();
                            return true;
                    }
                }
                return false;
            }
        });
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                startLocationService();
            } else {
                Toast.makeText(this, getString(R.string.location_permissions_toast), Toast.LENGTH_SHORT).show();
            }
            binding.bottomNavigation.setSelectedItemId(R.id.miHome);
        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationService();
            binding.bottomNavigation.setSelectedItemId(R.id.miHome);
        }
        else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        //setting the selected item for the nav bar as defaulting to the "home" page
        binding.cartImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.bottomNavigation.getMenu().setGroupCheckable(0, false, true);
                binding.cartImageView.setImageResource(R.drawable.cart_icon_filled);
                openFragment(new CartFragment());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //adding function to pending intent for quit action button on notification
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //stopping the background service and finishing this activity
                Intent LocationServiceIntent = new Intent(MainActivity.this, LocationService.class);
                stopService(LocationServiceIntent);
                finish();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.QUIT_ACTION);
        this.registerReceiver(receiver,filter);
    }

    public void startLocationService(){
        LocationService locationService = new LocationService();
        Intent serviceIntent = new Intent(this, locationService.getClass());
        serviceIntent.setAction(Constants.PERMISSIONS_GRANTED);
        if (!isLocationServiceRunning(locationService.getClass())) {
            startService(serviceIntent);
        }
    }

    @Override
    public void onBackPressed() {
        this.getSupportFragmentManager().popBackStack();
        if(this.getSupportFragmentManager().getBackStackEntryCount() == 0){
            binding.bottomNavigation.setSelectedItemId(R.id.miHome);
        }
    }

    public void openFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName());
        fragmentTransaction.commit();
    }

    private boolean isLocationServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}