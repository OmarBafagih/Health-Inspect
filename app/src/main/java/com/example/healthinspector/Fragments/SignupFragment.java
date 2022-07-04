package com.example.healthinspector.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.healthinspector.Activities.LoginActivity;
import com.example.healthinspector.Constants;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.FragmentSignupBinding;
import com.example.healthinspector.databinding.FragmentUserProfileBinding;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.List;

public class SignupFragment extends Fragment {
    private FragmentSignupBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = binding.usernameEditText.getText().toString();
                String password = binding.passwordEditText.getText().toString();

                //input checking
                if(username.isEmpty()){
                    Toast.makeText(requireContext(), "Please enter a valid username", Toast.LENGTH_SHORT).show();
                }
                else if(password.isEmpty()){
                    Toast.makeText(requireContext(), "Please enter a valid password", Toast.LENGTH_SHORT).show();
                }
                // querying users to check for duplicate username
                ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
                userQuery.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> usersFound, ParseException e) {
                        // check for errors
                        if (e != null) {
                            Log.e("", "Issue with getting posts", e);
                            return;
                        }
                        for (ParseUser user : usersFound) {
                            System.out.println("post");
                            if(user.getUsername().equals(username)){
                                Toast.makeText(requireContext(), "Username is already taken", Toast.LENGTH_SHORT).show();
                            }
                        }
                        //username is valid, sign user up
                        signupUser(username, password);
                    }
                });
            }
        });
    }

    private void signupUser(String username, String password){
        // Creating the new Parse user and navigating the user to their profile
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);

        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if(e != null){
                    return;
                }
                Toast.makeText(requireContext(), "Successfully created account and signed in", Toast.LENGTH_SHORT).show();

                //send user to user profile along with bundle
                UserProfileFragment userProfileFragment = new UserProfileFragment();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.SIGN_UP_FLOW, FragmentSwitch.SIGN_UP);
                userProfileFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.fragment_container, userProfileFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }
}