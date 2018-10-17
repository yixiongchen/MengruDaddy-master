package com.mengrudaddy.instagram.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mengrudaddy.instagram.Models.Like;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;


import java.util.HashMap;


public class likeListAdapter extends BaseAdapter {


    private String[] likeIds,userIds;
    private final Context mContext;
    private User user;
    private final String TAG ="LikeListAdapter::";
    private FirebaseDatabase database;
    private DatabaseReference likeRef;
    private ValueEventListener mlistener;

    private Like like;


    public likeListAdapter (Context context, String[] likeIds, User user){
        this.likeIds = likeIds;
        this.mContext = context;
        this.user = user;
        userIds = new String[this.likeIds.length];
    }


    // 2
    @Override
    public int getCount() {
        return likeIds.length;
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
        final String id = this.likeIds[position];

        final int index = position;



        //2
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);

            convertView = layoutInflater.inflate(R.layout.like_listview_layout, null);
            final ImageView imageView = (ImageView)convertView.findViewById(R.id.profile_image);
            final TextView username =(TextView)convertView.findViewById(R.id.username);
            final Button follow = (Button)convertView.findViewById(R.id.follow);
            final ProgressBar progressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);

            final likeListAdapter.ViewHolder viewHolder = new likeListAdapter.ViewHolder(imageView,username, follow,progressBar);
            convertView.setTag(viewHolder);
        }
        final likeListAdapter.ViewHolder viewHolder = (likeListAdapter.ViewHolder)convertView.getTag();

        //load likeId object
        //real time database
        database = FirebaseDatabase.getInstance();
        likeRef = database.getReference("likes").child(id);


        //read user info
        ValueEventListener likeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                like = dataSnapshot.getValue(Like.class);
                viewHolder.username.setText(like.username);
                userIds[index] = like.userId;
                if(user.following != null && user.following.containsValue(like.userId)){
                    viewHolder.follow.setEnabled(false);
                    viewHolder.follow.setText("Followed");
                }
                else{
                    viewHolder.follow.setText("Follow");
                    viewHolder.follow.setEnabled(true);
                    viewHolder.follow.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View view) {
                            //add user id to following list
                            DatabaseReference user_like_list = database.getReference("users/").child(user.Id).child("following");
                            String key = user_like_list.push().getKey();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put(key, like.userId);
                            user_like_list.updateChildren(map);
                            viewHolder.follow.setEnabled(false);
                            viewHolder.follow.setText("Followed");
                        }});
                }
                viewHolder.progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        likeRef.addListenerForSingleValueEvent(likeListener);
        mlistener = likeListener;
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




}
