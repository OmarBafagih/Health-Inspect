package com.example.healthinspector.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.healthinspector.R;
import com.example.healthinspector.databinding.ActivityLoginBinding;
import com.example.healthinspector.databinding.ActivityMainBinding;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {


    public static final String TAG = "LoginActivity";
    //view binder
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //using view binding to reduce boilerplate code
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        //if the user was already previously logged in
        if(ParseUser.getCurrentUser() != null){
            navigateToHome();
        }


        //creating on click listeners for login and signup buttons
        //Login button onClick listener
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick Login button");

                //collect the inputted text from the input fields
                String username = binding.etUsername.getText().toString();
                String password = binding.etPassword.getText().toString();

                //simple input checking
                if(username.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Please enter your username", Toast.LENGTH_SHORT).show();
                }
                else if(password.isEmpty()){
                    Toast.makeText(LoginActivity.this, "please enter your password", Toast.LENGTH_SHORT).show();
                }
                else{
                    loginUser(username, password);
                }

            }
        });

        //Login button onClick listener
        binding.btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick Signup button");

               //go to "user profile" fragment where user can add their allergies/ingredients etc...
                

            }
        });



    }

    private void loginUser(String username, String password) {
        Log.i(TAG, "Attempting to login user: " + username);
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(e != null){
                    Log.e(TAG, "error with login: ", e);
                    Toast.makeText(LoginActivity.this, "Login information incorrect", Toast.LENGTH_SHORT).show();
                    return;
                }
                //navigate to Timeline activity on successful login
                navigateToHome();
                Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }




    public void navigateToHome(){
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
    }
}