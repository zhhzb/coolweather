package activitytest.example.com.coolweather;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.List;



public class WeatherInfoAdapter extends FragmentPagerAdapter {

    private List<WeatherInfoFragment> mFragmentList;
    public WeatherInfoFragment currentFragment;

    public WeatherInfoAdapter(FragmentManager fm, List<WeatherInfoFragment> fragmentList) {
        super(fm);
        mFragmentList = fragmentList;
        if(mFragmentList.size()>0){
            currentFragment = mFragmentList.get(0);
        }
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        currentFragment = (WeatherInfoFragment)object;
        super.setPrimaryItem(container, position, object);
    }
}
