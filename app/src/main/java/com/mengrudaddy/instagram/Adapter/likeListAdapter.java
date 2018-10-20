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
import com.mengrudaddy.instagram.Models.Event;
import com.mengrudaddy.instagram.Models.Like;
import com.mengrudaddy.instagram.Models.Reminder;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;
import com.squareup.picasso.Picasso;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class likeListAdapter extends BaseAdapter {


    private String[] likeIds,userIds;
    private final Context mContext;
    private User user, likeUser;
    private final String TAG ="LikeListAdapter::";
    private FirebaseDatabase database;
    private Like like;

    private FirebaseStorage storage;



    public likeListAdapter (Context context, String[] likeIds, User user){
        this.likeIds = likeIds;
        this.mContext = context;
        this.user = user;
        userIds = new String[this.likeIds.length];
        storage = FirebaseStorage.getInstance();
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
        DatabaseReference likeRef = database.getReference("likes").child(id);


        //read user info
        ValueEventListener likeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                like = dataSnapshot.getValue(Like.class);
                //access username
                accessUsername(viewHolder, like.userId);
                //access profile image
                accessProfileImage(viewHolder, like.userId);
                userIds[index] = like.userId;

                //set views
                //user is AuthUser
                if(user.Id.compareTo(like.userId)==0){
                    viewHolder.follow.setVisibility(View.GONE);
                }

                // user is not in the following
                if(user.following != null && user.following.containsValue(like.userId)){
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
                            //map.put(key, like.userId);
                            user_following_list.updateChildren(map);

                            //add user id to follower list for profile user;
                            DatabaseReference user_follower_list = database.getReference("users/").child(like.userId).child("followers");
                            String followerKey = user_follower_list.push().getKey();
                            HashMap<String, Object> followermap = new HashMap<>();
                            followermap.put(followerKey, user.Id);
                            user_follower_list.updateChildren(followermap);


                            //update event notification
                            updateEvent(user, like);

                            //update reminder notification
                            updateReminder(user, like);




                            viewHolder.follow.setEnabled(false);
                            viewHolder.follow.setText("Followed");
                        }});
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        likeRef.addListenerForSingleValueEvent(likeListener);

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
    public void accessUsername(final likeListAdapter.ViewHolder viewHolder, final String userId){
        DatabaseReference userRef = database.getReference("users").child(userId);
        //read user info
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                likeUser = dataSnapshot.getValue(User.class);
                viewHolder.username.setText(likeUser.username);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        userRef.addListenerForSingleValueEvent(userListener);
    }


    /*
       load profile image
    */
    public void accessProfileImage(final likeListAdapter.ViewHolder viewHolder, final String userId){
        StorageReference profile_pic_ref = storage.getReference("profile_pic/"+userId);

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



    private void updateEvent(final User user,  final Like like ){
        //event notification updates
        Date date = new Date();
        DatabaseReference eventRef  = database.getReference("events/");
        String eventId = eventRef.push().getKey();
        HashMap<String, String> action = new HashMap<>();
        action.put("userId", user.Id);
        action.put("type", "follow");
        Event eventObject  = new Event(eventId, action, date);
        eventRef.child(eventId).setValue(eventObject);
        //add eventId to the list of target user
        DatabaseReference eventListRef = database.getReference("users/"+like.userId+"/"+"events");
        String event_list_key = eventListRef.push().getKey();
        Map<String, Object> updateEventList = new HashMap<>();
        updateEventList.put(event_list_key,eventId);
        eventListRef.updateChildren(updateEventList);
    }

    /*
        Activity Feed: update reminder for all the followers
     */
    public void updateReminder(final User user, final Like like){
        Date date = new Date();
        //update a like notification for reminder
        //create a new Reminder
        DatabaseReference reminderRef = database.getReference("reminders/");
        final String reminderId = reminderRef.push().getKey();
        HashMap<String, String> action = new HashMap<>();
        action.put("actionUserId", user.Id); //who
        action.put("targetUserId", like.userId); //on whom
        action.put("type", "follow");
        //action.put("content", content);
        Reminder reminder = new Reminder(reminderId, action, date);
        reminderRef.child(reminderId).setValue(reminder);
        if(user.followers != null){
            //for each follower, update it reminder list
            for(String follower_key : user.followers.keySet()){
                String follower_id = user.followers.get(follower_key);
                DatabaseReference reminderListRef = database.getReference("users/"+follower_id+"/"+"reminders");
                String reminder_key = reminderListRef.push().getKey();
                Map<String, Object> updateReminderList = new HashMap<>();
                updateReminderList.put(reminder_key, reminderId);
                reminderListRef.updateChildren(updateReminderList);
            }
        }
    }









}
