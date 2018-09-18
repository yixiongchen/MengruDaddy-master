package com.mengrudaddy.instagram;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mengrudaddy.instagram.utils.BottomSwitchHelper;

public class Photo extends AppCompatActivity{
    private static final String TAG = "Photo";
    private static final int ACTIVITY_NUM=1;
    private Context context=Photo.this;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        //overridePendingTransition(R.anim.slide_left, R.anim.slide_right);


    }

    public void close(View view) {
        finish();
       // onBackPressed();

    }
    public void switch_upload_mode(View view) {
        int id=view.getId();
        BottomSwitchHelper.SwitchEnable(context,id);
    }
    public void goFilter(View view){
        Intent getFilter = new Intent(context, ImageFilter.class);
        //home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(getFilter);
    }
}
