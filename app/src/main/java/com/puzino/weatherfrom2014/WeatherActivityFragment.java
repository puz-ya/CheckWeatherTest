package com.puzino.weatherfrom2014;

import android.graphics.Typeface;
import android.os.Handler;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 * Separate Thread to asynchronously fetch data from the OpenWeatherMap API
 */
public class WeatherActivityFragment extends Fragment {

    //for our shinny font
    Typeface mWeatherFont;

    //textfields from .xml
    TextView mCityField;
    TextView mUpdatedField;
    TextView mDetailsField;
    TextView mCurrentTemperatureField;
    TextView mWeatherIcon;

    //handler to
    Handler mHandler;

    public WeatherActivityFragment() {
        mHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //this fragment view
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        mCityField = (TextView) view.findViewById(R.id.city_field);
        mUpdatedField = (TextView) view.findViewById(R.id.updated_field);
        mDetailsField = (TextView) view.findViewById(R.id.details_field);
        mCurrentTemperatureField = (TextView) view.findViewById(R.id.current_temperature_field);
        mWeatherIcon = (TextView) view.findViewById(R.id.weather_icon);

        //set our font
        mWeatherIcon.setTypeface(mWeatherFont);

        return view;
    }

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);

        mWeatherFont = Typeface.createFromAsset(getActivity().getAssets(), "weathericons-regular-webfont.ttf");
        updateWeatherData(new CityPreference(getActivity()).getCity());
    }

    //@param    city    string of chosen city to get json and show data
    void updateWeatherData(final String city){

        //set background thread
        new Thread(){
            public void run(){
                final JSONObject jsonObject = RemoteFetch.getJSON(getActivity(), city);
                if(jsonObject == null){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //showing error Toast from not main thread
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
                    //json got smth
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            renderWeather(jsonObject);
                        }
                    });
                }
            }
        }.start();

    }

    //just parsing jsonObject
    private void renderWeather(JSONObject jsonObject){
        try{
            mCityField.setText(jsonObject.getString("name").toUpperCase(Locale.US));

            //we get array object from json (by 2017.01)
            JSONObject details01 = jsonObject.getJSONArray("weather").getJSONObject(0);

            JSONObject main = jsonObject.getJSONObject("main");
            mDetailsField.setText(details01.getString("description").toUpperCase(Locale.US) +
                    "\n" + getActivity().getString(R.string.humidity) +
                    main.getString("humidity") +  getActivity().getString(R.string.percentage) +
                    "\n" + getActivity().getString(R.string.pressure) +
                    main.getString("pressure") + getActivity().getString(R.string.pa));

            String temp = String.format(Locale.getDefault(), "%.2f", main.getDouble("temp"))+ getActivity().getString(R.string.celsius);
            mCurrentTemperatureField.setText(temp);

            //get current time, set format
            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(jsonObject.getLong("dt")*1000));
            String lastUpdate = getActivity().getString(R.string.last_update) + updatedOn;
            mUpdatedField.setText(lastUpdate);

            //render correct icon (OpenWeatherMap API supports a lot of weather conditions)
            setWeatherIcon(details01.getInt("id"),
                    jsonObject.getJSONObject("sys").getLong("sunrise") * 1000,
                    jsonObject.getJSONObject("sys").getLong("sunset") * 1000);

        }catch (Exception ex){
            Log.d("WeatherFragment", ex.getMessage());
        }
    }


    private void setWeatherIcon(int actualId, long sunrise, long sunset){

        //weather conditions are like 200, 300 etc
        int id = actualId / 100;
        String icon = "";

        //sunrise and sunset times to display the sun or the moon, depending on the current time
        if(actualId == 800){
            long currentTime = new Date().getTime();
            if(currentTime >= sunrise && currentTime < sunset) {
                icon = getActivity().getString(R.string.weather_sunny);
            } else {
                icon = getActivity().getString(R.string.weather_clear_night);
            }
        } else {
            switch(id) {
                case 2 : icon = getActivity().getString(R.string.weather_thunder);
                    break;
                case 3 : icon = getActivity().getString(R.string.weather_drizzle);
                    break;
                case 7 : icon = getActivity().getString(R.string.weather_foggy);
                    break;
                case 8 : icon = getActivity().getString(R.string.weather_cloudy);
                    break;
                case 6 : icon = getActivity().getString(R.string.weather_snowy);
                    break;
                case 5 : icon = getActivity().getString(R.string.weather_rainy);
                    break;
            }
        }
        mWeatherIcon.setText(icon);
    }

    //this will call from Activity
    public void changeCity(String city){
        updateWeatherData(city);
    }
}
