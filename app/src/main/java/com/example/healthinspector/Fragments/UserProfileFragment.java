package com.example.healthinspector.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.healthinspector.Activities.LoginActivity;
import com.example.healthinspector.Activities.MainActivity;
import com.example.healthinspector.Constants;
import com.example.healthinspector.Adapters.ItemAdapter;
import com.example.healthinspector.R;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.databinding.FragmentUserProfileBinding;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseUser;

import java.util.ArrayList;

public class UserProfileFragment extends Fragment {

    private FragmentUserProfileBinding binding;
    private SearchFragment searchFragment;
    private FragmentSwitch fragmentSwitch;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        //if the user is currently in signup flow, configure the done button
        if(getArguments() != null){
            Log.i("USERPROFFRAG", "NOT NULL");
            Bundle bundle = getArguments();
            fragmentSwitch = (FragmentSwitch) bundle.getSerializable(Constants.SIGN_UP_FLOW);
            if(fragmentSwitch.equals(FragmentSwitch.SIGN_UP)){
                binding.btnDone.setVisibility(View.VISIBLE);
                binding.btnDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(requireContext(), MainActivity.class);
                        startActivity(i);
                    }
                });
            }
        }

        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //logout parse user
                ParseUser.logOut();
                ParseUser currentUser = ParseUser.getCurrentUser();
                startActivity(new Intent(requireContext().getApplicationContext(), LoginActivity.class));
            }
        });

        binding.addWarningImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFragment = new SearchFragment();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                Bundle bundle = new Bundle();

                if(fragmentSwitch != null && fragmentSwitch.equals(FragmentSwitch.SIGN_UP)){
                    bundle.putSerializable(Constants.SIGN_UP_FLOW, FragmentSwitch.SIGN_UP);
                }


                bundle.putSerializable(Constants.FRAGMENT_SWITCH, FragmentSwitch.ADDITIVE_SEARCH);
                searchFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.fragment_container, searchFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        binding.addAllergyImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFragment = new SearchFragment();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                Bundle bundle = new Bundle();
                if(fragmentSwitch != null && fragmentSwitch.equals(FragmentSwitch.SIGN_UP)){
                    bundle.putSerializable(Constants.SIGN_UP_FLOW, FragmentSwitch.SIGN_UP);
                }

                bundle.putSerializable(Constants.FRAGMENT_SWITCH, FragmentSwitch.ALLERGEN_SEARCH);
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

        ArrayList<String> userAllergies = (ArrayList) ParseUser.getCurrentUser().get(Constants.PARSE_USER_ALLERGIES);
        ItemAdapter allergiesAdapter = new ItemAdapter(requireContext(), userAllergies, FragmentSwitch.USER_ALLERGIES);
        binding.userAllergiesRecyclerView.setAdapter(allergiesAdapter);
        LinearLayoutManager linearLayoutManagerAllergies = new LinearLayoutManager(getContext());
        binding.userAllergiesRecyclerView.setLayoutManager(linearLayoutManagerAllergies);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                String deletedAllergy = userAllergies.get(viewHolder.getAdapterPosition());
                int position = viewHolder.getAdapterPosition();
                userAllergies.remove(viewHolder.getAdapterPosition());
                allergiesAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());

                //User can undo deletion for a period of time
                Snackbar.make(binding.userAllergiesRecyclerView, deletedAllergy, Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                    //snackbar onclick "undo"
                    @Override
                    public void onClick(View v) {
                        //add the item back into the user's warning list
                        userAllergies.add(position, deletedAllergy);
                        //update user on Parse database
                        ParseUser.getCurrentUser().put(Constants.PARSE_USER_ALLERGIES, userAllergies);
                        ParseUser.getCurrentUser().saveInBackground();
                        allergiesAdapter.notifyItemInserted(position);
                    }
                }).show();
                //update user on Parse database after the item has been swiped (deleted)
                ParseUser.getCurrentUser().put(Constants.PARSE_USER_ALLERGIES, userAllergies);
                ParseUser.getCurrentUser().saveInBackground();

            }
        }).attachToRecyclerView(binding.userAllergiesRecyclerView);

        ArrayList<String> userWarnings = (ArrayList) ParseUser.getCurrentUser().get(Constants.PARSE_USER_WARNINGS);
        ItemAdapter warningsAdapter = new ItemAdapter(requireContext(), userWarnings, FragmentSwitch.USER_WARNINGS);
        binding.userWarningsRecyclerView.setAdapter(warningsAdapter);
        LinearLayoutManager linearLayoutManagerWarnings = new LinearLayoutManager(getContext());
        binding.userWarningsRecyclerView.setLayoutManager(linearLayoutManagerWarnings);

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
                Snackbar.make(binding.userWarningsRecyclerView, deletedWarning, Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                    //snackbar onclick "undo"
                    @Override
                    public void onClick(View v) {
                        //add the item back into the user's warning list
                        userWarnings.add(position, deletedWarning);
                        //update user on Parse database
                        ParseUser.getCurrentUser().put(Constants.PARSE_USER_WARNINGS, userWarnings);
                        ParseUser.getCurrentUser().saveInBackground();
                        warningsAdapter.notifyItemInserted(position);
                    }
                }).show();
                //update user on Parse database after the item has been swiped (deleted)
                ParseUser.getCurrentUser().put(Constants.PARSE_USER_WARNINGS, userWarnings);
                ParseUser.getCurrentUser().saveInBackground();

            }
        }).attachToRecyclerView(binding.userWarningsRecyclerView);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}