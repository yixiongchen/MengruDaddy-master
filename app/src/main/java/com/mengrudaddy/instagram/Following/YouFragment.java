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
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mengrudaddy.instagram.Adapter.youTabAdapter;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;

import java.util.ArrayList;
import java.util.List;

public class YouFragment extends Fragment{

    private static final String TAG = "YouFragment";

    EditText editTextName;
    ImageButton buttonSearch;

    RecyclerView mResultList;
    youTabAdapter adapter;
    final FirebaseDatabase database =  FirebaseDatabase.getInstance();

    DatabaseReference databaseUsers;
    List<User> testList;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_you,container,false);
        mResultList = (RecyclerView) view.findViewById(R.id.you_activities);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(getContext()));

        testList = new ArrayList<>();
        for (int i=0;i<10;i++){
            testList.add(new User());
        }

        adapter = new youTabAdapter(getContext(),R.layout.list_following,testList);
        // 4. set adapter
        mResultList.setAdapter(adapter);
        // 5. set item animator to DefaultAnimator
        mResultList.setItemAnimator(new DefaultItemAnimator());
        return view;
    }
}
