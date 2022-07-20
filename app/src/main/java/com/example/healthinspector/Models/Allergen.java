package com.example.healthinspector.Models;
import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Allergens")
public class Allergen extends ParseObject{
    public static final String ALLERGEN_USES_KEY = "uses";
    public static final String ALLERGEN_KEY = "allergenKey";
    public static final String ALLERGEN_VALUE = "allergenValue";
    public static final String ALLERGEN_PRODUCT_COUNT = "productCount";
    public static final String ALLERGEN_POPULARITY_SCORE = "popularityScore";

    public Allergen(){}
    public String getAllergenKey(){return getString(ALLERGEN_KEY);}
    public String getAllergenValue(){return getString(ALLERGEN_VALUE);}
    public void setAllergenUsage(int uses){put(ALLERGEN_USES_KEY, uses);}
    public int getAllergenUsage(){return getInt(ALLERGEN_USES_KEY);}
    public int getAllergenProductCount(){return getInt(ALLERGEN_PRODUCT_COUNT);}
    public void setAllergenPopularityScore(double score){put(ALLERGEN_POPULARITY_SCORE, score);}
}
