package activitytest.example.com.coolweather.gson;

/**
 * Created by xiaozeng on 2017/9/8.
 */

public class AQI {
    public AQICity city;
    public class AQICity{
        public String aqi;
        public String pm25;
    }
}
