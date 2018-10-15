package com.mengrudaddy.instagram.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import com.squareup.picasso.Picasso;


public class photoAdapter extends BaseAdapter {

    private final Context mContext;
    private final String[] photos;
    private FirebaseStorage storage;
    private final String TAG ="photoAdapter::";

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
            final ImageView imageView = (ImageView)convertView.findViewById(R.id.imageview_photo);
            final ProgressBar progressBar = (ProgressBar)convertView.findViewById(R.id.ImageProgress);
            final ViewHolder viewHolder = new ViewHolder(imageView,progressBar);
            convertView.setTag(viewHolder);

        }

        final ViewHolder viewHolder = (ViewHolder)convertView.getTag();


        StorageReference photoRef = storage.getReference("posts/thumbnails/"+photoUrl);

        //final long ONE_MEGABYTE = 1024 * 1024;
        photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                //picasso lib load remote image
                Picasso.with(mContext).load(uri.toString()).into(viewHolder.imageViewPhoto,
                        new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                //do smth when picture is loaded successfully
                                viewHolder.progressBar.setVisibility(View.GONE);
                            }
                            @Override
                            public void onError() {
                                //do smth when there is picture loading error
                            }
                        });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "Can not download file, please check connection");
            }
        });

        return convertView;
    }


    private class ViewHolder {

        private final ImageView imageViewPhoto;
        private final ProgressBar progressBar;


        public ViewHolder(ImageView imageViewPhoto, ProgressBar progressBar) {
            this.imageViewPhoto = imageViewPhoto;
            this.progressBar = progressBar;

        }
    }

}
