package com.example.healthinspector.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.healthinspector.Activities.LoginActivity;
import com.example.healthinspector.Constants;
import com.example.healthinspector.Fragments.ScanFlow.ProductDetailsFragment;
import com.example.healthinspector.ItemAdapter;
import com.example.healthinspector.R;
import com.example.healthinspector.SearchFragmentSwitch;
import com.example.healthinspector.databinding.FragmentSearchBinding;
import com.example.healthinspector.databinding.FragmentUserProfileBinding;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.parceler.Parcels;

import java.util.ArrayList;

public class UserProfileFragment extends Fragment {

    private FragmentUserProfileBinding binding;
    private final static String TAG = "UserProfileFragment";
    private SearchFragment searchFragment = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                ParseUser currentUser = ParseUser.getCurrentUser();
                startActivity(new Intent(requireContext().getApplicationContext(), LoginActivity.class));
            }
        });
        binding.addAllergyImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                searchFragment = new SearchFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.PREVIOUS_FRAGMENT, SearchFragmentSwitch.ALLERGEN_SEARCH);
                searchFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.fragment_container, searchFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        binding.addWarningImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                searchFragment = new SearchFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.PREVIOUS_FRAGMENT, SearchFragmentSwitch.ADDITIVE_SEARCH);
                searchFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.fragment_container, searchFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayList<String> userAllergies = (ArrayList) ParseUser.getCurrentUser().get("userAllergies");
        ItemAdapter allergiesAdapter = new ItemAdapter(requireContext(), userAllergies, SearchFragmentSwitch.ADDITIVE_SEARCH);
        binding.userAllergiesRecyclerView.setAdapter(allergiesAdapter);
        LinearLayoutManager linearLayoutManagerAllergies = new LinearLayoutManager(getContext());
        binding.userAllergiesRecyclerView.setLayoutManager(linearLayoutManagerAllergies);

        ArrayList<String> userWarnings = (ArrayList) ParseUser.getCurrentUser().get("userWarningIngredients");
        ItemAdapter warningsAdapter = new ItemAdapter(requireContext(), userWarnings, SearchFragmentSwitch.ADDITIVE_SEARCH);
        binding.userAllergiesRecyclerView.setAdapter(warningsAdapter);
        LinearLayoutManager linearLayoutManagerWarnings = new LinearLayoutManager(getContext());
        binding.userAllergiesRecyclerView.setLayoutManager(linearLayoutManagerWarnings);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}