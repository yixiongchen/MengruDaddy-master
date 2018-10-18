package com.mengrudaddy.instagram.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.Profile.ProfileActivity;
import com.mengrudaddy.instagram.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class userListAdapter extends RecyclerView.Adapter<userListAdapter.userViewHolder>{

    private Context mCtx;
    private List<User> userList;
    private FirebaseStorage storage;


    public userListAdapter(Context mCtx, int list_layout, List<User> userList) {
        this.mCtx = mCtx;
        this.userList = userList;
        storage = FirebaseStorage.getInstance();
    }


    @NonNull
    @Override
    public userViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.list_layout, null);
        return new userViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull userViewHolder userViewHolder, final int i) {
        final User user = userList.get(i);

        userViewHolder.textViewUsername.setText(user.username);
        userViewHolder.textViewEmail.setText(user.email);
//        Glide.with(mCtx).load(user.image).into(userViewHolder.imageView);

        //load profile image
        accessProfileImage(userViewHolder, user);
        userViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(v.getContext(), i, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mCtx, ProfileActivity.class);
                String userId = userList.get(i).Id;
                intent.putExtra("id", userId);
                mCtx.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class userViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textViewUsername, textViewEmail;
        ProgressBar progressBar;

        public userViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.profile_image);
            textViewUsername = itemView.findViewById(R.id.name_text);
            textViewEmail = itemView.findViewById(R.id.email_text);
            progressBar = itemView.findViewById(R.id.progressBar);

        }
    }

    /*
       load profile image
    */
    public void accessProfileImage(final userListAdapter.userViewHolder viewHolder, User user){
        StorageReference profile_pic_ref = storage.getReference("profile_pic/"+user.Id);

        profile_pic_ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //picasso lib load remote image
                Picasso.with(mCtx).load(uri.toString()).into(viewHolder.imageView,
                        new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                viewHolder.progressBar.setVisibility(View.GONE);
                                //do smth when picture is loaded successfully
                                //viewHolder.progressBar.setVisibility(View.GONE);
                            }
                            @Override
                            public void onError() {
                                viewHolder.progressBar.setVisibility(View.GONE);
                                //do smth when there is picture loading error
                                //viewHolder.progressBar.setVisibility(View.GONE);
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                viewHolder.progressBar.setVisibility(View.GONE);
                //viewHolder.progressBar.setVisibility(View.GONE);
                //Log.d(TAG, "Can not download file, please check connection");
            }
        });
    }

}
