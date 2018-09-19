package com.mengrudaddy.instagram.Adapter;
/*
ViewPagerAdapter.java
This class is adpter for display fragment of tabs in all related layouts
 */


import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter{
    private static final String TAG = "ViewPagerAdapter";
    private final List<Fragment> fmList = new ArrayList<>();
    private final List<String> fmTitleList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm){
        super(fm);
    }

    public Fragment getItem(int position){
        return fmList.get(position);
    }

    public int getCount(){
        return fmList.size();
    }

    public void addFragment(Fragment fm, String tile){
        fmList.add(fm);
        fmTitleList.add(tile);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return fmTitleList.get(position);
    }
}
