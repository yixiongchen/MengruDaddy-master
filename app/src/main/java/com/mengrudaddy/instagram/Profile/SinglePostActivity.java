package com.mengrudaddy.instagram.Profile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.mengrudaddy.instagram.Likes.LikesListActivity;
import com.mengrudaddy.instagram.Models.Like;
import com.mengrudaddy.instagram.Models.Post;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;
import com.mengrudaddy.instagram.utils.BottomNavigHelper;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class SinglePostActivity extends AppCompatActivity {

    private FirebaseStorage storage;
    private StorageReference imageReference;
    // friebase authentication
    private FirebaseUser authUser;
    private boolean likeBoolean = false;

    //realtime database
    private FirebaseDatabase database;
    private DatabaseReference userRef, postRef;
    private final String TAG = "SinglePostActivity::";
    private ProgressBar progressBar;
    private ImageView imageView;
    private TextView userName, numComments, numLikes, description, date;
    private ImageView like, comment;
    private String postId; //post id
    private Post post;
    private User user;


    private ValueEventListener mPostListener, mUserListener;





    private Context context=SinglePostActivity.this;
    private static final int ACTIVITY_NUM=4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post);

        // Get intent data
        Intent i = getIntent();
        // get post id
        postId = i.getExtras().getString("id");

        //views
        imageView = (ImageView) findViewById(R.id.image);
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

        //set toolbar
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


        authUser = FirebaseAuth.getInstance().getCurrentUser();
        //load post profile
        //real time database
        database = FirebaseDatabase.getInstance();
        //the post's path
        String indexPath = "posts/"+postId;
        postRef = database.getReference(indexPath);
        //the user's path
        userRef = database.getReference("users/"+authUser.getUid());


        //firebase storage
        storage = FirebaseStorage.getInstance();
        //storage location: posts/postid/
        String postRef = "posts/images/";
        imageReference = storage.getReference(postRef).child(postId);
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
        //read user info
        accessUserProfile();
        //read post info
        accessPostProfile();

        //click like button
        like.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //like object reference
                DatabaseReference likeRef  = database.getReference("likes/");
                DatabaseReference likeListRef = database.getReference("posts/"+postId+"/"+"likes");
                //unllike event
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
                    //create like object
                    Like likeObject  = new Like(postId, authUser.getUid(), user.username, new Date());
                    //write like object to database
                    String likeId = likeRef.push().getKey();
                    likeRef.child(likeId).setValue(likeObject);

                    //add <UserId, LikeId> to the list in the post
                    Map<String, Object> updateValue = new HashMap<>();
                    updateValue.put(authUser.getUid(),likeId); //userId:likeId
                    likeListRef.updateChildren(updateValue);

                    //set icon
                    likeBoolean = true;
                    like.setImageDrawable(getApplicationContext().getDrawable(R.drawable.ic_action_activity));
                    Toast.makeText(SinglePostActivity.this, "You Liked the Post!", Toast.LENGTH_SHORT).show();

                }
            }
        });

        //click comment button
        comment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //go to add a comment activity

            }
        });

        //view all comments
        numComments.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


            }
        });

        //view all likes
        numLikes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                 //pass a list of likeId to LikeListActivity
                HashMap<String, String> likesIdMap = (HashMap<String, String>) post.likes;
                Collection<String> likesIdList = likesIdMap.values();
                Object[] objectArray  = likesIdList.toArray();
                String[] likeIds = Arrays.copyOf(objectArray, objectArray.length, String[].class);

                Intent intent = new Intent(SinglePostActivity.this, LikesListActivity.class);
                intent.putExtra("LikeIdList",likeIds);
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
        //mItem.setCheckable(false);
        mItem.setEnabled(false);

    }

    /*
      Read the post info from database
     */
    private void accessPostProfile(){
        //read post info
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                post = dataSnapshot.getValue(Post.class);
                userName.setText(post.username);
                Date postdate = post.date;
                DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
                String todate = dateFormat.format(postdate);
                date.setText(todate);
                if(description == null){
                    description.setVisibility(View.GONE);
                }
                else{
                    description.setText(post.username + ": " +post.description);
                }
                if(post.comments == null){
                    numComments.setVisibility(View.GONE);
                }
                else{
                    numComments.setText("View all "+Integer.toString(post.comments.keySet().size())+" Comments");
                }

                if(post.likes == null){
                    numLikes.setVisibility(View.GONE);
                }
                else{
                    numLikes.setVisibility(View.VISIBLE);
                    numLikes.setText(Integer.toString(post.likes.keySet().size())+" likes");
                    if(post.likes.containsKey(authUser.getUid())){
                        likeBoolean = true;
                        like.setImageDrawable(getApplicationContext().getDrawable(R.drawable.ic_action_like));
                    }
                }

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
     private void accessUserProfile(){

         //read user info
         ValueEventListener userListener = new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot)  {
                 user = dataSnapshot.getValue(User.class);

             }
             @Override
             public void onCancelled(DatabaseError databaseError) {}
         };
         userRef.addValueEventListener(userListener);
         mUserListener = userListener;

     }





}
