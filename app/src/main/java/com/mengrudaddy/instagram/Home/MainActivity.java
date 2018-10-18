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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mengrudaddy.instagram.Adapter.mainFeedAdapter;
import com.mengrudaddy.instagram.Adapter.userListAdapter;
import com.mengrudaddy.instagram.Camera.FullScreenCapture;
import com.mengrudaddy.instagram.Models.Post;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;
import com.mengrudaddy.instagram.Search.SearchActivity;
import com.mengrudaddy.instagram.utils.BottomNavigHelper;

import java.util.ArrayList;
import java.util.Collections;


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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //View initialization
        recyclerView = (RecyclerView)findViewById(R.id.main_feed);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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



        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                user = dataSnapshot.getValue(User.class);
                //access posts
                accessPosts();
                //setting recycleView for adapter

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        userRef.addListenerForSingleValueEvent(userListener);
        mUserListener = userListener;

    }


    /*
        load posts from database
     */
    public void accessPosts(){

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
                    else if(user.following.containsValue(post.userId)){
                        posts.add(post.Id);
                    }
                    else{
                        continue;
                    }
                }
                Collections.reverse(posts);
                mainFeedAdapter adapter = new mainFeedAdapter(MainActivity.this, posts, user);
                recyclerView.setAdapter(adapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        postsRef.orderByChild("date").addListenerForSingleValueEvent(postListener);
        mPostsListener = postListener;

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
