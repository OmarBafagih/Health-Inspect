package com.example.healthinspector.Fragments.ScanFlow;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.example.healthinspector.Activities.MainActivity;
import com.example.healthinspector.Constants;
import com.example.healthinspector.Models.ScannedProduct;
import com.example.healthinspector.R;
import com.example.healthinspector.databinding.FragmentProductDetailsBinding;

import org.parceler.Parcels;

import java.util.Locale;


public class ProductDetailsFragment extends Fragment {

    private FragmentProductDetailsBinding binding;
    private static final String TAG = "ProductDetailsFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        ScannedProduct scannedProduct = (ScannedProduct) Parcels.unwrap(bundle.getParcelable(Constants.SCANNED_PRODUCT));

        binding.productNameTextView.setText(scannedProduct.getProductName());
        Glide.with(requireContext()).load(scannedProduct.getImageUrl()).into(binding.productImageView);

        switch (scannedProduct.getHealthInspectorScore().toUpperCase(Locale.ROOT)){
            case "A":
                binding.ratingBar.setRating(5);
                break;
            case "B":
                binding.ratingBar.setRating(4);
                break;
            case "C":
                binding.ratingBar.setRating(3);
                break;
            case "D":
                binding.ratingBar.setRating(2);
                break;
            case "E":
                binding.ratingBar.setRating(1);
                break;
            default:
                binding.ratingBar.setRating(0);
                break;
        }
        ArrayAdapter warningsAdapter = new ArrayAdapter<String>(requireContext(),
               android.R.layout.simple_list_item_1,scannedProduct.getNutrientLevels());
        binding.warningsListView.setAdapter(warningsAdapter);
        
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}