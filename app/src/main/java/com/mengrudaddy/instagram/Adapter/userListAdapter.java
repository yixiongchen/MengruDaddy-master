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

/**
 * Created by 50650 on 2018/10/17
 */
public class userListAdapter extends RecyclerView.Adapter<userListAdapter.userViewHolder>{

    public userListAdapter(Context mCtx, int list_layout, List<User> userList) {
        this.mCtx = mCtx;
        this.userList = userList;
    }

    private Context mCtx;
    private List<User> userList;


    @NonNull
    @Override
    public userViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.list_layout, null);
        return new userViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull userViewHolder userViewHolder, int i) {
        User user = userList.get(i);

        userViewHolder.textViewUsername.setText(user.username);
        userViewHolder.textViewEmail.setText(user.email);
//        Glide.with(mCtx).load(user.image).into(userViewHolder.imageView);

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class userViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textViewUsername, textViewEmail;

        public userViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.profile_image);
            textViewUsername = itemView.findViewById(R.id.name_text);
            textViewEmail = itemView.findViewById(R.id.email_text);

        }
    }

}
