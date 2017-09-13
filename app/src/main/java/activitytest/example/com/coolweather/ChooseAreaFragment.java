package activitytest.example.com.coolweather;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import activitytest.example.com.coolweather.db.City;
import activitytest.example.com.coolweather.db.County;
import activitytest.example.com.coolweather.db.Province;
import activitytest.example.com.coolweather.util.HttpUtil;
import activitytest.example.com.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class ChooseAreaFragment extends Fragment {

    /**
     * 级别常量
     */
    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    ProgressDialog progressDialog;

    private TextView textView;

    private Button button;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    /**
     * 用于显示的数据
     */
    private List<String> dataList = new ArrayList<>();

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 乡列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省
     */
    private Province selectedProvince;

    /**
     * 选中的市
     */
    private City selectedCity;

    /**
     * 选中的乡
     */
    private County selectedCounty;

    /**
     * 当前选中的级别
     */
    public int currentLevel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        textView = (TextView)view.findViewById(R.id.title_text);
        button = (Button)view.findViewById(R.id.back_button);
        listView = (ListView)view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.
                simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if(currentLevel == LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    if(getActivity() instanceof MainActivity ) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        WeatherInfoFragment fragment = activity.adapter.currentFragment;
                        if(activity.drawerLayout != null)
                            activity.drawerLayout.closeDrawers();
                        if(fragment != null) {
                            fragment.swipeRefresh.setRefreshing(true);
                            fragment.weatherId = weatherId;
                            fragment.requestWeather(weatherId);
                        }
                    }
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国所有省，优先从数据库查询，如果没有再到服务器查询
     */
    void queryProvinces(){
        textView.setText("中国");
        button.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size() > 0){
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, LEVEL_PROVINCE);
        }
    }

    /**
     * 查询选中省所有市，优先从数据库查询，如果没有再到服务器查询
     */
    void queryCities(){
        textView.setText(selectedProvince.getProvinceName());
        button.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", selectedProvince.getId()+"").find(City.class);
        if(cityList.size() > 0){
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            String address = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode();
            queryFromServer(address, LEVEL_CITY);
        }
    }

    /**
     * 查询选中县所有市，优先从数据库查询，如果没有再到服务器查询
     */
    private void queryCounties(){
        textView.setText(selectedCity.getCityName());
        button.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", selectedCity.getId()+"").find(County.class);
        if(countyList.size() > 0){
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            currentLevel = LEVEL_COUNTY;
            listView.setSelection(0);
        }else{
            String address = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode()
                    + "/" + selectedCity.getCityCode();
            queryFromServer(address, LEVEL_COUNTY);
        }
    }

    /**
     *从服务器查询数据
     * @param address 数据来源地址
     * @param level 查询等级
     */
    private void queryFromServer(String address, final int level){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                boolean result = false;
                switch (level){
                    case LEVEL_CITY:
                        result = Utility.handleCityResponse(responseString,
                                selectedProvince.getId());
                        break;
                    case LEVEL_COUNTY:
                        result = Utility.handleCountyResponse(responseString,
                                selectedCity.getId());
                        break;
                    case LEVEL_PROVINCE:
                        result = Utility.handleProvinceResponse(responseString);
                        break;
                    default:
                        break;
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            switch (level){
                                case LEVEL_CITY:
                                    queryCities();
                                    break;
                                case LEVEL_COUNTY:
                                    queryCounties();
                                    break;
                                case LEVEL_PROVINCE:
                                    queryProvinces();
                                    break;
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

}
