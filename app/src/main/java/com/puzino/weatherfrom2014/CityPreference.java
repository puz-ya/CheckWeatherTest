package com.puzino.weatherfrom2014;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by YD on 23.01.2017.
 * Here we just store chosen city
 */

public class CityPreference {

    private SharedPreferences mPreferences;

    CityPreference(Activity activity){
        mPreferences = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    String getCity(){
        return mPreferences.getString("city", "Moscow, RU");
    }

    void setCity(String set){
        //commit writes directly, aplly - in background
        mPreferences.edit().putString("city", set).apply();
    }
}
