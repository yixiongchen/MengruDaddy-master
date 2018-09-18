package com.mengrudaddy.instagram.utils;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class TabAdapter extends FragmentPagerAdapter {
    private final List<Fragment> fmlist = new ArrayList<>();
    private final List<String> fmTitles = new ArrayList<>();


    public TabAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fm, String title){
        fmlist.add(fm);
        fmTitles.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fmTitles.get(position);
    }

    @Override
    public Fragment getItem(int i) {
        return fmlist.get(i);
    }

    @Override
    public int getCount() {
        return fmlist.size();
    }
}
