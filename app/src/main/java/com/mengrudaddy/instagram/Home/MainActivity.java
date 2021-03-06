package com.mengrudaddy.instagram.Home;


/*
MainActivity.java
This class is activity for showing photo posting activities of following users in home page
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mengrudaddy.instagram.Adapter.mainFeedAdapter;
import com.mengrudaddy.instagram.Bluetooth.BluetoothActivity;
import com.mengrudaddy.instagram.Models.Post;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;
import com.mengrudaddy.instagram.utils.BottomNavigHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity::";
    private Context context=MainActivity.this;
    private static final int ACTIVITY_NUM=0;

    //database connection
    private FirebaseDatabase database;
    private DatabaseReference postsRef, userRef;
    //auth user
    private FirebaseUser authUser;
    private FirebaseAuth auth;

    //database listener
    private ValueEventListener mUserListener, mPostsListener;

    //auth user object
    private User user;
    private ArrayList<String> posts;

    //view
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipe;
    private ImageView bluetooth;

    private static final int requestDate = 0;
    private static final int requestLocation = 1;

    private int FeedType = requestDate;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //View initialization
        recyclerView = (RecyclerView)findViewById(R.id.main_feed);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bluetooth = (ImageView) findViewById(R.id.bluetooth);
        swipe=(SwipeRefreshLayout)findViewById(R.id.swiperefresh);

        // on click bluetooth imageview start bluetooth activity
        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent activity = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(activity);
            }
        });

        swipe.setEnabled(false);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setAdapater();
            }
        });





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

        // execution order
        // 1. load user profile from database
        // 2. load posts by date from database, and then do some filters
        // 3. setting adpater for recycle view
        //read user info
        setAdapater();


    }


    /*
        load posts from database
     */
    public void accessPostsByDate(){

        //read user info
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                posts = new ArrayList<>();
                for(DataSnapshot postShot :  dataSnapshot.getChildren()){
                    Post post  = postShot.getValue(Post.class);
                    //your posts
                    if(post.userId.compareTo(user.Id)==0){
                        posts.add(post.Id);
                    }
                    //posts of your followings
                    else if(user.following !=null && user.following.containsValue(post.userId)){
                        posts.add(post.Id);
                    }
                    else{
                        continue;
                    }
                }
                Collections.reverse(posts);
                mainFeedAdapter adapter = new mainFeedAdapter(MainActivity.this, posts, user);
                recyclerView.setAdapter(adapter);
                swipe.setRefreshing(false);
                swipe.setEnabled(true);
                //start refresh

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        postsRef.orderByChild("date").addListenerForSingleValueEvent(postListener);
        mPostsListener = postListener;
    }


    /*
        load posts from database
     */
    public void accessPostsByLocation(){

        //read user info
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                posts = new ArrayList<>();
                HashMap<String, Integer> locationMap = new HashMap<>();
                for(DataSnapshot postShot :  dataSnapshot.getChildren()){
                    Post post  = postShot.getValue(Post.class);
                    //your posts
                    if(post.userId.compareTo(user.Id)==0){
                        posts.add(post.Id);
                        if(post.location == null){

                        }
                        else{
                            Double latitude = post.location.get("latitude");
                            Double longitude = post.location.get("longitude");


                        }
                    }
                    //posts of your followings
                    else if(user.following !=null && user.following.containsValue(post.userId)){
                        posts.add(post.Id);
                    }
                    else{
                        continue;
                    }
                }
                //sort by the nearest date;



                mainFeedAdapter adapter = new mainFeedAdapter(MainActivity.this, posts, user);
                recyclerView.setAdapter(adapter);
                swipe.setRefreshing(false);
                swipe.setEnabled(true);
                //start refresh

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        postsRef.orderByChild("date").addListenerForSingleValueEvent(postListener);
        mPostsListener = postListener;
    }






    /*
        Set adapter for posts
     */
    public void setAdapater(){
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                user = dataSnapshot.getValue(User.class);
                //access posts
                accessPostsByDate();


                //setting recycleView for adapter

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        userRef.addValueEventListener(userListener);
        mUserListener = userListener;

    }





    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove post value event listener
        if (mPostsListener != null) {
            postsRef.removeEventListener(mPostsListener);
        }
        if(mUserListener != null){
            userRef.removeEventListener(mUserListener);
        }
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
        mItem.setEnabled(false);
    }



    @Override
    protected void  onStart(){
        super.onStart();
        setUpBottomNavigView();

    }



}
