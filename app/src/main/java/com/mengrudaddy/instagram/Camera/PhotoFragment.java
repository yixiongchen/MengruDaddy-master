package com.mengrudaddy.instagram.Camera;

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

import com.mengrudaddy.instagram.R;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;


public class PhotoFragment extends Fragment{
    private static final String TAG = "PhotoFragment";
    private static final int CAMERA_REQUEST_CODE = 0;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo,container,false);
        Log.d(TAG, "onCreatView: started.");

        Button btnLaunchCamera = (Button) view.findViewById(R.id.take);
        //imageView = (ImageView)view.findViewById(R.id.new_capture);
        btnLaunchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick:launching camera.");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,CAMERA_REQUEST_CODE);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d(TAG, "show result");
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            Log.d(TAG, "passing image to filter");
            Bitmap bitmap =(Bitmap)data.getExtras().get("data");

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            byte[] image = stream.toByteArray();

            Intent intent = new Intent(getActivity(), ImageFilter.class);

            intent.putExtra("picture", image);
            startActivity(intent);
            //getActivity().finish();

        }

    }


}
