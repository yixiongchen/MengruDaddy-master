package com.mengrudaddy.instagram.Profile;


/*
ProfileActivity.java
This class is activity for showing user profile after the user login
To Display stats on posts, followers and following, profile pic
Display all user photos uploaded
 */

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mengrudaddy.instagram.Adapter.photoAdapter;
import com.mengrudaddy.instagram.Login.LoginActivity;
import com.mengrudaddy.instagram.R;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.utils.BottomNavigHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity{
    private static final String TAG = "ProfileActivity::";
    private Context context=ProfileActivity.this;
    private static final int ACTIVITY_NUM=4;
    private TextView title, username, email, followerNum, followingNum, postNum;
    private Button editFile;
    private ProgressBar progressBar;
    private ImageView logout;
    private GridView gridview;
    private ValueEventListener mUserListener, mPostListener;

    // friebase authentication
    private FirebaseAuth auth;
    // real time database
    private FirebaseDatabase database;
    private DatabaseReference authUserRef, userRef, postListRef ;
    //photo storage
    private ArrayList<String> photoIds;

    private String profileId; //profile user id


    private FirebaseUser authUser; //auth user
    private User LogUser, ProfileUser;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);

        // Get intent data
        Intent i = getIntent();
        // get user id
        if(i.getExtras() != null){
            profileId = i.getExtras().getString("id");
        }

        //View initialization
        title=(TextView)findViewById(R.id.toolbar_userprofile);
        username = (TextView)findViewById(R.id.username);
        email = (TextView)findViewById(R.id.email);
        followerNum =  (TextView)findViewById(R.id.followers_num);
        followingNum =  (TextView)findViewById(R.id.following_num);
        postNum =  (TextView)findViewById(R.id.post_num);
        logout = (ImageView)findViewById(R.id.logout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        editFile = (Button)findViewById(R.id.textEditProfile);
        gridview = (GridView) findViewById(R.id.gridView);



        //real time database
        database = FirebaseDatabase.getInstance();

        //auth
        auth = FirebaseAuth.getInstance();
        authUser = auth.getCurrentUser();
        if (authUser == null) {
            finish();
        }

        //Profile userId path
        if(profileId == null){
            profileId = authUser.getUid();
            editFile.setText("Edit Profile");
        }
        else{
            Log.d(TAG, "View other user");
            //set toolbar
            Toolbar toolbar = (Toolbar) findViewById(R.id.title_bar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            //getSupportActionBar().setTitle("Post");
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            title.setVisibility(View.GONE);
            accessAuthUserProfile();
        }

        //access Profile user
        accessUserProfile();

        //access post info
        accessPostList();

        //logout
        logout.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                if(auth.getCurrentUser() != null){
                    auth.signOut();
                    Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                }
            }});

    }

    //access auth user info
    private void accessAuthUserProfile(){
        String indexPath = "users/"+authUser.getUid();
        authUserRef = database.getReference(indexPath);
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                LogUser = dataSnapshot.getValue(User.class);
                if(LogUser.following!=null && LogUser.following.containsValue(profileId)){
                    editFile.setText("Followed");
                    editFile.setEnabled(false);
                }
                else{
                    editFile.setText("Follow");
                    editFile.setEnabled(true);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}

        };
        authUserRef.addListenerForSingleValueEvent(userListener);
    }



    //access user'profile
    private void accessUserProfile(){
        String indexPath = "users/"+profileId;
        userRef = database.getReference(indexPath);
        //read user info
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                ProfileUser = dataSnapshot.getValue(User.class);
                title.setText(ProfileUser.username);
                username.setText(ProfileUser.username);
                email.setText(ProfileUser.email);
                if(ProfileUser.followers == null){
                    followerNum.setText("0");
                }
                else{
                    followerNum.setText(Integer.toString(ProfileUser.followers.keySet().size()));
                }
                if(ProfileUser.following == null){
                    followingNum.setText("0");
                }
                else{
                    followingNum.setText(Integer.toString(ProfileUser.following.keySet().size()));

                }
                if(ProfileUser.posts == null){
                    postNum.setText("0");
                }
                else{
                    postNum.setText(Integer.toString(ProfileUser.posts.keySet().size()));
                }
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        userRef.addValueEventListener(userListener);
        mUserListener = userListener;

    }

    //access user's posts
    private void accessPostList(){
        ValueEventListener postsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                photoIds = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String postId = ds.getValue(String.class);
                    photoIds.add(postId);
                }
                Object[] objNames = photoIds.toArray();
                String[] list = Arrays.copyOf(objNames, objNames.length, String[].class);
                photoAdapter adapter = new photoAdapter(getApplicationContext(), list);
                gridview.setAdapter(adapter);

                gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent,
                                            View v, int position, long id){
                        // Send intent to SingleViewActivity
                        String photoUrl = parent.getItemAtPosition(position).toString();
                        Intent i = new Intent(getApplicationContext(), SinglePostActivity.class);
                        // Pass image index
                        i.putExtra("id", photoUrl);
                        startActivity(i);
                    }
                });

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        postListRef =  database.getReference("users/"+profileId+"/posts");
        postListRef.orderByKey().addValueEventListener(postsListener);
        mPostListener = postsListener;

    }




    @Override
    protected void onStart() {
        super.onStart();
        setUpBottomNavigView();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove post value event listener
        if (mUserListener != null) {
            userRef.removeEventListener(mUserListener);
        }
        if(mPostListener != null){
            postListRef.removeEventListener(mPostListener);
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
}
