package activitytest.example.com.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import activitytest.example.com.coolweather.util.HttpUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class WeatherActivity extends AppCompatActivity {

    public List<WeatherInfoFragment> mFragmentList;

    WeatherInfoAdapter adapter;

    private ImageView BingPicImg;

    public DrawerLayout drawerLayout;

    private ViewPager viewPager;

    private ChooseAreaFragment fragment;

    ImageView [] imageViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);
        fragment = (ChooseAreaFragment)getSupportFragmentManager()
                .findFragmentById(R.id.choose_area_fragment);

        viewPager = (ViewPager)findViewById(R.id.view_pager);
        mFragmentList = new ArrayList<>();
        WeatherInfoFragment fragment0 = new WeatherInfoFragment();
        fragment0.setId(0);
        mFragmentList.add(fragment0);
        WeatherInfoFragment fragment1 = new WeatherInfoFragment();
        fragment1.setId(1);
        mFragmentList.add(fragment1);
        WeatherInfoFragment fragment2 = new WeatherInfoFragment();
        fragment2.setId(2);
        mFragmentList.add(fragment2);

        imageViews = new ImageView[3];
        imageViews[0] = (ImageView)findViewById(R.id.nav_page_1);
        imageViews[1] = (ImageView)findViewById(R.id.nav_page_2);
        imageViews[2] = (ImageView)findViewById(R.id.nav_page_3);

        adapter = new WeatherInfoAdapter(getSupportFragmentManager(), mFragmentList);
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                for(int i = 0;  i< imageViews.length; i++){
                    if(i == position){
                        imageViews[i].setImageResource(R.drawable.page_indicator_focused);
                    }else {
                        imageViews[i].setImageResource(R.drawable.page_indicator_unfocused);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        BingPicImg = (ImageView)findViewById(R.id.bing_pic_img);
        String bingPic = prefs.getString("bing_pic", null);
        if(bingPic != null){
            Glide.with(this).load(bingPic).into(BingPicImg);
        }else {
            loadBingPic();
        }
    }

    @Override
    public void onBackPressed() {
        if(fragment!=null) {
            if(fragment.currentLevel == fragment.LEVEL_COUNTY){
                fragment.queryCities();
            }else if(fragment.currentLevel == fragment.LEVEL_CITY) {
                fragment.queryProvinces();
            }else if(fragment.currentLevel == fragment.LEVEL_PROVINCE){
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawers();
                }else {
                    super.onBackPressed();
                }
            }else {
                super.onBackPressed();
            }
        }else {
            super.onBackPressed();
        }
    }

    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final  String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(BingPicImg);
                    }
                });
            }
        });
    }
}
