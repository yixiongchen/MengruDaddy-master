package com.mengrudaddy.instagram.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mengrudaddy.instagram.Models.Comment;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class commentListAdapter extends BaseAdapter {

    private String[] commentIds,userIds;
    private final Context mContext;
    private User user;
    private final String TAG ="commentListAdapter::";
    private FirebaseDatabase database;
    private DatabaseReference commentRef;
    private Comment comment;
    private String postId;


    public commentListAdapter (Context context, String[] commentIds){

        this.commentIds = commentIds;
        this.mContext = context;
        userIds = new String[this.commentIds.length];
    }

    // 2
    @Override
    public int getCount() {
        return commentIds.length;
    }

    // 3
    @Override
    public long getItemId(int position) {
        return position;
    }

    // 4
    @Override
    public Object getItem(int position) {

        return commentIds[position];
    }


    // 5
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String id = this.commentIds[position];

        final int index = position;
        //2
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.comment_listview_layout, null);
            final ImageView imageView = (ImageView)convertView.findViewById(R.id.profile_image);
            final TextView username =(TextView)convertView.findViewById(R.id.username);
            final TextView content =(TextView)convertView.findViewById(R.id.content);
            final TextView date =(TextView)convertView.findViewById(R.id.date);
            final ProgressBar progressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);
            final commentListAdapter.ViewHolder viewHolder = new commentListAdapter.ViewHolder(imageView,username, content, date,progressBar);
            convertView.setTag(viewHolder);
        }
        final commentListAdapter.ViewHolder viewHolder = (commentListAdapter.ViewHolder)convertView.getTag();

        //load commentId object
        //real time database
        database = FirebaseDatabase.getInstance();
        commentRef = database.getReference("comments").child(id);

        //read user info
        ValueEventListener CommentListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                comment = dataSnapshot.getValue(Comment.class);
                viewHolder.username.setText(comment.username);
                viewHolder.content.setText(comment.content);
                DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
                String toDate = dateFormat.format(comment.date);
                viewHolder.date.setText(toDate);
                viewHolder.progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        commentRef.addListenerForSingleValueEvent(CommentListener);

        return convertView;
    }



    private class ViewHolder {

        private final ImageView imageViewPhoto;
        private final ProgressBar progressBar;
        private final TextView username, content, date;



        public ViewHolder(ImageView imageViewPhoto,TextView username, TextView content, TextView date,
                ProgressBar progressBar) {
            this.imageViewPhoto = imageViewPhoto;
            this.username = username;
            this.content = content;
            this.date =date;
            this.progressBar = progressBar;

        }
    }
}
