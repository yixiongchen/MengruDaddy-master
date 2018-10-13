package com.mengrudaddy.instagram.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mengrudaddy.instagram.R;

import java.util.ArrayList;

public class photoAdapter extends BaseAdapter {

    private final Context mContext;
    private final String[] photos;
    private FirebaseStorage storage;

    // 1
    public photoAdapter(Context context, String[] photos) {
        this.mContext = context;
        this.photos = photos;
        storage = FirebaseStorage.getInstance();


    }

    // 2
    @Override
    public int getCount() {
        return photos.length;
    }

    // 3
    @Override
    public long getItemId(int position) {
        return position;
    }

    // 4
    @Override
    public Object getItem(int position) {
        return photos[position];
    }

    // 5
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //1
        final String photoUrl = photos[position];


        //2
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.grid_layout, null);
        }
        //3

        final ImageView imageView = (ImageView)convertView.findViewById(R.id.imageview_cover_art);

        final ProgressBar progressBar = (ProgressBar)convertView.findViewById(R.id.ImageProgress);

        StorageReference photoRef = storage.getReference("posts/"+photoUrl);

        final long ONE_MEGABYTE = 2000 * 2000;
        photoRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                //Bitmap bitmap = BitmapFactory.decodeResource(convertView.getResources(), R.drawable.dad);
                Bitmap thumbImg = Bitmap.createScaledBitmap(bitmap,300,300,false);
                Log.d("photoAdapter::", "Hello");
                imageView.setImageBitmap(thumbImg);
                progressBar.setVisibility(View.GONE);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });


        //Bitmap bitmap = BitmapFactory.decodeResource(mContext, R.drawable.dad);

        //Bitmap thumbImg = Bitmap.createScaledBitmap(bitmap,300,300,false);

        //imageView.setImageBitmap(thumbImg);

        return convertView;
    }

}
