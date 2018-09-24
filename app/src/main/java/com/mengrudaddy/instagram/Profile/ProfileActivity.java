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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mengrudaddy.instagram.Login.LoginActivity;
import com.mengrudaddy.instagram.R;
import com.mengrudaddy.instagram.models.User;
import com.mengrudaddy.instagram.utils.BottomNavigHelper;

public class ProfileActivity extends AppCompatActivity{
    private static final String TAG = "ProfileActivity";
    private Context context=ProfileActivity.this;
    private static final int ACTIVITY_NUM=4;
    private TextView title, username, email;
    private ProgressBar progressBar;
    private ImageView logout;

    private ValueEventListener mPostListener;

    // friebase authentication
    private FirebaseAuth auth;
    // real time database
    private FirebaseDatabase database;
    private DatabaseReference userRef;

    private FirebaseUser user;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);


        //View initialization
        title=(TextView)findViewById(R.id.toolbar_userprofile);
        username = (TextView)findViewById(R.id.username);
        email = (TextView)findViewById(R.id.email);
        logout = (ImageView)findViewById(R.id.logout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        //auth
        user = auth.getCurrentUser();
        if (user == null) {
            finish();
        }
        String indexPath = "users/"+user.getUid();
        userRef = database.getReference(indexPath);

        //setUpBottomNavigView();

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

    @Override
    protected void onStart() {
        super.onStart();
        setUpBottomNavigView();
        //read user info
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                User newUser = dataSnapshot.getValue(User.class);
                title.setText(newUser.username);
                username.setText(newUser.username);
                email.setText(newUser.email);
                //Log.d("Test for datachange:", "okok");
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        userRef.addValueEventListener(userListener);
        mPostListener = userListener;

    }


    @Override
    public void onStop() {
        super.onStop();
        // Remove post value event listener
        if (mPostListener != null) {
            userRef.removeEventListener(mPostListener);
        }
    }




}
