package com.mengrudaddy.instagram.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class followingTabAdapter extends RecyclerView.Adapter<followingTabAdapter.userViewHolder>{
    private Context mCtx;
    private List<User> userList;

    public followingTabAdapter(Context mCtx, int list_layout, List<User> userList) {
        this.mCtx = mCtx;
        this.userList = userList;
    }
    @NonNull
    @Override
    public userViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.list_following, null);
        return new userViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull userViewHolder userViewHolder, int i) {
        final User user = userList.get(i);

        // for testing layout, can be deleted
        userViewHolder.name.setText("Mack Daddy");
        userViewHolder.action.setText("liked Jack's post/start to folowing Jack ");
    }

    public int getItemCount() {
        return userList.size();
    }

    // for view initialize
    public class userViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profile_pic;
        ImageView post;
        TextView name, action;

        public userViewHolder(@NonNull View itemView) {
            super(itemView);

            profile_pic = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.name);
            action = itemView.findViewById(R.id.action);
            post = itemView.findViewById(R.id.post);
            post.setVisibility(View.VISIBLE);

        }
    }

}
