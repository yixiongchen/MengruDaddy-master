package com.mengrudaddy.instagram.Camera;

/**
 * Created by 50650 on 2018/9/20
 */
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.mengrudaddy.instagram.R;

public class CameraFunction extends AppCompatActivity {


    //    @Override
    //    protected void onCreate(Bundle savedInstanceState) {
    //        Launched by action_launched_capture
    //
    //        super.onCreate(savedInstanceState);
    //        setContentView(R.layout.activity_main);
    //
    //        Button btnCamera = (Button)findViewById(R.id.btnCamera);
    //        ImageView imageView = (ImageView)findViewById(R.id.imageView);
    //
    //        btnCamera.setOnClickListener(new View.OnClickListener() {
    //            @Override
    //            public void onClick(View view) {
    //                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    //                startActivityForResult(intent,0);
    //
    //
    //            }
    //        });

    ImageView imageView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCamera = (Button)findViewById(R.id.take);
//        imageView = (ImageView)findViewById(R.id.imageView);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,0);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap =(Bitmap)data.getExtras().get("data");
        imageView.setImageBitmap(bitmap);
    }
}
