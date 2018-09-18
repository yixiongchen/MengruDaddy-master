package com.mengrudaddy.instagram;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class FullScreenCapture extends AppCompatActivity{
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreencapture);

    }
    public void close(View view) {
        finish();

    }
}
