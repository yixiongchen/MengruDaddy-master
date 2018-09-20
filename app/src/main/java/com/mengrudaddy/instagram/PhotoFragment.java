package com.mengrudaddy.instagram;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.os.Bundle;


public class PhotoFragment extends Fragment{
    private static final String TAG = "PhotoFragment";
    @Nullable
    ImageView imageView;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo,container,false);
        Log.d(TAG, "onCreatView: started.");

        Button btnLaunchCamera = (Button) view.findViewById(R.id.take);
        imageView = (ImageView)view.findViewById(R.id.new_capture);
        btnLaunchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick:launching camera.");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,0);
            }
        });

        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap =(Bitmap)data.getExtras().get("data");
        imageView.setImageBitmap(bitmap);
    }
}
