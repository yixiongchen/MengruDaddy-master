package com.mengrudaddy.instagram.Profile;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.mengrudaddy.instagram.Comments.CommentsListActivity;
import com.mengrudaddy.instagram.Likes.LikesListActivity;
import com.mengrudaddy.instagram.Models.Event;
import com.mengrudaddy.instagram.Models.Like;
import com.mengrudaddy.instagram.Models.Post;
import com.mengrudaddy.instagram.Models.Reminder;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;
import com.mengrudaddy.instagram.utils.BottomNavigHelper;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class SinglePostActivity extends AppCompatActivity {

    private FirebaseStorage storage;
    private StorageReference imageReference,profile_pic_ref;
    // friebase authentication
    private FirebaseUser authUser;
    private boolean likeBoolean = false;

    //realtime database
    private FirebaseDatabase database;
    private DatabaseReference userRef, postRef;
    private final String TAG = "SinglePostActivity::";
    private ProgressBar progressBar;
    private ImageView imageView, profileImage;
    private TextView userName, numComments, numLikes, description, date,location;
    private ImageView like, comment;
    private String postId; //post id
    private Post post;
    private User user;
    private String userId;

    private ValueEventListener mPostListener, mUserListener;





    private Context context=SinglePostActivity.this;
    private static final int ACTIVITY_NUM=4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post);

        // Get intent data
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        // get post id
        postId = extras.getString("postId");
        //userId = extras.getString("userId");

        //Log.d(TAG, "postId is: "+postId);
        //Log.d(TAG, "userId is: "+userId);

        //views
        imageView = (ImageView) findViewById(R.id.image);
        profileImage = (ImageView)findViewById(R.id.profile_image);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        userName = (TextView)findViewById(R.id.username);
        //description
        description = (TextView)findViewById(R.id.photo_description);
        //number of likes
        numLikes=(TextView)findViewById(R.id.likes);
        like =(ImageView)findViewById(R.id.like);
        //number of comments
        numComments = (TextView)findViewById(R.id.comments);
        comment = (ImageView)findViewById(R.id.comment);
        //date
        date =(TextView)findViewById(R.id.date);
        location = (TextView)findViewById(R.id.location);


        Toolbar toolbar = (Toolbar) findViewById(R.id.title_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Post");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //the auth user
        authUser = FirebaseAuth.getInstance().getCurrentUser();

        //load post profile
        //real time database
        database = FirebaseDatabase.getInstance();
        //the post's path
        String indexPath = "posts/"+postId;
        postRef = database.getReference(indexPath);


        //firebase storage
        storage = FirebaseStorage.getInstance();
        //storage location: posts/postid/
        String postRef = "posts/images/";
        imageReference = storage.getReference(postRef).child(postId);
        //show the post image
        imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //picasso lib load remote image
                Picasso.with(getApplicationContext()).load(uri.toString()).into(imageView,
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


        //read post info
        accessPostProfile();


        //click event for viewing all likes
        numLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //postId to LikeListActivity
                Intent intent = new Intent(SinglePostActivity.this, LikesListActivity.class);
                intent.putExtra("postId",postId);
                startActivity(intent);

            }
        });
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
            postRef.removeEventListener(mPostListener);
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
        mItem.setEnabled(true);

    }

    /*
      Read the post info from database
     */
    private void accessPostProfile(){
        //read post info
        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                //first get the post object
                post = dataSnapshot.getValue(Post.class);
                //retrieve the userid of the poster
                userId = post.userId;
                //access the user info by userId
                accessUserProfile(userId);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        postRef.addValueEventListener(postListener);
        mPostListener = postListener;
    }


     /*
      Read the user info from database
     */
     private void accessUserProfile(String userId){

         //user listener
         ValueEventListener userListener = new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot)  {
                 //retrieve user object
                 user = dataSnapshot.getValue(User.class);
                 //set username
                 userName.setText(user.username);
                 Date postdate = post.date;

                 //set location
                 if(post.location !=null){
                     String address = getAddress(post.location.get("latitude"), post.location.get("longitude"));
                     location.setText(address);
                 }
                 else{
                     location.setVisibility(View.GONE);
                 }

                 //set dat view
                 DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
                 String todate = dateFormat.format(postdate);
                 date.setText(todate);

                 //set description
                 if(description == null){
                     description.setVisibility(View.GONE);
                 }
                 else{
                     description.setText(user.username + ": " +post.description);
                 }

                 //set num of comments
                 if(post.comments == null){
                     numComments.setVisibility(View.GONE);
                 }
                 else{
                     numComments.setVisibility(View.VISIBLE);
                     numComments.setText("View all "+Integer.toString(post.comments.keySet().size())+" Comments");
                 }

                 //set like icon
                 if(post.likes == null) {
                     numLikes.setVisibility(View.GONE);
                     likeBoolean = false;
                     like.setImageDrawable(getApplicationContext().getDrawable(R.drawable.ic_action_activity));

                 }

                 //set num of likes
                 else{
                     numLikes.setVisibility(View.VISIBLE);
                     numLikes.setText(Integer.toString(post.likes.keySet().size())+" likes");
                     if(post.likes.containsKey(authUser.getUid())){
                         likeBoolean = true;
                         like.setImageDrawable(getApplicationContext().getDrawable(R.drawable.ic_action_like));
                     }
                 }

                 //load profile image
                 profile_pic_ref = storage.getReference().child("profile_pic/"+post.userId);
                 if(user.image != null){
                     profile_pic_ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                         @Override
                         public void onSuccess(Uri uri) {
                             //picasso lib load remote image
                             Picasso.with(getApplicationContext()).load(uri.toString()).into(profileImage,
                                     new com.squareup.picasso.Callback() {
                                         @Override
                                         public void onSuccess() {
                                             //do smth when picture is loaded successfully
                                             //progressBar.setVisibility(View.GONE);
                                         }
                                         @Override
                                         public void onError() {
                                             //progressBar.setVisibility(View.GONE);
                                             //do smth when there is picture loading error
                                         }
                                     });

                         }
                     }).addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception exception) {
                             //progressBar.setVisibility(View.GONE);
                             Log.d(TAG, "Can not download file, please check connection");
                         }
                     });
                 }

                 //handle comment and like clicker
                 //click like button
                 like.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         //like object reference
                         DatabaseReference likeRef  = database.getReference("likes/");
                         DatabaseReference likeListRef = database.getReference("posts/"+postId+"/"+"likes");

                         //unlike event
                         if(likeBoolean){
                             HashMap<String, String> map =( HashMap<String, String>)post.likes;
                             String likeId = map.get(authUser.getUid());
                             //remove <userid, likeId> from likesList
                             likeListRef.child(authUser.getUid()).removeValue();
                             //remove Like object from database
                             likeRef.child(likeId).removeValue();
                             //set icon
                             likeBoolean = false;
                             like.setImageDrawable(getApplicationContext().getDrawable(R.drawable.ic_action_activity));
                             Toast.makeText(SinglePostActivity.this, "You unliked the Post!", Toast.LENGTH_SHORT).show();

                         }
                         //like event
                         else{
                             Date date = new Date();
                             //create like object
                             Like likeObject  = new Like(postId, authUser.getUid(), user.username, date);
                             //write like object to database
                             String likeId = likeRef.push().getKey();
                             likeRef.child(likeId).setValue(likeObject);

                             //add <UserId, LikeId> to the list in the post
                             Map<String, Object> updateValue = new HashMap<>();
                             updateValue.put(authUser.getUid(),likeId); //userId:likeId
                             likeListRef.updateChildren(updateValue);


                             //if like other user's post, update a like notification for event
                             if(post.userId.compareTo(authUser.getUid())!=0){
                                 DatabaseReference eventRef  = database.getReference("events/");
                                 String eventId = eventRef.push().getKey();
                                 HashMap<String, String> action = new HashMap<>();
                                 action.put("userId", authUser.getUid());
                                 action.put("type", "like");
                                 action.put("typeId", postId);
                                 Event eventObject  = new Event(eventId, action, date);
                                 eventRef.child(eventId).setValue(eventObject);
                                 //add eventId to the list of target user
                                 DatabaseReference eventListRef = database.getReference("users/"+post.userId+"/"+"events");
                                 String event_list_key = eventListRef.push().getKey();
                                 Map<String, Object> updateEventList = new HashMap<>();
                                 updateEventList.put(event_list_key,eventId);
                                 eventListRef.updateChildren(updateEventList);
                             }

                             //update a like notification for reminder
                             //create a new Reminder
                             DatabaseReference reminderRef = database.getReference("reminders/");
                             final String reminderId = reminderRef.push().getKey();
                             HashMap<String, String> action = new HashMap<>();
                             action.put("actionUserId", authUser.getUid()); //who
                             action.put("targetUserId", post.userId);                           //on whom
                             action.put("type", "like");
                             action.put("typeId", postId);
                             //action.put("content", content);
                             Reminder reminder = new Reminder(reminderId, action, date);
                             reminderRef.child(reminderId).setValue(reminder);

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


                             //set icon
                             likeBoolean = true;
                             like.setImageDrawable(getApplicationContext().getDrawable(R.drawable.ic_action_activity));
                             Toast.makeText(SinglePostActivity.this, "You Liked the Post!", Toast.LENGTH_SHORT).show();
                         }
                     }
                 });

                 //click comment button
                 comment.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         Intent intent = new Intent(SinglePostActivity.this, CommentsListActivity.class);
                         Bundle extras = new Bundle();
                         extras.putString("postId",postId);
                         extras.putString("userId", post.userId);
                         intent.putExtras(extras);
                         startActivity(intent);

                     }
                 });

                 //view all comments
                 numComments.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         Intent intent = new Intent(SinglePostActivity.this, CommentsListActivity.class);
                         Bundle extras = new Bundle();
                         extras.putString("postId",postId);
                         extras.putString("userId", post.userId);
                         intent.putExtras(extras);
                         startActivity(intent);

                     }
                 });

             }
             @Override
             public void onCancelled(DatabaseError databaseError) {}
         };
         //the user's path
         Log.d(TAG, userId);
         userRef = database.getReference("users/"+userId);
         userRef.addValueEventListener(userListener);
         mUserListener = userListener;
     }


    /*
        Return the address by coordinates
     */
    private String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                result.append(address.getLocality()).append("\n");
                result.append(address.getCountryName());
                //result.append(address.get)
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }





}
