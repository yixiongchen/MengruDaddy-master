package com.mengrudaddy.instagram;

/*
UploadActivity.java
This class is activity for upload images
with fragment of choose photo from local album
or choose photo by taking a new one
 */

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.mengrudaddy.instagram.Adapter.ViewPagerAdapter;
import com.mengrudaddy.instagram.utils.BottomNavigHelper;

public class UploadActivity extends AppCompatActivity{
    private static final String TAG = "UploadActivity";
    private Context context=UploadActivity.this;

    private ViewPager viewPager;
    ConstraintLayout constraintLayout;
    AlbumFragment albumFragment;
    PhotoFragment photoFragment;
    TabLayout tabLayout;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);



        tabLayout = (TabLayout) findViewById(R.id.album_photo_tabs);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        constraintLayout = (ConstraintLayout) findViewById(R.id.coordinator);

        setUpViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);


    }

    private void setUpViewPager(ViewPager vp){
        ViewPagerAdapter adpter = new ViewPagerAdapter(getSupportFragmentManager());
        albumFragment = new AlbumFragment();
        photoFragment = new PhotoFragment();
        adpter.addFragment(albumFragment,"Library");
        adpter.addFragment(photoFragment,"Photo");
        vp.setAdapter(adpter);
    }
    public void goFilter(View view){
        Intent getFilter = new Intent(context, ImageFilter.class);
        //home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(getFilter);
    }
    public void close(View view) {
        finish();
        Log.d("UploadActivity", "Close it");
        // onBackPressed();
    }



}
