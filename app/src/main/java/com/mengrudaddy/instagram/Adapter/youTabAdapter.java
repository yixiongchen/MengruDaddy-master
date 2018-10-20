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
import android.widget.Button;
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
import com.mengrudaddy.instagram.Models.Event;
import com.mengrudaddy.instagram.Models.Like;
import com.mengrudaddy.instagram.Models.Reminder;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.Profile.ProfileActivity;
import com.mengrudaddy.instagram.Profile.SinglePostActivity;
import com.mengrudaddy.instagram.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class youTabAdapter extends RecyclerView.Adapter<youTabAdapter.userViewHolder>{
    private Context mContext;
    private ArrayList<String> events;
    private User user, EventUser;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private String TAG ="youTabAdapter::";



    public youTabAdapter(Context mContext, ArrayList<String> events, User user) {
        this.mContext = mContext;
        this.events = events;
        this.user = user;
        storage = FirebaseStorage.getInstance();
        database =FirebaseDatabase.getInstance();
    }

    @NonNull
    @Override
    public userViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_you, null);
        return new userViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final userViewHolder userViewHolder, int i) {
        final String eventId = events.get(i);

        //load event
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                Event event= dataSnapshot.getValue(Event.class);
                final HashMap<String, String> action = event.action;

                //load profile Image
                accessProfileImage(userViewHolder, action.get("userId"));

                //load userName
                accessUsername(userViewHolder, action.get("userId"));

                //set view
                //action type is like
                if(action.get("type").compareTo("like")==0){
                    userViewHolder.action.setText("liked your Post");
                    //show post image
                    userViewHolder.post.setVisibility(View.VISIBLE);
                    accessPostImage(userViewHolder, action.get("typeId"));
                }
                //action type is comment
                else if(action.get("type").compareTo("comment")==0){
                    userViewHolder.action.setText("commented: "+action.get("content"));
                    //show post image
                    userViewHolder.post.setVisibility(View.VISIBLE);
                    accessPostImage(userViewHolder, action.get("typeId"));
                }
                //action type is follow
                else{
                    userViewHolder.action.setText("start to follow you");
                    userViewHolder.follow.setVisibility(View.VISIBLE);
                    //hanlde follow button
                    handleFollow(userViewHolder, action.get("userId"), user);

                }


                //set clicker for profile image
                userViewHolder.profile_pic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(v.getContext(), i, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(mContext, ProfileActivity.class);
                        intent.putExtra("id", action.get("userId"));
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
                            extras.putString("userId", action.get("userId"));
                            intent.putExtras(extras);
                            Log.d(TAG, "postId is:: "+action.get("typeId"));
                            //Log.d(TAG, "userId is:: "+action.get("userId"));
                            mContext.startActivity(intent);
                        }
                    }
                });

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        DatabaseReference eventRef = database.getReference("events").child(eventId);
        eventRef.addValueEventListener(eventListener);
    }


    public int getItemCount() {
        return events.size();
    }

    // on view initialize
    public class userViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profile_pic;
        ImageView post;
        TextView name, action;
        Button follow;

        public userViewHolder(@NonNull View itemView) {
            super(itemView);
            profile_pic = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.name);
            action = itemView.findViewById(R.id.action);
            post = itemView.findViewById(R.id.post);
            follow = itemView.findViewById(R.id.follow);
            post.setVisibility(View.GONE);
            follow.setVisibility(View.GONE);
        }
    }


    /*
     load profile image
    */
    public void accessProfileImage(final youTabAdapter.userViewHolder viewHolder, final String userId){
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
     read username from user profile
  */
    private void accessUsername(final youTabAdapter.userViewHolder viewHolder, final String userId){
        DatabaseReference userRef = database.getReference("users").child(userId);
        //read user info
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                EventUser = dataSnapshot.getValue(User.class);
                viewHolder.name.setText(EventUser.username);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        userRef.addListenerForSingleValueEvent(userListener);
    }


    /*
        access post image
     */
    private void accessPostImage(final youTabAdapter.userViewHolder viewHolder, final String postId){
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

    /*
        handle follow
     */

    private void handleFollow(final youTabAdapter.userViewHolder viewHolder, final String userId,
                              final User user){
        //in following list
        if(user.following!=null && user.following.containsValue(userId)){
            viewHolder.follow.setText("followed");
            viewHolder.follow.setEnabled(false);
        }
        // not in following list
        else{
            viewHolder.follow.setText("Follow");
            viewHolder.follow.setEnabled(true);
            viewHolder.follow.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {

                    //add user id to following list
                    DatabaseReference user_following_list = database.getReference("users/").child(user.Id).child("following");
                    String key = user_following_list.push().getKey();
                    HashMap<String, Object> map = new HashMap<>();
                    map.put(key, userId);
                    user_following_list.updateChildren(map);

                    //add user id to follower list for profile user;
                    DatabaseReference user_follower_list = database.getReference("users/").child(userId).child("followers");
                    String followerKey = user_follower_list.push().getKey();
                    HashMap<String, Object> followermap = new HashMap<>();
                    followermap.put(followerKey, user.Id);
                    user_follower_list.updateChildren(followermap);



                    //event notification updates (For You)
                    Date date = new Date();
                    DatabaseReference eventRef  = database.getReference("events/");
                    String eventId = eventRef.push().getKey();
                    HashMap<String, String> action = new HashMap<>();
                    action.put("userId", user.Id);
                    action.put("type", "follow");
                    //action.put("typeId", post.Id);
                    Event eventObject  = new Event(eventId, action, date);
                    eventRef.child(eventId).setValue(eventObject);
                    //add eventId to the list of target user
                    DatabaseReference eventListRef = database.getReference("users/"+userId+"/"+"events");
                    String event_list_key = eventListRef.push().getKey();
                    Map<String, Object> updateEventList = new HashMap<>();
                    updateEventList.put(event_list_key,eventId);
                    eventListRef.updateChildren(updateEventList);


                    //reminder notification updates (For Followings)
                    updateReminder(user, userId);




                    viewHolder.follow.setEnabled(false);
                    viewHolder.follow.setText("Followed");
                }

            });

        }
    }


    /*
        Activity Feed: update reminder for all the followers
     */
    public void updateReminder(final User user, final String userId) {
        Date date = new Date();
        //update a like notification for reminder
        //create a new Reminder
        DatabaseReference reminderRef = database.getReference("reminders/");
        final String reminderId = reminderRef.push().getKey();
        HashMap<String, String> action = new HashMap<>();
        action.put("actionUserId", user.Id); //who
        action.put("targetUserId", userId); //on whom
        action.put("type", "follow");
        //action.put("content", content);
        Reminder reminder = new Reminder(reminderId, action, date);
        reminderRef.child(reminderId).setValue(reminder);
        if (user.followers != null) {
            //for each follower, update it reminder list
            for (String follower_key : user.followers.keySet()) {
                String follower_id = user.followers.get(follower_key);
                DatabaseReference reminderListRef = database.getReference("users/" + follower_id + "/" + "reminders");
                String reminder_key = reminderListRef.push().getKey();
                Map<String, Object> updateReminderList = new HashMap<>();
                updateReminderList.put(reminder_key, reminderId);
                reminderListRef.updateChildren(updateReminderList);
            }
        }

    }


}
