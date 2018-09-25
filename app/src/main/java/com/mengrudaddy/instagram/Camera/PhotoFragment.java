package com.mengrudaddy.instagram.Camera;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import com.mengrudaddy.instagram.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;


public class PhotoFragment extends Fragment{
    private static final String TAG = "PhotoFragment";
    private static final int CAMERA_REQUEST_CODE = 0;

    private String mCurrentPhotoPath;
    private String dir;
    private Uri imageUri;

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
                //dispatchTakePictureIntent();
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());

                takePicture();

            }
        });

        return view;
    }


    public void takePicture(){
        //access external storage
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + "MobileIns";
        File destDir = new File(path);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        //create image file
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String file = "Ins"+timeStamp+".jpg";
        File photoFile =new File(destDir,file);


        if (photoFile != null) {
            Uri outputUri = Uri.fromFile(photoFile);
            imageUri = outputUri;
            Log.d(TAG, imageUri.getPath());

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d(TAG, "show result");
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            if(imageUri!=null){
                String path = imageUri.getPath();
                Log.d(TAG, "Image Url is"+path);
                //File imagefile = new File(path);
                Intent intent = new Intent(getActivity(), ImageFilter.class);
                intent.putExtra("picture", path);
                startActivity(intent);

            }


            //Bitmap bitmap =(Bitmap)data.getExtras().get("data");
            //ByteArrayOutputStream stream = new ByteArrayOutputStream();
            //bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            //byte[] image = stream.toByteArray();
            //Intent intent = new Intent(getActivity(), ImageFilter.class);
            //intent.putExtra("picture", image);
            //startActivity(intent);
            //getActivity().finish();

        }

    }



}
