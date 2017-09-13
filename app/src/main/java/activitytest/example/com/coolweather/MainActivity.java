package activitytest.example.com.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private ChooseAreaFragment fragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragment = (ChooseAreaFragment)getSupportFragmentManager()
                .findFragmentById(R.id.choose_area_fragment);
        SharedPreferences preferences = PreferenceManager.
                getDefaultSharedPreferences(this);
        if(preferences.getString("weather0", null) != null){
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if(fragment!=null) {
            if(fragment.currentLevel == fragment.LEVEL_COUNTY){
                fragment.queryCities();
            }else if(fragment.currentLevel == fragment.LEVEL_CITY) {
                fragment.queryProvinces();
            }else{
                super.onBackPressed();
            }
        }else {
            super.onBackPressed();
        }
    }
}
