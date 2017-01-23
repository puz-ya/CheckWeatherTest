package com.puzino.weatherfrom2014;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by YD on 23.01.2017.
 * This class is responsible for fetching the weather data from the OpenWeatherMap API
 */

public class RemoteFetch {

    private static final String OPEN_WEATHER_API = "http://api.openweathermap.org/data/2.5/weather?q=%s&APPID=%s&units=metric";

    public static JSONObject getJSON(Context context, String city){
        try{
            String apikey = context.getString(R.string.open_weather_maps_app_id);

            //replace &s with city name
            URL url = new URL(String.format(OPEN_WEATHER_API, city, apikey));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            //in old version need to set Property
            //connection.addRequestProperty("x-api-key", apikey);

            //read API response
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer stringJson = new StringBuffer(1024);
            String tmp = "";

            //read and summirize values
            while ((tmp = bufferedReader.readLine()) != null){
                stringJson.append(tmp);
            }
            bufferedReader.close();

            JSONObject data = new JSONObject(stringJson.toString());

            //check if response is OK
            if(data.getInt("cod") != 200){
                return null;
            }

            return data;

        }catch (Exception e){
            Log.d("RemoteFetch", e.getMessage());
            return null;
        }
    }
}
