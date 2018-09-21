package com.mengrudaddy.instagram;

/*
ActivityFeed.java
This class is activity for showing tab-activity of 'Following' and 'You' by clicking 'heart' icon in main bottom navigation
 */

import android.content.Context;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mengrudaddy.instagram.Adapter.ViewPagerAdapter;
import com.mengrudaddy.instagram.utils.BottomNavigHelper;

public class ActivityFeed extends AppCompatActivity{
    private static final String TAG = "ActivityFeed";
    private Context context=ActivityFeed.this;
    private static final int ACTIVITY_NUM=3;
    private ViewPagerAdapter tabPage;
    private ViewPager viewPage;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        //setUpBottomNavigView();
        tabPage = new ViewPagerAdapter(getSupportFragmentManager());
        viewPage = findViewById(R.id.container);
        setUpTabView(viewPage);

        TabLayout layout = findViewById(R.id.tabs);
        layout.setupWithViewPager(viewPage);



    }
    /*
    Bottom Navigation Set up
     */

    private void setUpBottomNavigView(){
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        Log.d(TAG, "setUpBottomNavigView: "+bottomNavigationView);
        BottomNavigHelper.setUp(bottomNavigationView);
        BottomNavigHelper.NavigEnable(context,bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem mItem = menu.getItem(ACTIVITY_NUM);
        mItem.setChecked(true);
        //mItem.setEnabled(false);
        mItem.setEnabled(false);
    }

    private void setUpTabView(ViewPager page){
        tabPage.addFragment(new FollowingFragment(),"Following");
        tabPage.addFragment(new YouFragment(),"You");
        page.setAdapter(tabPage);

    }


    @Override
    protected void  onStart(){
        super.onStart();
        setUpBottomNavigView();

    }

}
