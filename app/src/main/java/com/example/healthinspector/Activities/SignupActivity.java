package com.example.healthinspector.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.healthinspector.Fragments.SignupFragment;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.ActivityMainBinding;
import com.example.healthinspector.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SignupFragment()).commit();
    }
}