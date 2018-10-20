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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mengrudaddy.instagram.Adapter.followingTabAdapter;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;

import java.util.ArrayList;
import java.util.List;

public class FollowingFragment extends Fragment{

    private static final String TAG = "FollowingFragment";

    // element about layout
    private RecyclerView mResultList;
    private followingTabAdapter adapter;

    // element about database
    private DatabaseReference databaseUsers;
    private final FirebaseDatabase database =  FirebaseDatabase.getInstance();
    private List<User> testList;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_following,container,false);
        mResultList = (RecyclerView) view.findViewById(R.id.following_activities);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(getContext()));

        testList = new ArrayList<>();
        for (int i=0;i<10;i++){
            testList.add(new User());
        }


        adapter = new followingTabAdapter(getContext(),R.layout.list_following,testList);
        // set adapter
        mResultList.setAdapter(adapter);
        // set item animator to DefaultAnimator
        mResultList.setItemAnimator(new DefaultItemAnimator());
        return view;
    }
}
