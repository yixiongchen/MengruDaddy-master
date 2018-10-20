package com.mengrudaddy.instagram.Camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
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
import com.google.firebase.storage.UploadTask;
import com.mengrudaddy.instagram.Home.MainActivity;
import com.mengrudaddy.instagram.Models.Like;
import com.mengrudaddy.instagram.Models.Post;
import com.mengrudaddy.instagram.Models.Reminder;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;
import com.mengrudaddy.instagram.utils.LocationGetter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ShareActivity extends AppCompatActivity {

    private Context context = ShareActivity.this;
    private EditText postContent;
    private ImageView image,btnPost;
    private TextView btnLocation;
    private String username;
    private String content, postId;
    private Double latitude,longitude;
    private Date date;
    private ArrayList<String> posts;
    private String imagePath;

    private FirebaseStorage storage;
    private DatabaseReference UserDatabaseRef;
    private DatabaseReference DatabaseRef;
    private FirebaseUser authUser;
    private StorageReference imageReference, thumbReference;
    private FirebaseDatabase database;
    private ProgressBar progressBar;
    private ValueEventListener mPostListener;
    private InputStream thumbStream;
    private Bitmap thumbBitmap;
    private byte[] thumbData;

    private static final int LOCATION_REQUEST_CODE = 1;




    private static final String TAG = "ShareActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Log.d(TAG, "start");

        //receive image intent
        String path = getIntent().getStringExtra("PostImage");
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        Log.d(TAG, path);
        imagePath = path;

        //get views
        image =  (ImageView)findViewById(R.id.imageShare);
        //ImageView btnBack =  (ImageView)findViewById(R.id.icon_back);
        postContent = (EditText) findViewById(R.id.postcontent);
        btnPost =  (ImageView)findViewById(R.id.icon_next);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnLocation = (TextView) findViewById(R.id.add_location);


        Toolbar toolbar = (Toolbar) findViewById(R.id.title_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Share");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });

        //set imageView
        image.setImageBitmap(bitmap);

        //initialize a date
        //DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        date = new Date();
        //String strDate = dateFormat.format(date).toString();

        //access user's username
        database = FirebaseDatabase.getInstance();
        authUser =FirebaseAuth.getInstance().getCurrentUser();
        UserDatabaseRef = database.getReference("users/"+ authUser.getUid());


        //access descritipn content
        content = postContent.getText().toString();

        //database location: posts/userId/postid/
        DatabaseRef = database.getReference();
        //database: create a postId into the firebase database
        postId = DatabaseRef.child("posts/").push().getKey();

        //storage location: posts/postid/
        String postRef = "posts/";

        storage = FirebaseStorage.getInstance();
        imageReference = storage.getReference(postRef).child("images/").child(postId);
        thumbReference = storage.getReference(postRef).child("thumbnails/").child(postId);

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, LocationGetter.class);//ACTIVITY_NUM=1
                startActivityForResult(intent,LOCATION_REQUEST_CODE);
            }
        });


        Log.d(TAG, content);
        btnPost.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //upload image to storage
                uploadImage();

                //write post info to database
                HashMap<String, String> comments = new  HashMap<>();
                HashMap<String, String> likes = new  HashMap<>();
                HashMap<String, Double> location = new  HashMap<>();
                content = postContent.getText().toString();

                location.put("latitude" ,latitude);
                location.put("longitude", longitude);
                writePost(postId, username, authUser.getUid(), content, location,
                        date, comments, likes);

                //update user profile : add a postId
                updateUser(authUser.getUid(), posts);

                //update reminder for followers
                updateReminder(postId);


            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode ==LOCATION_REQUEST_CODE && resultCode == RESULT_OK) {

            Bundle bd = data.getExtras();
            latitude = (double)bd.get("latitude");
            longitude = (double)bd.get("longitude");
            Log.d(TAG, "onActivityResult: "+longitude+"####"+latitude);
            if (latitude == null || longitude == null) {
                btnLocation.setText("Failed to get your location");
            }
            else {
                btnLocation.setText("Your Location\nLatitude: " + latitude + "\nLogitude: " + longitude);

            }
        }
    }

    //upload photo
    private void uploadImage() {
        //upload full images
        if(imageReference != null)
        {
            progressBar.setVisibility(View.VISIBLE);
            //disable user interaction
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            //InputStream stream = new FileInputStream(new File(imagePath));
            Uri filepath = Uri.fromFile(new File(imagePath));
            imageReference.putFile(filepath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //upload thumbnails
                            //convert image to thumbnails
                            Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
                            thumbBitmap = Bitmap.createScaledBitmap(bitmap,300,300,false);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            thumbData = baos.toByteArray();

                            //upload thumbnails
                            if(thumbData!=null){
                                thumbReference.putBytes(thumbData)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(ShareActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                Intent i = new Intent(ShareActivity.this, MainActivity.class);
                                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(i);
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressBar.setVisibility(View.GONE);
                                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                Toast.makeText(ShareActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            Toast.makeText(ShareActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
        /*

        */



    }



    //upload post info
    public void writePost(String postId, String username, String userId, String description,
                          HashMap<String, Double> location, Date date,
                          HashMap<String, String> comments,
                          HashMap<String, String> likes){

        HashMap<String, Double> locationMap = new HashMap<>();
        locationMap.put("latitude" ,latitude);
        locationMap.put("longitude", longitude);
        Post post = new Post(postId, username, userId, description,location, date, comments,likes);
        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + postId, postValues);
        DatabaseRef.updateChildren(childUpdates);

    }


    //update user's post list
    public void updateUser(String userid, ArrayList<String> posts){
        DatabaseReference ref = DatabaseRef.child("users/").child(userid+"/posts");
        String key = ref.push().getKey();
        Map<String, Object> updates = new HashMap<>();
        updates.put(key, postId);
        ref.updateChildren(updates);
    }


    @Override
    public void onStart(){
        super.onStart();
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                User newUser = dataSnapshot.getValue(User.class);
                username = newUser.username;
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        UserDatabaseRef.addValueEventListener(userListener);
        mPostListener = userListener;
    }


    @Override
    public void onStop() {
        super.onStop();
        // Remove post value event listener
        if (mPostListener != null) {
            UserDatabaseRef.removeEventListener(mPostListener);
        }
    }

    /*
        Activity Feed: update reminder for all the followers
     */
    public void updateReminder(String postId){
        Date date = new Date();
        //update a like notification for reminder
        //create a new Reminder
        DatabaseReference reminderRef = database.getReference("reminders/");
        final String reminderId = reminderRef.push().getKey();
        HashMap<String, String> action = new HashMap<>();
        action.put("actionUserId", authUser.getUid()); //who
        action.put("targetUserId", authUser.getUid());

        action.put("type", "post");
        action.put("typeId", postId);
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
