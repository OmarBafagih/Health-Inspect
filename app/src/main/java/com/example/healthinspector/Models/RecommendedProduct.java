package com.example.healthinspector.Models;

import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
public class RecommendedProduct {
    private String keyWords;
    private String brand;
    private String productName;

    // empty constructor needed by the Parceler library
    public RecommendedProduct(){}

    public RecommendedProduct(String keyWords, String brand, String productName) {
        this.keyWords = keyWords;
        this.brand = brand;
        this.productName = productName;
    }

    public String getKeyWords() {return keyWords;}
    public String getBrand() {return brand;}
    public String getProductName() {return productName;}
}
