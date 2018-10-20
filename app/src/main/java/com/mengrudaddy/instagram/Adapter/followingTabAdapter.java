package com.mengrudaddy.instagram.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mengrudaddy.instagram.Models.Reminder;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.Profile.ProfileActivity;
import com.mengrudaddy.instagram.Profile.SinglePostActivity;
import com.mengrudaddy.instagram.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class followingTabAdapter extends RecyclerView.Adapter<followingTabAdapter.userViewHolder>{
    private Context mContext;
    private ArrayList<String> reminders;
    private User user, TargetUser;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private String TAG ="followingTabAdapter::";

    public followingTabAdapter(Context mContext, ArrayList<String> reminders, User user) {
        this.mContext = mContext;
        this.reminders = reminders;
        this.user = user;
        storage = FirebaseStorage.getInstance();
        database =FirebaseDatabase.getInstance();
    }
    @NonNull
    @Override
    public userViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_following, null);
        return new userViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull final userViewHolder userViewHolder, int i) {
        final String reminderId = reminders.get(i);
        //read reminder
        //load event
        ValueEventListener reminderListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Reminder reminder= dataSnapshot.getValue(Reminder.class);
                final HashMap<String, String> action = reminder.action;

                //load profile Image
                accessProfileImage(userViewHolder, action.get("actionUserId"));

                //load action userName
                accessActionUser(userViewHolder, action.get("actionUserId"));

                //load target userName
                accessTargetUser(userViewHolder,action.get("targetUserId"), action);

                //demo part
                //load targte user image
                if(action.get("type").compareTo("follow")==0){
                    userViewHolder.userImage.setVisibility(View.VISIBLE);
                    //load target user profile image
                    accessUserImage(userViewHolder, action.get("targetUserId"));
                }
                //load post image
                else{
                    userViewHolder.post.setVisibility(View.VISIBLE);
                    //load post image
                    accessPostImage(userViewHolder,action.get("typeId"));
                }



                //set clicker for profile image
                userViewHolder.profile_pic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(v.getContext(), i, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(mContext, ProfileActivity.class);
                        intent.putExtra("id", action.get("actionUserId"));
                        mContext.startActivity(intent);
                    }
                });

                //set clicker for post image
                userViewHolder.post.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(v.getContext(), i, Toast.LENGTH_SHORT).show();
                        if(action !=null){
                            Intent intent = new Intent(mContext, SinglePostActivity.class);
                            Bundle extras = new Bundle();
                            extras.putString("postId", action.get("typeId"));
                            extras.putString("userId", action.get("targetUserId"));
                            intent.putExtras(extras);
                            //Log.d(TAG, "postId is:: "+action.get("typeId"));
                            //Log.d(TAG, "userId is:: "+action.get("userId"));
                            mContext.startActivity(intent);
                        }
                    }
                });

                //set clicker for follow Image
                userViewHolder.userImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(v.getContext(), i, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(mContext, ProfileActivity.class);
                        intent.putExtra("id", action.get("targetUserId"));
                        mContext.startActivity(intent);
                    }

                });







            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        DatabaseReference reminderRef = database.getReference("reminders").child(reminderId);
        reminderRef.addValueEventListener(reminderListener);


    }

    public int getItemCount() {
        return reminders.size();
    }

    // for view initialize
    public class userViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profile_pic, userImage;
        ImageView post;
        TextView name, action;

        public userViewHolder(@NonNull View itemView) {
            super(itemView);
            profile_pic = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.name);
            action = itemView.findViewById(R.id.action);
            post = itemView.findViewById(R.id.post);
            userImage = itemView.findViewById(R.id.userImage);
            userImage.setVisibility(View.GONE);
            post.setVisibility(View.GONE);

        }
    }


    /*
    load profile image
   */
    public void accessProfileImage(final followingTabAdapter.userViewHolder viewHolder, final String userId){
        StorageReference profile_pic_ref = storage.getReference("profile_pic/"+userId);

        profile_pic_ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //picasso lib load remote image
                Picasso.with(mContext).load(uri.toString()).into(viewHolder.profile_pic);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //viewHolder.progressBar.setVisibility(View.GONE);
                //Log.d(TAG, "Can not download file, please check connection");
            }
        });
    }


    /*
     load profile image of the Target user
   */
    public void accessUserImage(final followingTabAdapter.userViewHolder viewHolder, final String userId){
        StorageReference profile_pic_ref = storage.getReference("profile_pic/"+userId);

        profile_pic_ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //picasso lib load remote image
                Picasso.with(mContext).load(uri.toString()).into(viewHolder.userImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //viewHolder.progressBar.setVisibility(View.GONE);
                //Log.d(TAG, "Can not download file, please check connection");
            }
        });
    }





    /*
    read username of user who commits the action
    */
    private void accessActionUser(final followingTabAdapter.userViewHolder viewHolder, final String userId){
        DatabaseReference userRef = database.getReference("users").child(userId);
        //read user info
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                user = dataSnapshot.getValue(User.class);
                viewHolder.name.setText(user.username);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        userRef.addListenerForSingleValueEvent(userListener);
    }


    /*
       read username of TargetUser (action type is follow)
    */

    private void accessTargetUser(final followingTabAdapter.userViewHolder viewHolder, final String userId,
                              final HashMap<String,String> action){
        DatabaseReference userRef = database.getReference("users").child(userId);
        //read user info
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                TargetUser = dataSnapshot.getValue(User.class);
                //action type is comment
                if(action.get("type").compareTo("comment")==0){

                    viewHolder.action.setText("commented "+TargetUser.username+"'s post: "+ action.get("content"));
                }
                else if(action.get("type").compareTo("like")==0){
                    viewHolder.action.setText("liked "+TargetUser.username +"'s post");

                }
                else if(action.get("type").compareTo("follow")==0){
                    viewHolder.action.setText("start to follow "+TargetUser.username);
                }
                //post
                else{
                    viewHolder.action.setText("shared a new post");
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        userRef.addListenerForSingleValueEvent(userListener);
    }


    /*
       access post image
    */
    private void accessPostImage(final followingTabAdapter.userViewHolder viewHolder, final String postId){
        StorageReference post_pic_ref = storage.getReference("posts/thumbnails/"+postId);

        post_pic_ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //picasso lib load remote image
                Picasso.with(mContext).load(uri.toString()).into(viewHolder.post);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //viewHolder.progressBar.setVisibility(View.GONE);
                //Log.d(TAG, "Can not download file, please check connection");
            }
        });

    }





}
