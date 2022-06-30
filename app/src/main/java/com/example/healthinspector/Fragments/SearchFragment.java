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
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.healthinspector.Constants;
import com.example.healthinspector.Fragments.ScanFlow.ScanFragment;
import com.example.healthinspector.ItemAdapter;
import com.example.healthinspector.R;
import com.example.healthinspector.SearchFragmentSwitch;
import com.example.healthinspector.databinding.FragmentSearchBinding;
import com.parse.ParseUser;

import java.util.ArrayList;


public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private ScanFragment scanFragment;
    private FragmentManager fragmentManager;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private static final String TAG = "SearchFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //initializing the scanFragment
        scanFragment = new ScanFragment();
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        Bundle bundle = getArguments();
        SearchFragmentSwitch searchFragmentSwitch = (SearchFragmentSwitch) bundle.getSerializable(Constants.SEARCH_FRAGMENT_ENUM);

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
        else if(!searchFragmentSwitch.equals(SearchFragmentSwitch.USER_WARNINGS) && !searchFragmentSwitch.equals(SearchFragmentSwitch.USER_ALLERGIES)){
            boolean isAdditiveSearch = false;
            //setting what was the text view prompt to resemble a button for users to confirm
            RelativeLayout.LayoutParams promptParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            promptParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            promptParams.setMargins(30,30,30,30);
            binding.searchPromptTextView.setLayoutParams(promptParams);
            binding.searchPromptTextView.setTextSize(18);
            binding.searchPromptTextView.setBackground(requireContext().getDrawable(R.drawable.textview_button_style));
            binding.searchPromptTextView.setText(R.string.search_ingredients_prompt);

            ItemAdapter itemAdapter = new ItemAdapter();


            if(searchFragmentSwitch.equals(SearchFragmentSwitch.ADDITIVE_SEARCH)){

            }
            else if(searchFragmentSwitch.equals(SearchFragmentSwitch.ALLERGEN_SEARCH)){
                //attach adapter to recycler view
            }

            binding.searchPromptTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //save selections to parse database
                    if(isAdditiveSearch){
                        //save additives to parse
                    }
                    else{
                        //save the warnings to parse
                    }
                }
            });

            LinearLayoutManager linearLayoutManagerAllergies = new LinearLayoutManager(getContext());
            binding.searchItemsRecyclerView.setLayoutManager(linearLayoutManagerAllergies);
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