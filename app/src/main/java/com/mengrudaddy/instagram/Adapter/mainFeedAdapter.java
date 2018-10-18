package com.mengrudaddy.instagram.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.mengrudaddy.instagram.Models.Post;
import com.mengrudaddy.instagram.R;

import java.util.ArrayList;

public class mainFeedAdapter  extends RecyclerView.Adapter<mainFeedAdapter.postViewHolder> {

    private Context mContext;
    private ArrayList<Post> postList;
    private FirebaseStorage storage;


    public mainFeedAdapter(Context mContext, ArrayList<Post> postList) {
        this.mContext = mContext;
        this.postList = postList;
        storage = FirebaseStorage.getInstance();
    }


    @NonNull
    @Override
    public postViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.main_feed_layout, null);
        return new mainFeedAdapter.postViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull mainFeedAdapter.postViewHolder postViewHolder, final int i) {



    }

    @Override
    public int getItemCount() {
        return postList.size();
    }


    public class postViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textViewUsername, textViewEmail;
        ProgressBar progressBar;

        public postViewHolder(@NonNull View itemView) {
            super(itemView);


        }
    }



}
