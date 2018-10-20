package com.mengrudaddy.instagram.Adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;


public class followerUserAdapter extends BaseAdapter {


    private String[] followerIds,userIds;
    private final Context mContext;
    private User user, followerUser;
    private static final String TAG = "followerUserAdapter";
    private FirebaseDatabase database;
    private DatabaseReference followerRef,userRef;
    private FirebaseStorage storage;

    private User itemUser;


    public followerUserAdapter (Context context, String[] followerIds, User user){
        this.followerIds = followerIds;
        this.mContext = context;
        this.user = user;
        userIds = new String[this.followerIds.length];
        storage = FirebaseStorage.getInstance();
    }


    // 2
    @Override
    public int getCount() {
        return followerIds.length;
    }

    // 3
    @Override
    public long getItemId(int position) {
        return position;
    }

    // 4
    @Override
    public Object getItem(int position) {

        return userIds[position];
    }


    // 5
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String id = this.followerIds[position];

        final int index = position;




        //2
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);

            convertView = layoutInflater.inflate(R.layout.like_listview_layout, null);
            final ImageView imageView = (ImageView)convertView.findViewById(R.id.profile_image);
            final TextView username =(TextView)convertView.findViewById(R.id.username);
            final Button follow = (Button)convertView.findViewById(R.id.follow);
            final ProgressBar progressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);

            final followerUserAdapter.ViewHolder viewHolder = new followerUserAdapter.ViewHolder(imageView,username, follow,progressBar);
            convertView.setTag(viewHolder);
        }
        final followerUserAdapter.ViewHolder viewHolder = (followerUserAdapter.ViewHolder)convertView.getTag();

        //load likeId object
        //real time database
        database = FirebaseDatabase.getInstance();
        followerRef = database.getReference("users").child(id);


        //read user info
        ValueEventListener likeListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot)  {
                itemUser = dataSnapshot.getValue(User.class);

                //access username
                accessUsername(viewHolder);
                //access profile image
                accessProfileImage(viewHolder);
                userIds[index] = itemUser.Id;

                //user is AuthUser
                if(user.Id.compareTo(itemUser.Id)==0){
                    viewHolder.follow.setVisibility(View.GONE);
                }
                // user is not in the following
                if(user.following != null && user.following.containsValue(itemUser.Id)){
                    viewHolder.follow.setEnabled(false);
                    viewHolder.follow.setText("Followed");
                }
                // user is not in the following
                else{
                    viewHolder.follow.setText("Follow");
                    viewHolder.follow.setEnabled(true);
                    viewHolder.follow.setTag(index);
                    viewHolder.follow.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View view) {
                            //add user id to following list
                            DatabaseReference user_following_list = database.getReference("users/").child(user.Id).child("following");
                            String key = user_following_list.push().getKey();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put(key,userIds[Integer.parseInt(viewHolder.follow.getTag().toString())]);
                            user_following_list.updateChildren(map);

                            //add user id to follower list for profile user;
                            DatabaseReference user_follower_list = database.getReference("users/").child(itemUser.Id).child("followers");
                            String followerKey = user_follower_list.push().getKey();
                            HashMap<String, Object> followermap = new HashMap<>();
                            followermap.put(followerKey, user.Id);
                            user_follower_list.updateChildren(followermap);

                            viewHolder.follow.setEnabled(false);
                            viewHolder.follow.setText("Followed");
                        }});
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        followerRef.addListenerForSingleValueEvent(likeListener);

        return convertView;
    }



    private class ViewHolder {

        private final ImageView imageViewPhoto;
        private final ProgressBar progressBar;
        private final TextView username;
        private final Button follow;



        public ViewHolder(ImageView imageViewPhoto,TextView username, Button follow,
                          ProgressBar progressBar) {
            this.imageViewPhoto = imageViewPhoto;
            this.username = username;
            this.follow= follow;
            this.progressBar = progressBar;

        }
    }

    /*
        read username from user profile
     */
    public void accessUsername(final followerUserAdapter.ViewHolder viewHolder){
        userRef = database.getReference("users").child(itemUser.Id);
        //read user info
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                followerUser = dataSnapshot.getValue(User.class);
                viewHolder.username.setText(followerUser.username);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        userRef.addListenerForSingleValueEvent(userListener);
    }


    /*
       load profile image
    */
    public void accessProfileImage(final followerUserAdapter.ViewHolder viewHolder){
        StorageReference profile_pic_ref = storage.getReference("profile_pic/"+itemUser.Id);

        profile_pic_ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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

                                viewHolder.progressBar.setVisibility(View.GONE);
                            }
                        });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                viewHolder.progressBar.setVisibility(View.GONE);
                viewHolder.imageViewPhoto.setImageResource(R.drawable.ic_action_face);
                Log.d(TAG, "Can not download file, please check connection");
            }
        });
    }







}
