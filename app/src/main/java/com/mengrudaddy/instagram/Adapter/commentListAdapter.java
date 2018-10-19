package com.mengrudaddy.instagram.Adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mengrudaddy.instagram.Models.Comment;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class commentListAdapter extends BaseAdapter {

    private String[] commentIds,userIds;
    private final Context mContext;
    private User user;
    private final String TAG ="commentListAdapter::";
    private FirebaseDatabase database;
    private DatabaseReference commentRef, userRef;
    private Comment comment;
    private String postId;
    private FirebaseStorage storage;


    public commentListAdapter (Context context, String[] commentIds){

        this.commentIds = commentIds;
        this.mContext = context;
        userIds = new String[this.commentIds.length];
        storage = FirebaseStorage.getInstance();
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
            final ImageView imageViewphoto = (ImageView)convertView.findViewById(R.id.profile_image);
            final TextView username =(TextView)convertView.findViewById(R.id.username);
            final TextView content =(TextView)convertView.findViewById(R.id.content);
            final TextView date =(TextView)convertView.findViewById(R.id.date);
            final ProgressBar progressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);
            final commentListAdapter.ViewHolder viewHolder = new commentListAdapter.ViewHolder(imageViewphoto,username, content, date,progressBar);
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
                viewHolder.content.setText(comment.content);
                DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
                String toDate = dateFormat.format(comment.date);
                viewHolder.date.setText(toDate);

                //load user info
                accessUsername(viewHolder);
                //load profile image
                accessProfileImage(viewHolder);


            }
            //viewHolder.progressBar.setVisibility(View.GONE);

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        commentRef.addValueEventListener(CommentListener);

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

    /*
        load profile image
     */
    public void accessProfileImage(final ViewHolder viewHolder){
        StorageReference profile_pic_ref = storage.getReference("profile_pic/"+comment.userId);

        profile_pic_ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //picasso lib load remote image
                Picasso.with(mContext).load(uri.toString()).into(viewHolder.imageViewPhoto,
                        new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                //do smth when picture is loaded successfully
                                //viewHolder.progressBar.setVisibility(View.GONE);
                            }
                            @Override
                            public void onError() {
                                //do smth when there is picture loading error
                                //viewHolder.progressBar.setVisibility(View.GONE);
                            }
                        });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //viewHolder.progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Can not download file, please check connection");
            }
        });
    }


     /*
        read username by userid
     */

     public void accessUsername(final ViewHolder viewHolder){
         userRef = database.getReference("users").child(comment.userId);
         //read user info
         ValueEventListener userListener = new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot)  {
                 user = dataSnapshot.getValue(User.class);
                 viewHolder.username.setText(user.username);

             }
             @Override
             public void onCancelled(DatabaseError databaseError) {}
         };
         userRef.addListenerForSingleValueEvent(userListener);


     }
}
