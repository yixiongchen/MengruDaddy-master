package com.mengrudaddy.instagram.Profile;


/*
ProfileActivity.java
This class is activity for showing user profile after the user login
To Display stats on posts, followers and following, profile pic
Display all user photos uploaded
 */

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.mengrudaddy.instagram.Models.Event;
import com.mengrudaddy.instagram.Models.Like;
import com.mengrudaddy.instagram.Models.Reminder;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;
import com.mengrudaddy.instagram.utils.BottomNavigHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity{
    private static final String TAG = "ProfileActivity::";
    private Context context=ProfileActivity.this;
    private static final int ACTIVITY_NUM=4;
    private TextView title, username, email, followerNum, followingNum, postNum,description;
    private Button editFile;
    private ProgressBar progressBar;
    private ImageView logout;
    private GridView gridview;
    private CircleImageView profile_pic;
    private ValueEventListener mUserListener, mPostListener;

    // friebase authentication
    private FirebaseAuth auth;
    private FirebaseUser authUser; //auth user
    // real time database
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private DatabaseReference authUserRef, userRef, postListRef;
    private StorageReference profile_pic_ref;
    //photo storage
    private ArrayList<String> photoIds;
    private String profileId; //profile user id


    private User LogUser, ProfileUser;
    private final int EDIT_PROFILE_REQUEST_CODE=1;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);

        // Get intent data
        Intent i = getIntent();

        //View initialization
        title=(TextView)findViewById(R.id.toolbar_userprofile);
        username = (TextView)findViewById(R.id.username);
        email = (TextView)findViewById(R.id.email);
        description = (TextView)findViewById(R.id.description) ;
        followerNum =  (TextView)findViewById(R.id.followers_num);
        followingNum =  (TextView)findViewById(R.id.following_num);
        postNum =  (TextView)findViewById(R.id.post_num);
        logout = (ImageView)findViewById(R.id.logout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        editFile = (Button)findViewById(R.id.textEditProfile);
        gridview = (GridView) findViewById(R.id.gridView);
        profile_pic = (CircleImageView) findViewById(R.id.profile_image) ;

        //real time database
        database = FirebaseDatabase.getInstance();
        // get storage
        storage = FirebaseStorage.getInstance();
        //auth
        auth = FirebaseAuth.getInstance();
        authUser = auth.getCurrentUser();
        if (authUser == null) {
            finish();
        }

        // get user id
        if(i.getExtras() != null
                && authUser.getUid().compareTo(i.getExtras().getString("id"))!=0){
            profileId = i.getExtras().getString("id");
            logout.setVisibility(View.GONE);
        }

        //Personal profile
        if(profileId == null){
            profileId = authUser.getUid();
            editFile.setText("Edit Profile");
            editFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, EditProfileActivity.class);
                    startActivity(intent);
                }
            });
        }
        //view other profile
        else{
            Log.d(TAG, "View other user");
            //set toolbar
            Toolbar toolbar = (Toolbar) findViewById(R.id.title_bar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
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
        profile_pic_ref = storage.getReference().child("profile_pic/"+profileId);
        accessUserProfile();

        //access post info
        accessPostList();

        // show following users
        followingNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FollowingListActivity.class);
                intent.putExtra("uId",profileId);
                startActivity(intent);
            }
        });
        followerNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FollowerListActivity.class);
                intent.putExtra("uId",profileId);
                startActivity(intent);
            }
        });

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
                    //click follow button
                    editFile.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View view) {
                            //add user id to following list for authUser
                            DatabaseReference user_following_list = database.getReference("users/").child(authUser.getUid()).child("following");
                            String key = user_following_list.push().getKey();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put(key, profileId);
                            user_following_list.updateChildren(map);
                            //add user id to follower list for profile user;
                            DatabaseReference user_follower_list = database.getReference("users/").child(profileId).child("followers");
                            String followerKey = user_follower_list.push().getKey();
                            HashMap<String, Object> followermap = new HashMap<>();
                            followermap.put(followerKey, authUser.getUid());
                            user_follower_list.updateChildren(followermap);


                            //event notification updates
                            updateEvent();

                            //reminder notification update
                            updateReminder();



                            editFile.setEnabled(false);
                            editFile.setText("Followed");


                        }
                    });

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
                //set title name
                if(profileId.compareTo(authUser.getUid())!=0){
                    getSupportActionBar().setTitle(username.getText().toString());
                }
                if(ProfileUser.email != null){
                    email.setText(ProfileUser.email);
                }
                description.setText(ProfileUser.description);
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
                //load profile image
                if(ProfileUser.image != null){
                    profile_pic_ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //picasso lib load remote image
                            Picasso.with(getApplicationContext()).load(uri.toString()).into(profile_pic,
                                    new com.squareup.picasso.Callback() {
                                        @Override
                                        public void onSuccess() {
                                            //do smth when picture is loaded successfully
                                            progressBar.setVisibility(View.GONE);
                                        }
                                        @Override
                                        public void onError() {
                                            //do smth when there is picture loading error
                                        }
                                    });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d(TAG, "Can not download file, please check connection");
                        }
                    });

                }
                else{
                    progressBar.setVisibility(View.GONE);
                }
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
                        Bundle extras = new Bundle();
                        extras.putString("postId", photoUrl);
                        //extras.putString("userId", profileId);
                        i.putExtras(extras);
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
        if(authUser.getUid().compareTo(profileId) == 0){
            mItem.setEnabled(false);
        }
    }

    /*
       Activity Feed: update events for the user
    */
    public void updateEvent(){
        Date date = new Date();
        DatabaseReference eventRef  = database.getReference("events/");
        String eventId = eventRef.push().getKey();
        HashMap<String, String> action = new HashMap<>();
        action.put("userId", authUser.getUid());
        action.put("type", "follow");
        //action.put("typeId", post.Id);
        Event eventObject  = new Event(eventId, action, date);
        eventRef.child(eventId).setValue(eventObject);
        //add eventId to the list of target user
        DatabaseReference eventListRef = database.getReference("users/"+profileId+"/"+"events");
        String event_list_key = eventListRef.push().getKey();
        Map<String, Object> updateEventList = new HashMap<>();
        updateEventList.put(event_list_key,eventId);
        eventListRef.updateChildren(updateEventList);
    }

    /*
       Activity Feed: update reminder for all the followers
    */
    public void updateReminder(){
        Date date = new Date();
        //update a like notification for reminder
        //create a new Reminder
        DatabaseReference reminderRef = database.getReference("reminders/");
        final String reminderId = reminderRef.push().getKey();
        HashMap<String, String> action = new HashMap<>();
        action.put("actionUserId", authUser.getUid()); //who
        action.put("targetUserId", profileId); //on whom
        action.put("type", "follow");
        //action.put("content", content);
        Reminder reminder = new Reminder(reminderId, action, date);
        reminderRef.child(reminderId).setValue(reminder);

        //retrieve authUser
        //retrieve authenticate user's follower list
        //user listener
        ValueEventListener authUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user.followers != null){
                    //for each follower, update it reminder list
                    for(String follower_key : user.followers.keySet()){
                        String follower_id = user.followers.get(follower_key);
                        DatabaseReference reminderListRef = database.getReference("users/"+follower_id+"/"+"reminders");
                        String reminder_key = reminderListRef.push().getKey();
                        Map<String, Object> updateReminderList = new HashMap<>();
                        updateReminderList.put(reminder_key, reminderId);
                        reminderListRef.updateChildren(updateReminderList);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        DatabaseReference authUserRef = database.getReference("users").child(authUser.getUid());
        authUserRef.addListenerForSingleValueEvent(authUserListener);


    }
}
