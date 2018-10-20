package com.mengrudaddy.instagram.Following;

/*
Tab1Following.java
This class is activity for tab 'following'
To show that following activities of users that current user are following
 */

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mengrudaddy.instagram.Adapter.followingTabAdapter;
import com.mengrudaddy.instagram.Adapter.youTabAdapter;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FollowingFragment extends Fragment{

    private static final String TAG = "FollowingFragment";

    // element about layout
    private RecyclerView mResultList;
    private followingTabAdapter adapter;
    private ValueEventListener mReminderListener;

    private FirebaseAuth auth;
    private FirebaseUser authUser; //auth user

    private User user;

    // element about database
    private DatabaseReference userRef, remindersRef;
    private final FirebaseDatabase database =  FirebaseDatabase.getInstance();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_following,container,false);
        mResultList = (RecyclerView) view.findViewById(R.id.following_activities);
        mResultList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new
                LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mResultList.setLayoutManager(linearLayoutManager);


        //auth
        auth = FirebaseAuth.getInstance();
        authUser = auth.getCurrentUser();

        if (authUser == null) {
            getActivity().finish();
        }

        //read the event lists of user from database
        //read user info
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                user= dataSnapshot.getValue(User.class);
                accessEvents();

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        userRef = database.getReference("users").child(authUser.getUid());
        userRef.addListenerForSingleValueEvent(userListener);


        return view;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove event listener
        if(mReminderListener != null){
            remindersRef.removeEventListener(mReminderListener);
        }
    }


    /*
        read events list of the user
     */
    private void accessEvents(){

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                ArrayList<String> reminders = new ArrayList<>();
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    String reminderId = singleSnapshot.getValue(String.class);
                    reminders.add(reminderId);
                }
                //order of events
                Collections.reverse(reminders);
                adapter = new followingTabAdapter(getContext(), reminders, user);
                // set adapter
                mResultList.setAdapter(adapter);
                // set item animator to DefaultAnimator
                mResultList.setItemAnimator(new DefaultItemAnimator());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        remindersRef = database.getReference("users").child(authUser.getUid()).child("reminders");
        remindersRef.orderByKey().addValueEventListener(eventListener);
        mReminderListener = eventListener;
    }

}
