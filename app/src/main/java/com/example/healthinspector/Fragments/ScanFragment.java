package com.example.healthinspector.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.FragmentScanBinding;
import com.example.healthinspector.databinding.FragmentUserProfileBinding;
import com.google.zxing.Result;


public class ScanFragment extends Fragment {

    private FragmentScanBinding binding;
    private CodeScanner codeScannerView;
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        binding = FragmentScanBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        final Activity activity = getActivity();
        CodeScannerView scannerView = binding.scannerView;
        codeScannerView = new CodeScanner(getActivity(), scannerView);

        //resets the scannerView to look for a new barcode
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                codeScannerView.startPreview();
            }
        });
        //if the camera permissions were granted, check the CodeScanner view for a barcode
        //initialize CodeScanner view
        codeScannerView.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //barcode is stored in result
                        //making request to OpenFoodFacts API here
                        Toast.makeText(getContext(), result.getText(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onResume() {
        super.onResume();
        codeScannerView.startPreview();
    }

    @Override
    public void onPause() {
       codeScannerView.releaseResources();
        super.onPause();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}