package com.mengrudaddy.instagram.utils;

/*
BottomSwitchHelper.java
This class is activity for help on listening click event on main bottome menu navigation
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;
import com.mengrudaddy.instagram.ActivityFeed;
import com.mengrudaddy.instagram.MainActivity;
import com.mengrudaddy.instagram.ProfileActivity;
import com.mengrudaddy.instagram.R;
import com.mengrudaddy.instagram.SearchActivity;
import com.mengrudaddy.instagram.UploadActivity;

/*
 Created by mengru
 Bottom Naviga helper
 */
public class BottomNavigHelper {
    private static final String TAG = "BottomNavigHelper";

    public static void setUp(BottomNavigationView bt_navig) {
        Log.d(TAG, "setUpBottomNavigView: "+bt_navig);


    }
    public static void NavigEnable(final Context context, BottomNavigationView bt_navig){
        bt_navig.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id=menuItem.getItemId();
                switch (id){
                    case R.id.action_home:
                        Intent home = new Intent(context, MainActivity.class); //ACTIVITY_NUM=0
                        //home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(home);
                        break;
                    case R.id.action_search:
                        Intent search = new Intent(context, SearchActivity.class);//ACTIVITY_NUM=1
                        //search.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(search);
                        break;
                    case R.id.action_upload:
                        Intent upload = new Intent(context, UploadActivity.class);//ACTIVITY_NUM=2
                        context.startActivity(upload);
                        ((Activity)context).overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                        break;
                    case R.id.action_activity:
                        Intent activity = new Intent(context, ActivityFeed.class);//ACTIVITY_NUM=3
                        //activity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(activity);
                        break;
                    case R.id.action_profile:
                        Intent profile = new Intent(context, ProfileActivity.class);//ACTIVITY_NUM=4
                        //profile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(profile);
                        break;

                }
                return true;
            }
        });

    }

}
