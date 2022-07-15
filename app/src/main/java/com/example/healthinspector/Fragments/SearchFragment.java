package com.example.healthinspector.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.healthinspector.Adapters.ItemAdapter;
import com.example.healthinspector.CachedLists;
import com.example.healthinspector.Constants;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.Fragments.ScanFlow.ScanFragment;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.FragmentSearchBinding;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.parse.ParseUser;

import org.json.JSONException;

import java.util.ArrayList;


public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private ScanFragment scanFragment;
    private UserProfileFragment userProfileFragment;
    private FragmentManager fragmentManager;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private static final String TAG = "SearchFragment";
    private ItemAdapter itemAdapter;
    private FragmentSwitch signupSwitch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //initializing the scanFragment
        scanFragment = new ScanFragment();
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        Bundle bundle = getArguments();
        FragmentSwitch fragmentSwitch = (FragmentSwitch) bundle.getSerializable(Constants.FRAGMENT_SWITCH);
        if(bundle.containsKey(Constants.SIGN_UP_FLOW)){
            signupSwitch = (FragmentSwitch) bundle.get(Constants.SIGN_UP_FLOW);
        }

        //launches a popup to request for User's camera permissions
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                //permissions are granted, navigate to scanFragment
                fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, scanFragment).addToBackStack(null).commit();
            } else {
                Toast.makeText(requireContext(), getString(R.string.no_camera_permissions), Toast.LENGTH_SHORT).show();
            }
        });

        if(fragmentSwitch.equals(FragmentSwitch.MAIN_ACTIVITY)){
            binding.scanIconImageView.setVisibility(View.VISIBLE);
            binding.scanPromptTextView.setVisibility(View.VISIBLE);
            binding.orPromptTextView.setVisibility(View.VISIBLE);
            binding.searchPromptTextView.setText(getString(R.string.search_products_prompt));
        }
        else if(!fragmentSwitch.equals(FragmentSwitch.USER_WARNINGS) && !fragmentSwitch.equals(FragmentSwitch.USER_ALLERGIES)){
            userProfileFragment = new UserProfileFragment();
            //setting what was the text view prompt to resemble a button for users to confirm
            RelativeLayout.LayoutParams promptParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            promptParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            promptParams.setMargins(30,30,30,30);

            binding.searchPromptTextView.setLayoutParams(promptParams);
            binding.searchPromptTextView.setTextSize(18);
            binding.searchPromptTextView.setBackground(requireContext().getDrawable(R.drawable.textview_button_style));
            binding.searchPromptTextView.setText(getString(R.string.search_ingredients_prompt));

            if(fragmentSwitch.equals(FragmentSwitch.ADDITIVE_SEARCH)){
                setupSearch(Constants.PARSE_USER_WARNINGS, FragmentSwitch.ADDITIVE_SEARCH);
                setupSearchBarTextWatcher(Constants.PARSE_USER_WARNINGS, FragmentSwitch.ADDITIVE_SEARCH);
            }
            else if(fragmentSwitch.equals(FragmentSwitch.ALLERGEN_SEARCH)){
               setupSearch(Constants.PARSE_USER_ALLERGIES, FragmentSwitch.ALLERGEN_SEARCH);
               setupSearchBarTextWatcher(Constants.PARSE_USER_ALLERGIES, FragmentSwitch.ALLERGEN_SEARCH);
            }
            binding.searchItemsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.scanIconImageView.setOnClickListener(v -> {
            //request for User's camera permissions
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                //camera permissions are already granted
                fragmentManager = requireActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, scanFragment).addToBackStack(null).commit();
            }
            else {
                //launch the request to the user, since permissions have not been granted yet
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    public void setupSearchBarTextWatcher(String warningType, FragmentSwitch fragmentSwitch){
        //creating TextWatcher for edit text to act as a search bar
        binding.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    filter(binding.editTextSearch.getText().toString(),
                            CachedLists.getInstance().itemsNotInUser((ArrayList) ParseUser.getCurrentUser().get(warningType), requireContext(), fragmentSwitch));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), getString(R.string.error_searching), Toast.LENGTH_SHORT).show();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), getString(R.string.error_searching), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String text, ArrayList<String> items) throws JSONException, JsonProcessingException {
        // creating a new array list to filter our data.
        ArrayList<String> filteredList = new ArrayList<>();
        for (String item : items) {
            if (item.toUpperCase().contains(text.toUpperCase())) {
                filteredList.add(item);
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.no_item_found), Toast.LENGTH_SHORT).show();
        } else {
            itemAdapter.filterList(filteredList);
        }
    }

    public void setupSearch(String parseKey, FragmentSwitch fragmentSwitch){
        try {
            ArrayList<String> userAllergies = (ArrayList) ParseUser.getCurrentUser().get(parseKey);
            itemAdapter = new ItemAdapter(requireContext(),
                    CachedLists.getInstance().itemsNotInUser(userAllergies, requireContext(), fragmentSwitch), fragmentSwitch);
            binding.searchItemsRecyclerView.setAdapter(itemAdapter);

            ItemAdapter finalItemAdapter = itemAdapter;
            binding.searchPromptTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userAllergies.add(finalItemAdapter.getAddedItem());
                    ParseUser user = ParseUser.getCurrentUser();
                    user.put(parseKey, userAllergies);
                    user.saveInBackground();
                    if(signupSwitch != null && signupSwitch.equals(FragmentSwitch.SIGN_UP)){
                        UserProfileFragment userProfileFragment = new UserProfileFragment();
                        FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Constants.SIGN_UP_FLOW, FragmentSwitch.SIGN_UP);
                        userProfileFragment.setArguments(bundle);
                        fragmentTransaction.replace(R.id.fragment_container, userProfileFragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    }
                    else{
                        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, userProfileFragment).commit();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();

        } catch (JsonProcessingException e) {
            e.printStackTrace();

        }
    }
}