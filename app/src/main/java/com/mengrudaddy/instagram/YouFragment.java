package com.mengrudaddy.instagram;

/*
Tab2You.java
This class is activity for tab 'you'
To Display users following that liked photos or started following user
 */

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class YouFragment extends Fragment{
    private static final String TAG = "YouFragment";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_you,container,false);
        return view;
    }
}
