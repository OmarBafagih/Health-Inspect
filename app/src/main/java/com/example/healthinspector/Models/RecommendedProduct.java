package com.example.healthinspector.Models;

import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
public class RecommendedProduct {
    private String keyWords;
    private String brand;
    private String productName;
    private String productImageUrl;
    private ArrayList<String> nutrientLevels;

    // empty constructor needed by the Parceler library
    public RecommendedProduct(){}

    public RecommendedProduct(String keyWords, String brand, String productName, String url, ArrayList<String> nutrientLevels) {
        this.keyWords = keyWords;
        this.brand = brand;
        this.productName = productName;
        this.productImageUrl = url;
        this.nutrientLevels = nutrientLevels;
    }

    public String getKeyWords() {return keyWords;}
    public String getBrand() {return brand;}
    public String getProductName() {return productName;}
    public String getProductImageUrl() {return productImageUrl;}
    public ArrayList<String> getNutrientLevels() {return nutrientLevels;}
}
