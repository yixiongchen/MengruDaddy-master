package com.mengrudaddy.instagram;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.mengrudaddy.instagram.utils.BottomSwitchHelper;

public class Album extends AppCompatActivity{
    private static final String TAG = "Photo";
    private static final int ACTIVITY_NUM=0;
    private Context context=Album.this;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        //overridePendingTransition(R.anim.slide_left, R.anim.slide_right);


    }

    public void close(View view) {
        finish();

    }
    public void switch_upload_mode(View view) {
        int id=view.getId();
        BottomSwitchHelper.SwitchEnable(context,id);
    }
}
