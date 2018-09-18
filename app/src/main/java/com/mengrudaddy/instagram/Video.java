package com.mengrudaddy.instagram;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.mengrudaddy.instagram.utils.BottomSwitchHelper;

public class Video extends AppCompatActivity{
    private static final String TAG = "Video";
    private static final int ACTIVITY_NUM=2;
    private Context context=Video.this;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        //overridePendingTransition(R.anim.slide_up, R.anim.slide_down);


    }

    public void close(View view) {
        finish();

    }

    public void switch_upload_mode(View view) {
        int id=view.getId();
        BottomSwitchHelper.SwitchEnable(context,id);
    }

}
