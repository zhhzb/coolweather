package activitytest.example.com.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by xiaozeng on 2017/9/8.
 */

public class AQI {

    public AQICity city;

    public class AQICity{

        public String aqi;

        public String pm25;

        @SerializedName("qlty")
        public String quality;

    }
}
