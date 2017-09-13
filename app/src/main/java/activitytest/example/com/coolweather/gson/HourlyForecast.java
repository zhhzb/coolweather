package activitytest.example.com.coolweather.gson;

import com.google.gson.annotations.SerializedName;


public class HourlyForecast {

    @SerializedName("cond")
    public Condition condition;

    public String date;

    @SerializedName("tmp")
    public String temperature;

    public class Condition{

        @SerializedName("txt")
        public String state;
    }
}
