package com.example.healthinspector.Models;
import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Additives")
public class Additive extends ParseObject {
    public static final String ADDITIVE_USES_KEY = "uses";
    public static final String ADDITIVE_KEY = "additiveKey";
    public static final String ADDITIVE_VALUE = "additiveValue";

    public Additive(){}
    public String getAdditiveKey(){return getString(ADDITIVE_KEY);}
    public String getAdditiveValue(){return getString(ADDITIVE_VALUE);}
    public void setAdditiveUsage(int uses){put(ADDITIVE_USES_KEY, uses);}
    public int getAdditiveUsage(){return getInt(ADDITIVE_USES_KEY);}
}
