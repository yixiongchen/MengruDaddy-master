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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
    private static final String TAG = "ProfileActivity";
    private Context context=ProfileActivity.this;
    private static final int ACTIVITY_NUM=4;
    private TextView title, username, email, followerNum, followingNum, postNum;
    private ProgressBar progressBar;
    private ImageView logout;
    private GridView gridview;

    private ValueEventListener mPostListener;

    // friebase authentication
    private FirebaseAuth auth;
    // real time database
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    //photo storage
    private ArrayList<String> photoIds;


    private FirebaseUser user;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);

        //View initialization
        title=(TextView)findViewById(R.id.toolbar_userprofile);
        username = (TextView)findViewById(R.id.username);
        email = (TextView)findViewById(R.id.email);
        followerNum =  (TextView)findViewById(R.id.followers_num);
        followingNum =  (TextView)findViewById(R.id.following_num);
        postNum =  (TextView)findViewById(R.id.post_num);
        logout = (ImageView)findViewById(R.id.logout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        gridview = (GridView) findViewById(R.id.gridView);



        //real time database
        database = FirebaseDatabase.getInstance();

        //auth
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            finish();
        }
        //userId path
        String indexPath = "users/"+user.getUid();
        userRef = database.getReference(indexPath);


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
        accessUseProfile();
        accessPostList();

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

    /*
    Read Database
     */
    private void accessUseProfile(){
        //read user info
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                User newUser = dataSnapshot.getValue(User.class);
                title.setText(newUser.username);
                username.setText(newUser.username);
                email.setText(newUser.email);
                if(newUser.followers == null){
                    followerNum.setText("0");
                }
                else{
                    followerNum.setText(Integer.toString(newUser.followers.keySet().size()));
                }
                if(newUser.following == null){
                    followingNum.setText("0");
                }
                else{
                    followingNum.setText(Integer.toString(newUser.following.keySet().size()));

                }
                if(newUser.posts == null){
                    postNum.setText("0");
                }
                else{
                    postNum.setText(Integer.toString(newUser.posts.keySet().size()));
                }

                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        userRef.addValueEventListener(userListener);
        mPostListener = userListener;

    }


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
        DatabaseReference postListRef =  database.getReference("users/"+user.getUid()+"/posts");
        postListRef.orderByKey().addValueEventListener(postsListener);


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
        if (mPostListener != null) {
            userRef.removeEventListener(mPostListener);
        }
    }




}
