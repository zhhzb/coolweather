package activitytest.example.com.coolweather;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import activitytest.example.com.coolweather.gson.DailyForecast;
import activitytest.example.com.coolweather.gson.HourlyForecast;
import activitytest.example.com.coolweather.gson.Weather;
import activitytest.example.com.coolweather.service.AutoUpdateService;
import activitytest.example.com.coolweather.util.HttpUtil;
import activitytest.example.com.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherInfoFragment extends Fragment {

    private int id;

    public String weatherId;

    public DrawerLayout drawerLayout;

    private Button navButton;

    public SwipeRefreshLayout swipeRefresh;

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout dailyForecastLayout;

    private LinearLayout hourlyForecastLayout;

    private TextView airQualityText;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView wearText;

    private TextView fluText;

    private TextView carWashText;

    private TextView sportText;

    private TextView travelText;

    private TextView ultravioletRayText;


    public void setId(int id){
        this.id = id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather_info, container, false);


        weatherLayout = (ScrollView)view.findViewById(R.id.weather_layout);
        titleCity = (TextView)view.findViewById(R.id.title_city);
        titleUpdateTime = (TextView)view.findViewById(R.id.title_update_time);
        degreeText = (TextView)view.findViewById(R.id.degree_text);
        weatherInfoText = (TextView)view.findViewById(R.id.weather_info_text);
        dailyForecastLayout = (LinearLayout)view.findViewById(R.id.daily_forecast_layout);
        hourlyForecastLayout = (LinearLayout)view.findViewById(R.id.hourly_forecast_layout);
        airQualityText = (TextView)view.findViewById(R.id.air_quality_text);
        aqiText = (TextView)view.findViewById(R.id.aqi_text);
        pm25Text = (TextView)view.findViewById(R.id.pm25_text);
        comfortText = (TextView)view.findViewById(R.id.comfort_text);
        wearText = (TextView)view.findViewById(R.id.wear_text);
        fluText = (TextView)view.findViewById(R.id.flu_text);
        carWashText = (TextView)view.findViewById(R.id.car_wash_text);
        sportText = (TextView)view.findViewById(R.id.sport_text);
        travelText = (TextView)view.findViewById(R.id.travel_text);
        ultravioletRayText = (TextView)view.findViewById(R.id.ultraviolet_ray_text);

        swipeRefresh = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        drawerLayout = (DrawerLayout)view.findViewById(R.id.drawer_layout);
        navButton = (Button) view.findViewById(R.id.nav_button);

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((WeatherActivity)getActivity()).drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        String weatherString = prefs.getString("weather" + id, null);
        if(weatherString != null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            if(weather != null) {
                weatherId = weather.basic.weatherId;
                showWeatherInfo(weather);
            }else {
                Toast.makeText(view.getContext(), "加载失败", Toast.LENGTH_SHORT).show();
            }
        }else{
            if(id == 0) {
                weatherId = getActivity().getIntent().getStringExtra("weather_id");
            }else if(id == 1){
                weatherId = "CN101010100";
            }else if(id == 2){
                weatherId = "CN101020100";
            }
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
        return view;
    }

    public void requestWeather(final  String weatherId){

        String WeatherUrl = "http://guolin.tech/api/weather?cityid=" +weatherId+
                "&key=eb0799cd9f7d4fa8a716989a1685c109";
        HttpUtil.sendOkHttpRequest(WeatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(getContext()).edit();
                            editor.putString("weather"+id, responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                            Intent intent = new Intent(getContext(), AutoUpdateService.class);
                            getActivity().startService(intent);
                        }else {
                            Toast.makeText(getContext(), "获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = "数据更新：" + weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);

        dailyForecastLayout.removeAllViews();
        for (DailyForecast dailyForecast : weather.dailyForecastList) {
            View view = LayoutInflater.from(getContext())
                    .inflate(R.layout.forecast_item, dailyForecastLayout, false);
            TextView dataText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dataText.setText(dailyForecast.date);
            infoText.setText(dailyForecast.more.info);
            maxText.setText(dailyForecast.temperature.max);
            minText.setText(dailyForecast.temperature.min);
            dailyForecastLayout.addView(view);
        }

        hourlyForecastLayout.removeAllViews();
        for(HourlyForecast hourlyForecast : weather.hourlyForecasts){
            View view = LayoutInflater.from(getContext())
                    .inflate(R.layout.hourly_forecast_item, hourlyForecastLayout, false);
            TextView dataText = (TextView) view.findViewById(R.id.hourly_forecast_time_text);
            TextView temperatureText = (TextView) view.findViewById(R.id.hourly_forecast_temperature_text);
            TextView conditionText = (TextView)view.findViewById(R.id.hourly_forecast_condition_text);
            dataText.setText(hourlyForecast.date.split(" ")[1]);
            temperatureText.setText(hourlyForecast.temperature);
            conditionText.setText(hourlyForecast.condition.state);
            hourlyForecastLayout.addView(view);
        }

        if(weather.aqi != null){
            airQualityText.setText(weather.aqi.city.quality);
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.brf + "\n" + weather.suggestion.comfort.info;
        String wear = "着装度：" + weather.suggestion.wear.brf + "\n" + weather.suggestion.wear.info;
        String flu = "感冒：" + weather.suggestion.flu.brf + "\n" + weather.suggestion.flu.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.brf + "\n" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.brf + "\n" + weather.suggestion.sport.info;
        String travel = "适合旅行：" + weather.suggestion.travel.brf + "\n" + weather.suggestion.travel.info;
        String ultravioletRay  = "紫外线：" + weather.suggestion.ultravioletRay.brf + "\n" + weather.suggestion.ultravioletRay.info;
        comfortText.setText(comfort);
        wearText.setText(wear);
        fluText.setText(flu);
        carWashText.setText(carWash);
        sportText.setText(sport);
        travelText.setText(travel);
        ultravioletRayText.setText(ultravioletRay);
        weatherLayout.setVisibility(View.VISIBLE);
    }

}
