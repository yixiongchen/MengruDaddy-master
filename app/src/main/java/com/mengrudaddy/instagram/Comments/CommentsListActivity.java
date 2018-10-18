package com.mengrudaddy.instagram.Comments;

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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mengrudaddy.instagram.Adapter.commentListAdapter;
import com.mengrudaddy.instagram.Adapter.likeListAdapter;
import com.mengrudaddy.instagram.Models.Comment;
import com.mengrudaddy.instagram.Models.Like;
import com.mengrudaddy.instagram.Models.Post;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.Profile.ProfileActivity;
import com.mengrudaddy.instagram.Profile.SinglePostActivity;
import com.mengrudaddy.instagram.R;
import com.mengrudaddy.instagram.utils.BottomNavigHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CommentsListActivity extends AppCompatActivity {


    private final String TAG = "LikesListActivity::";
    private Context context=CommentsListActivity.this;
    private static final int ACTIVITY_NUM=4;
    private User user;
    private FirebaseUser authUser;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference userRef,postRef;
    private ValueEventListener mUserListener, mPostListener;
    private commentListAdapter adapter;
    private ListView listView;
    private EditText newComment;
    private ImageButton send;

    private String[] commentIdList;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments_list);

        //receive commentId list
        commentIdList = getIntent().getStringArrayExtra("CommentIdList");
        //receive postId
        postId = getIntent().getStringExtra("postId");

        //access use profile and set up adpater
        listView = (ListView)findViewById(R.id.listView) ;
        newComment =(EditText)findViewById(R.id.newComment);
        send = (ImageButton)findViewById(R.id.button);


        //set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.title_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Who Comments");
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

        accessPostProfile();
        //set user list adapter
        accessUserProfile();
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
      Read the user info from database
     */
    private void accessUserProfile(){
        //userId path
        String indexPath = "users/"+authUser.getUid();
        userRef = database.getReference(indexPath);
        //read user info
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                user = dataSnapshot.getValue(User.class);
                //initialize send button
                send.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        DatabaseReference commentRef  = database.getReference("comments/");
                        DatabaseReference commentListRef = database.getReference("posts/"+postId+"/"+"comments");
                        String content = newComment.getText().toString();
                        if(content.trim().length() == 0){
                            Toast.makeText(CommentsListActivity.this, "Can not be Empty", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Comment commentObject  = new Comment(authUser.getUid(),user.username,content, new Date());
                            //write like object to database
                            String CommentId = commentRef.push().getKey();
                            commentRef.child(CommentId).setValue( commentObject);

                            //add <UserId, LikeId> to the list in the post
                            String key = commentListRef.push().getKey();
                            Map<String, Object> updateValue = new HashMap<>();
                            updateValue.put(key,CommentId); //userId:likeId
                            commentListRef.updateChildren(updateValue);
                            newComment.setText(" ");
                            Toast.makeText(CommentsListActivity.this, "You successfully sent a Comment!", Toast.LENGTH_SHORT).show();
                        }

                    }

                });

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        userRef.addValueEventListener(userListener);
        mUserListener = userListener;

    }


    /*
      Read the post info from database
     */
    private void accessPostProfile(){
        //read post info
        postRef = database.getReference("posts/"+postId+"/comments");
        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                ArrayList<String> comments = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String commentId = ds.getValue(String.class);
                    comments.add(commentId);
                }
                Object[] objNames = comments.toArray();
                String[] list = Arrays.copyOf(objNames, objNames.length, String[].class);
                adapter = new commentListAdapter(getApplicationContext(), list);
                listView.setAdapter(adapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        postRef.orderByKey().addValueEventListener(postListener);
        mPostListener = postListener;
    }






}
