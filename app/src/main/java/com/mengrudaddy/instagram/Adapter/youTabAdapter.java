package com.mengrudaddy.instagram.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by 50650 on 2018/10/17
 */

public class youTabAdapter extends RecyclerView.Adapter<youTabAdapter.userViewHolder>{
    private Context mCtx;
    private List<User> userList;

    public youTabAdapter(Context mCtx, int list_layout, List<User> userList) {
        this.mCtx = mCtx;
        this.userList = userList;
    }

    @NonNull
    @Override
    public userViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.list_you, null);
        return new userViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull userViewHolder userViewHolder, int i) {
        final User user = userList.get(i);
        if (i>3) {

            userViewHolder.name.setText("Mack Daddy");
            userViewHolder.action.setText("start to folowing you");
        }
        else {
            userViewHolder.name.setText("Lady Gaga");
            userViewHolder.action.setText("like your post");
            userViewHolder.follow.setVisibility(View.GONE);
            userViewHolder.post.setVisibility(View.VISIBLE);
        }
    }

    public int getItemCount() {
        return userList.size();
    }

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
            post.setVisibility(View.INVISIBLE);
            follow.setVisibility(View.VISIBLE);

        }
    }

}
