package com.example.healthinspector.Activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.healthinspector.Cache.KrogerLocationCacher;
import com.example.healthinspector.Constants;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.Fragments.CartFragment;
import com.example.healthinspector.Fragments.HomeFragment;
import com.example.healthinspector.Fragments.ScanFlow.ScanFragment;
import com.example.healthinspector.Fragments.SearchFragment;
import com.example.healthinspector.Fragments.UserProfileFragment;
import com.example.healthinspector.R;
import com.example.healthinspector.Services.LocationService;
import com.example.healthinspector.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private UserProfileFragment userProfileFragment;
    private HomeFragment homeFragment;
    private ScanFragment scanFragment;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private BroadcastReceiver receiver;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KrogerLocationCacher.getInstance().getToken(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        configureNavigationDrawer();
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.drawer_icon);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        if((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES){
            binding.cartImageView.setColorFilter(Color.WHITE);
            Drawable unwrappedDrawable = AppCompatResources.getDrawable(this, R.drawable.drawer_icon);
            Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
            DrawableCompat.setTint(wrappedDrawable, Color.WHITE);
            actionBar.setHomeAsUpIndicator(wrappedDrawable);
        }

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
                            if(scanFragment == null){
                                scanFragment = new ScanFragment();
                            }
                            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//                            Bundle bundle = new Bundle();
//                            bundle.putSerializable(Constants.FRAGMENT_SWITCH, FragmentSwitch.MAIN_ACTIVITY);
//                            scanFragment.setArguments(bundle);
                            fragmentTransaction.replace(R.id.fragment_container, scanFragment);
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void configureNavigationDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        binding.navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Fragment f = null;
                int itemId = menuItem.getItemId();
                switch (itemId){
                    case R.id.nav_logout:
                        ParseUser.logOut();
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        return true;
                    default:
                        return true;
                }
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