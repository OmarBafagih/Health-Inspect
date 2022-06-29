package com.example.healthinspector.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.healthinspector.Constants;
import com.example.healthinspector.Fragments.ScanFlow.ScanFragment;
import com.example.healthinspector.R;
import com.example.healthinspector.SearchFragmentSwitch;
import com.example.healthinspector.databinding.FragmentSearchBinding;


public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private ScanFragment scanFragment;
    private FragmentManager fragmentManager;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private static final String TAG = "SearchFragment";
    private String lastFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        SearchFragmentSwitch searchFragmentSwitch = (SearchFragmentSwitch) bundle.get(Constants.PREVIOUS_FRAGMENT);
        //initializing the scanFragment
        scanFragment = new ScanFragment();
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        //launches a popup to request for User's camera permissions
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                //permissions are granted, navigate to scanFragment
                fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, scanFragment).addToBackStack(null).commit();
            } else {
                Toast.makeText(getContext(), "Cannot scan barcode without camera permissions", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "User denied permission");

            }
        });

        if(searchFragmentSwitch.equals(SearchFragmentSwitch.MAIN_ACTIVITY)){
            binding.scanIconImageView.setVisibility(View.VISIBLE);
            binding.scanPromptTextView.setVisibility(View.VISIBLE);
            binding.orPromptTextView.setVisibility(View.VISIBLE);
            binding.searchPromptTextView.setText(R.string.search_products_prompt);
        }
        else if(searchFragmentSwitch.equals(SearchFragmentSwitch.ADDITIVE_SEARCH)){
            binding.searchPromptTextView.setText(R.string.search_additives_prompt);
        }
        else{
            binding.searchPromptTextView.setText(R.string.search_allergy_prompt);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.scanIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //request for User's camera permissions
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    //camera permissions are already granted
                    fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, scanFragment).addToBackStack(null).commit();
                }
                else {
                    //launch the request to the user, since permissions have not been granted yet
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}