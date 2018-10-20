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
import android.util.Log;
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
import com.mengrudaddy.instagram.Adapter.youTabAdapter;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YouFragment extends Fragment{

    private static final String TAG = "YouFragment";

    // element about layout
    private RecyclerView mResultList;
    private youTabAdapter adapter;
    private ValueEventListener mEventListener;

    private FirebaseAuth auth;
    private FirebaseUser authUser; //auth user

    private User user;

    // element about database
    private DatabaseReference userRef, eventsRef;
    private final FirebaseDatabase database =  FirebaseDatabase.getInstance();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_you,container,false);
        mResultList = (RecyclerView) view.findViewById(R.id.you_activities);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(getContext()));


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

        //user list
        return view;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove event listener
        if(mEventListener != null){
            eventsRef.removeEventListener(mEventListener);
        }
    }

    /*
        read events list of the user
     */
    private void accessEvents(){

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                ArrayList<String> events = new ArrayList<>();
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    String eventId = singleSnapshot.getValue(String.class);
                    events.add(eventId);
                }
                //order of events
                Collections.reverse(events);
                adapter = new youTabAdapter(getContext(), events, user);
                // set adapter
                mResultList.setAdapter(adapter);
                // set item animator to DefaultAnimator
                mResultList.setItemAnimator(new DefaultItemAnimator());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        eventsRef = database.getReference("users").child(authUser.getUid()).child("events");
        eventsRef.orderByKey().addValueEventListener(eventListener);
        mEventListener = eventListener;
    }
}
