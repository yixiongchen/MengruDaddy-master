package com.mengrudaddy.instagram;

import android.content.Context;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.mengrudaddy.instagram.utils.BottomNavigHelper;
import com.mengrudaddy.instagram.utils.TabAdapter;

public class ActivityFeed extends AppCompatActivity{
    private static final String TAG = "ActivityFeed";
    private Context context=ActivityFeed.this;
    private static final int ACTIVITY_NUM=3;
    private TabAdapter tabPage;
    private ViewPager viewPage;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        setUpBottomNavigView();
        tabPage = new TabAdapter(getSupportFragmentManager());
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
    }

    private void setUpTabView(ViewPager page){
        //TabAdapter tab = new TabAdapter(getSupportFragmentManager());
        tabPage.addFragment(new Tab1Following(),"Following");
        tabPage.addFragment(new Tab2You(),"You");
        page.setAdapter(tabPage);

    }
}
