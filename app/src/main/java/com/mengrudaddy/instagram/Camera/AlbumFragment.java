package com.mengrudaddy.instagram.Camera;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mengrudaddy.instagram.R;

public class AlbumFragment extends Fragment{
    private static final String TAG = "AlbumFragment";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album,container,false);

        ImageView btnClose = (ImageView) view.findViewById(R.id.icon_cancle);
        ImageView btnNext = (ImageView) view.findViewById(R.id.icon_next);


        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the gallery fragment.");
                close();
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Going to Filter activity.");
                redirectToFilter();
            }
        });
        return view;
    }


    public void redirectToFilter(){
        Intent getFilter = new Intent(getActivity(), ImageFilter.class);
        //bug
        startActivity(getFilter);
    }

    public void close() {
        getActivity().finish();
        Log.d("UploadActivity", "Close it");
        // onBackPressed();
    }
}
