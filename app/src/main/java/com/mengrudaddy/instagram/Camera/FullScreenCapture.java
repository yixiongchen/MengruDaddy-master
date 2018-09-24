package com.mengrudaddy.instagram.Camera;


/*
FullScreenCapture.java
This class is activity to take photo by clicking camera icon in left top corner in home page,
photo taken in full screen
 */

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mengrudaddy.instagram.R;

public class FullScreenCapture extends AppCompatActivity{
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreencapture);

    }

    //close the activity view
    public void close(View view) {
        finish();

    }
}
