package com.mengrudaddy.instagram.Home;


/*
MainActivity.java
This class is activity for showing photo posting activities of following users in home page
 */

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mengrudaddy.instagram.Camera.FullScreenCapture;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;
import com.mengrudaddy.instagram.utils.BottomNavigHelper;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Context context=MainActivity.this;
    private static final int ACTIVITY_NUM=0;

    //database connection
    private FirebaseDatabase database;
    private DatabaseReference postsRef, userRef;
    //auth user
    private FirebaseUser authUser;
    private FirebaseAuth auth;

    //auth user object
    private User user;


    //view
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //View initialization
        recyclerView = (RecyclerView)findViewById(R.id.main_feed);

        //auth
        auth = FirebaseAuth.getInstance();
        authUser = auth.getCurrentUser();
        if (authUser == null) {
            finish();
        }
        //database setting
        database = FirebaseDatabase.getInstance();

        //reference path
        postsRef = database.getReference("posts");
        userRef = database.getReference("users").child(authUser.getUid());

        




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
        //mItem.setCheckable(false);
        mItem.setEnabled(false);

    }
    public void openFullScreenCamera(View view){
        Intent activity = new Intent(MainActivity.this, FullScreenCapture.class);
        startActivity(activity);
    }


    @Override
    protected void  onStart(){
        super.onStart();
        setUpBottomNavigView();

    }



}
