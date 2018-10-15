package com.mengrudaddy.instagram.Profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
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
import com.mengrudaddy.instagram.Adapter.photoAdapter;
import com.mengrudaddy.instagram.Camera.ShareActivity;
import com.mengrudaddy.instagram.Models.Post;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;
import com.mengrudaddy.instagram.utils.BottomNavigHelper;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class SinglePostActivity extends AppCompatActivity {

    private FirebaseStorage storage;
    private StorageReference imageReference;
    // friebase authentication
    private FirebaseAuth auth;
    private FirebaseUser authUser;
    private FirebaseDatabase database;
    private DatabaseReference postRef;
    private final String TAG = "SinglePostActivity::";
    private ProgressBar progressBar;
    private ImageView imageView;
    private TextView userName, numComments, numLikes, description, date;
    private ImageView like, comment;
    private ValueEventListener mPostListener;
    private String id;

    private Context context=SinglePostActivity.this;
    private static final int ACTIVITY_NUM=4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post);

        // Get intent data
        Intent i = getIntent();
        // get post id
        id = i.getExtras().getString("id");

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


        authUser =auth.getInstance().getCurrentUser();
        //load post profile
        //real time database
        database = FirebaseDatabase.getInstance();
        //file path
        String indexPath = "posts/"+id;
        postRef = database.getReference(indexPath);


        //firebase storage
        storage = FirebaseStorage.getInstance();
        //storage location: posts/postid/
        String postRef = "posts/images/";
        imageReference = storage.getReference(postRef).child(id);
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

        //click like button

        like.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //DatabaseReference likeref = database.getReference("posts/"+id+"/likes");
                //String key = likeref.push().getKey();
                //Map<String, Object> updateValue = new HashMap<>();
               // updateValue.put(key,authUser.getUid());
                //likeref.updateChildren(updateValue);
                like.setImageDrawable(getApplicationContext().getDrawable(R.drawable.ic_action_like));
                Toast.makeText(SinglePostActivity.this, "You Liked the Post!", Toast.LENGTH_SHORT).show();
                //record like

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
                //go to add a comment activity

            }
        });

        //view all likes
        //view all comments
        numLikes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //go to add a comment activity

            }
        });


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
        mItem.setEnabled(false);

    }


    public void accessPostProfile(){
        //read user info
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                Post post = dataSnapshot.getValue(Post.class);
                description.setText(post.username + ": " +post.description);
                userName.setText(post.username);
                Date postdate = post.date;
                DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
                String todate = dateFormat.format(postdate);
                date.setText(todate);
                if(post.comments == null){
                    numComments.setVisibility(View.GONE);
                }
                else{
                    numComments.setText("View all "+Integer.toString(post.comments.size())+" Comments");
                }
                if(post.likes == null){
                    numLikes.setVisibility(View.GONE);
                }
                else{
                    numLikes.setText(Integer.toString(post.likes.size())+" likes");
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        postRef.addValueEventListener(userListener);
        mPostListener = userListener;
    }
}
