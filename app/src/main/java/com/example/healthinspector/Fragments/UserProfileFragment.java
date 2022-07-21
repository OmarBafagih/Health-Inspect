package com.example.healthinspector.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthinspector.Activities.LoginActivity;
import com.example.healthinspector.Activities.MainActivity;
import com.example.healthinspector.Adapters.ItemAdapter;
import com.example.healthinspector.Cache.CachedLists;
import com.example.healthinspector.Constants;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.Models.Additive;
import com.example.healthinspector.Models.Allergen;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.FragmentUserProfileBinding;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseUser;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class UserProfileFragment extends Fragment {

    private FragmentUserProfileBinding binding;
    private SearchFragment searchFragment;
    private FragmentSwitch fragmentSwitch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        CachedLists.loadMostPopularWarnings(requireContext(), Constants.ADDITIVES_FILE_NAME, Additive.ADDITIVE_USES_KEY, Constants.QUERY_LIMIT, Additive.class);
        CachedLists.loadMostPopularWarnings(requireContext(), Constants.ALLERGENS_FILE_NAME, Allergen.ALLERGEN_POPULARITY_SCORE, Constants.QUERY_LIMIT, Allergen.class);
        try {
            CachedLists.getAdditives(requireContext());
            CachedLists.getAllergens(requireContext());
        } catch (IOException | JSONException e) {
           e.printStackTrace();
        }
        //if the user is currently in signup flow, configure the done button
        Bundle bundle = getArguments();
        if(bundle != null){
            fragmentSwitch = (FragmentSwitch) bundle.getSerializable(Constants.SIGN_UP_FLOW);
            if(fragmentSwitch.equals(FragmentSwitch.SIGN_UP)){
                binding.btnDone.setVisibility(View.VISIBLE);
                binding.btnDone.setOnClickListener((View v) -> {
                    Intent i = new Intent(requireContext(), MainActivity.class);
                    startActivity(i);
                });
            }
        }
        binding.btnLogout.setOnClickListener(v -> {
            //logout parse user
            ParseUser.logOut();
            ParseUser currentUser = ParseUser.getCurrentUser();
            startActivity(new Intent(requireContext().getApplicationContext(), LoginActivity.class));
        });

        binding.addWarningImageView.setOnClickListener(v -> onImageViewClick(FragmentSwitch.ADDITIVE_SEARCH, fragmentSwitch));
        binding.addAllergyImageView.setOnClickListener(v -> onImageViewClick(FragmentSwitch.ALLERGEN_SEARCH, fragmentSwitch));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayList<String> userAllergies = (ArrayList) ParseUser.getCurrentUser().get(Constants.PARSE_USER_ALLERGIES);
        ItemAdapter allergiesAdapter = new ItemAdapter(requireContext(), userAllergies, FragmentSwitch.USER_ALLERGIES);
        binding.userAllergiesRecyclerView.setAdapter(allergiesAdapter);
        LinearLayoutManager linearLayoutManagerAllergies = new LinearLayoutManager(requireContext());
        binding.userAllergiesRecyclerView.setLayoutManager(linearLayoutManagerAllergies);
        setupSwipeToDelete(Constants.PARSE_USER_ALLERGIES, userAllergies, allergiesAdapter, binding.userAllergiesRecyclerView, FragmentSwitch.USER_ALLERGIES);

        ArrayList<String> userWarnings = (ArrayList) ParseUser.getCurrentUser().get(Constants.PARSE_USER_WARNINGS);
        ItemAdapter additivesAdapter = new ItemAdapter(requireContext(), userWarnings, FragmentSwitch.USER_WARNINGS);
        binding.userWarningsRecyclerView.setAdapter(additivesAdapter);
        LinearLayoutManager linearLayoutManagerWarnings = new LinearLayoutManager(requireContext());
        binding.userWarningsRecyclerView.setLayoutManager(linearLayoutManagerWarnings);
        setupSwipeToDelete(Constants.PARSE_USER_WARNINGS, userWarnings, additivesAdapter, binding.userWarningsRecyclerView, FragmentSwitch.USER_WARNINGS);
    }

    private void setupSwipeToDelete(String warningType, ArrayList<String> userWarnings, ItemAdapter warningsAdapter, RecyclerView recyclerView, FragmentSwitch fragmentSwitch){
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                String deletedWarning = userWarnings.get(viewHolder.getAdapterPosition());
                int position = viewHolder.getAdapterPosition();
                userWarnings.remove(viewHolder.getAdapterPosition());
                warningsAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                //User can undo deletion for a period of time
                //snack bar onclick "undo"
                Snackbar.make(recyclerView, deletedWarning, Snackbar.LENGTH_SHORT).setAction(getString(R.string.undo), v -> {
                    //add the item back into the user's warning list
                    userWarnings.add(position, deletedWarning);
                    //update Parse database
                    SearchFragment.updateAddedItemInDatabase(deletedWarning, fragmentSwitch, 1);
                    ParseUser.getCurrentUser().put(warningType, userWarnings);
                    ParseUser.getCurrentUser().saveInBackground();
                    warningsAdapter.notifyItemInserted(position);
                }).show();
                //update Parse database after the SnackBar timer has run out
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SearchFragment.updateAddedItemInDatabase(deletedWarning, fragmentSwitch, -1);
                    }
                }, Snackbar.LENGTH_SHORT);
                ParseUser.getCurrentUser().put(warningType, userWarnings);
                ParseUser.getCurrentUser().saveInBackground();
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void onImageViewClick(FragmentSwitch fragmentSwitch, FragmentSwitch signupSwitch){
        searchFragment = new SearchFragment();
        FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        Bundle bundle = new Bundle();
        if(signupSwitch != null && signupSwitch.equals(FragmentSwitch.SIGN_UP)){
            bundle.putSerializable(Constants.SIGN_UP_FLOW, FragmentSwitch.SIGN_UP);
        }
        bundle.putSerializable(Constants.FRAGMENT_SWITCH, fragmentSwitch);
        searchFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.fragment_container, searchFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}