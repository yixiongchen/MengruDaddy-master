package com.mengrudaddy.instagram.Comments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mengrudaddy.instagram.Adapter.commentListAdapter;
import com.mengrudaddy.instagram.Models.Comment;
import com.mengrudaddy.instagram.Models.Event;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CommentsListActivity extends AppCompatActivity {


    private final String TAG = "CommentListActivity::";
    private Context context=CommentsListActivity.this;
    private static final int ACTIVITY_NUM=4;
    private User user;
    private FirebaseUser authUser;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference userRef, postRef;
    private ValueEventListener mPostListener;
    private commentListAdapter adapter;
    private ListView listView;
    private EditText newComment;
    private ImageView send;
    private ProgressBar progressBar;
    private String postId;
    private String postUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments_list);


        //receive postId
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        postId = extras.getString("postId");
        //receive userId
        postUserId = extras.getString("userId");
        Log.d(TAG, "get postId:: "+postId);
        //Log.d(TAG, "get userId:: "+postUserId);




        //access use profile and set up adpater
        listView = (ListView)findViewById(R.id.listView) ;
        newComment =(EditText)findViewById(R.id.newComment);
        send = (ImageView)findViewById(R.id.button);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);



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

    }


    /*
      Read the user info from database
     */
    private void accessUserProfile(){
        //userId path
        String indexPath = "users/"+authUser.getUid();
        userRef = database.getReference(indexPath);

        

        //send a comment
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Log.d(TAG, "user Id: "+postUserId);
                DatabaseReference commentRef  = database.getReference("comments/");
                DatabaseReference commentListRef = database.getReference("posts/"+postId+"/"+"comments");
                String content = newComment.getText().toString();
                if(content.trim().length() == 0){
                    Toast.makeText(CommentsListActivity.this, "Can not be Empty", Toast.LENGTH_SHORT).show();
                }
                else{
                    Date date = new Date();
                    Comment commentObject  = new Comment(authUser.getUid(),content, date);
                    //write like object to database
                    String CommentId = commentRef.push().getKey();
                    commentRef.child(CommentId).setValue( commentObject);
                    //add <UserId, CommentId> to the list in the post
                    String key = commentListRef.push().getKey();
                    Map<String, Object> updateValue = new HashMap<>();
                    updateValue.put(key,CommentId); //userId:likeId
                    commentListRef.updateChildren(updateValue);

                    //if comment other user's post
                    if(postUserId.compareTo(authUser.getUid())!=0){
                        // update a comment notification event
                        DatabaseReference eventRef  = database.getReference("events/");
                        String eventId = eventRef.push().getKey();
                        HashMap<String, String> action = new HashMap<>();
                        action.put("userId", authUser.getUid());
                        action.put("type", "comment");
                        action.put("typeId", postId);
                        action.put("content", content);
                        Event eventObject  = new Event(eventId, action, date);
                        eventRef.child(eventId).setValue(eventObject);
                        //add eventId to the list of target user
                        DatabaseReference eventListRef = database.getReference("users/"+postUserId+"/"+"events");
                        String event_list_key = eventListRef.push().getKey();
                        Map<String, Object> updateEventList = new HashMap<>();
                        updateEventList.put(event_list_key,eventId);
                        eventListRef.updateChildren(updateEventList);

                        //update a comment notification for reminder

                    }

                    newComment.setText(" ");
                    Toast.makeText(CommentsListActivity.this, "You successfully sent a Comment!", Toast.LENGTH_SHORT).show();
                }

            }

        });



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
                progressBar.setVisibility(View.GONE);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        postRef.orderByKey().addValueEventListener(postListener);
        mPostListener = postListener;
    }






}
