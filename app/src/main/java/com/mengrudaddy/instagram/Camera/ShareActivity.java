package com.mengrudaddy.instagram.Camera;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mengrudaddy.instagram.Home.MainActivity;
import com.mengrudaddy.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.widget.ProgressBar;

import java.io.File;
import java.util.UUID;


public class ShareActivity extends AppCompatActivity {

    private FirebaseStorage storage;
    private DatabaseReference mDatabase;
    private FirebaseUser authUser;
    private StorageReference storageReference;
    private String imagePath;
    private ProgressBar progressBar;
    private static final String TAG = "ShareActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        //receive image intent
        String path = getIntent().getStringExtra("PostImage");
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        Log.d(TAG, path);
        imagePath = path;


        ImageView image =  (ImageView)findViewById(R.id.imageShare);
        //ImageView btnBack =  (ImageView)findViewById(R.id.icon_back);
        ImageView btnPost =  (ImageView)findViewById(R.id.icon_next);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

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

        //set imageView
        image.setImageBitmap(bitmap);

        //send image to fireBase storage
        storage = FirebaseStorage.getInstance();
        authUser =FirebaseAuth.getInstance().getCurrentUser();
        //posts/userId/postid/
        String userRef = "posts/"+authUser.getUid();
        storageReference = storage.getReference(userRef).child(UUID.randomUUID().toString());

        //send post info to firebase database
        String key = mDatabase.child("posts").push().getKey();

        btnPost.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                uploadPost();
            }
        });

    }

    //upload post
    private void uploadPost() {



        if(storageReference != null)
        {
            progressBar.setVisibility(View.VISIBLE);
            //disable user interaction
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            //InputStream stream = new FileInputStream(new File(imagePath));
            Uri filepath = Uri.fromFile(new File(imagePath));
            storageReference.putFile(filepath)
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

}
