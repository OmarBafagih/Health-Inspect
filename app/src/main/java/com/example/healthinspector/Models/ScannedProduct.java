package com.example.healthinspector.Models;

import org.parceler.Parcel;

import java.io.Serializable;
import java.util.ArrayList;

@Parcel
public class ScannedProduct implements Serializable {

    private String productName;
    private String healthInspectorScore;
    private ArrayList<String> ingredients;
    private ArrayList<String> ingredientsAnalysis;
    private String novaGroup;
    private ArrayList<String> nutrientLevels;
    private String imageUrl;
    private ArrayList<String> additives;
    private  ArrayList<String> allergens;
    private ArrayList<String> categories;


    // empty constructor needed by the Parceler library
    public ScannedProduct(){}

    public ScannedProduct(String productName, String healthInspectorScore, ArrayList<String> ingredients, ArrayList<String> ingredientsAnalysis, String novaGroup, ArrayList<String> nutrientLevels, String imageUrl, ArrayList<String> additives, ArrayList<String> allergens, ArrayList<String> categories) {
        this.productName = productName;
        this.healthInspectorScore = healthInspectorScore;
        this.ingredients = ingredients;
        this.ingredientsAnalysis = ingredientsAnalysis;
        this.novaGroup = novaGroup;
        this.nutrientLevels = nutrientLevels;
        this.imageUrl = imageUrl;
        this.additives = additives;
        this.allergens = allergens;
        this.categories = categories;
    }

    public String getProductName() {
        return productName;
    }

    public String getHealthInspectorScore() {
        return healthInspectorScore;
    }

    public ArrayList<String> getIngredients() {
        return ingredients;
    }

    public ArrayList<String> getIngredientsAnalysis() {
        return ingredientsAnalysis;
    }

    public String getNovaGroup() {
        return novaGroup;
    }

    public ArrayList<String> getNutrientLevels() {
        return nutrientLevels;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public ArrayList<String> getProductAdditives() {
        return additives;
    }

    public ArrayList<String> getAllergens() {
        return allergens;
    }

    public ArrayList<String> getAdditives() {
        return additives;
    }
    public ArrayList<String> getCategories() {
        return categories;
    }
}
