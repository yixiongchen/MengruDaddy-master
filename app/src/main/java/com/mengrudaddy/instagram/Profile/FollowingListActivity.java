package com.mengrudaddy.instagram.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mengrudaddy.instagram.Adapter.followingUserAdapter;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;
import com.mengrudaddy.instagram.utils.BottomNavigHelper;

import java.util.ArrayList;
import java.util.Arrays;


public class FollowingListActivity extends AppCompatActivity {

    private static final String TAG = "FollowingListActivity";
    private Context context=FollowingListActivity.this;
    private static final int ACTIVITY_NUM=4;
    private User user;
    private FirebaseUser authUser;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference userRef,followingRef;
    private ValueEventListener mUserListener, mFollowingListener;
    private followingUserAdapter adapter;
    private ListView listView;
    private String[] likeIdList;
    private String uID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_following);

        //receive likeId list
        //likeIdList = getIntent().getStringArrayExtra("LikeIdList");
        uID = getIntent().getStringExtra("uId");

        //set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.title_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Following");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //real time database
        database = FirebaseDatabase.getInstance();

        //auth
        auth = FirebaseAuth.getInstance();
        authUser = auth.getCurrentUser();
        if (authUser == null) {
            finish();
        }
        //userId path

        String indexPath = "users/"+authUser.getUid();
        userRef = database.getReference(indexPath);
        //access use profile and set up adpater
        accessUserProfile();

    }



    @Override
    protected void onStart() {
        super.onStart();
        setUpBottomNavigView();

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
        //mItem.setEnabled(false);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove post value event listener
        if ( mFollowingListener != null) {
            followingRef.removeEventListener( mFollowingListener);
        }

    }

    /*
      Read the user info from database
     */
    private void accessUserProfile(){

        //read user info
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                user = dataSnapshot.getValue(User.class);

                //HashMap<String, String> map = (HashMap<String, String>) user.following;
                //ArrayList<String> followings = new ArrayList<String>(map.values());

                listView = (ListView)findViewById(R.id.listView) ;
                //set user list adapter
                accessPostProfile();
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent,
                                            View v, int position, long id){
                        // Send intent to ProfileActivity
                        String userId = (String)parent.getItemAtPosition(position);
                        Log.d(TAG, "Click item"+userId);
                        Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                        // Pass id
                        i.putExtra("id", userId);
                        startActivity(i);
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        userRef.addListenerForSingleValueEvent(userListener);
        //mUserListener = userListener;
    }

    /*
      Read the post info from database
     */
    private void accessPostProfile(){
        //read post info
        followingRef = database.getReference("users/"+uID+"/following");
        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                ArrayList<String> following = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String uid = ds.getValue(String.class);
                    following.add(uid);
                }
                Object[] objNames = following.toArray();
                String[] list = Arrays.copyOf(objNames, objNames.length, String[].class);
                adapter = new followingUserAdapter(getApplicationContext(), list, user);
                listView.setAdapter(adapter);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        followingRef.orderByKey().addValueEventListener(postListener);
        mFollowingListener = postListener;
    }


}
