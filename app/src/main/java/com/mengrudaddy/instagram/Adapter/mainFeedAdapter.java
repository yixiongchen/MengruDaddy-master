package com.mengrudaddy.instagram.Adapter;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mengrudaddy.instagram.Comments.CommentsListActivity;
import com.mengrudaddy.instagram.Likes.LikesListActivity;
import com.mengrudaddy.instagram.Models.Comment;
import com.mengrudaddy.instagram.Models.Like;
import com.mengrudaddy.instagram.Models.Post;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.Profile.ProfileActivity;
import com.mengrudaddy.instagram.Profile.SinglePostActivity;
import com.mengrudaddy.instagram.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class mainFeedAdapter extends RecyclerView.Adapter<mainFeedAdapter.postViewHolder> {

    private Context mContext;
    private ArrayList<String> postList;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private User user;
    private boolean[] likeBoolean;




    public mainFeedAdapter(Context mContext, ArrayList<String> postList, User user) {
        this.mContext = mContext;
        this.postList = postList;
        this.user = user;
        storage = FirebaseStorage.getInstance();
        database =FirebaseDatabase.getInstance();
        likeBoolean = new boolean[this.postList.size()];
        for(int i=0; i<likeBoolean.length; i++){
            likeBoolean[i]=false;
        }

    }


    @NonNull
    @Override
    public postViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.main_feed_layout, null);
        return new mainFeedAdapter.postViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final mainFeedAdapter.postViewHolder postViewHolder, final int i) {

        final String postId = postList.get(i);

        //read post from firebase database
        DatabaseReference postRef = database.getReference("posts").child(postId);

        //read post info
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)  {
                Post post = dataSnapshot.getValue(Post.class);
                if(post != null){
                    renderPost(postViewHolder, post, i);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        postRef.addValueEventListener(postListener);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }


    public class postViewHolder extends RecyclerView.ViewHolder {

        ImageView profile_image, postImage, like, comment;
        TextView username, description,likes, comments, date, location;
        ProgressBar progressBar;

        public postViewHolder(@NonNull View itemView) {
            super(itemView);
            profile_image = itemView.findViewById(R.id.profile_image);
            postImage = itemView.findViewById(R.id.postImage);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            username = itemView.findViewById(R.id.username);
            description = itemView.findViewById(R.id.photo_description);
            likes = itemView.findViewById(R.id.likes);
            comments = itemView.findViewById(R.id.comments);
            date = itemView.findViewById(R.id.date);
            location = itemView.findViewById(R.id.location);
            progressBar= itemView.findViewById(R.id.progressBar);

        }
    }


    /*
      load profile image
   */
    public void accessProfileImage(final mainFeedAdapter.postViewHolder viewHolder, Post post){
        StorageReference profile_pic_ref = storage.getReference("profile_pic/"+post.userId);

        profile_pic_ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //picasso lib load remote image
                Picasso.with(mContext).load(uri.toString()).into(viewHolder.profile_image,
                        new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {

                            }
                            @Override
                            public void onError() {

                                //viewHolder.progressBar.setVisibility(View.GONE);
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //viewHolder.progressBar.setVisibility(View.GONE);
                //viewHolder.progressBar.setVisibility(View.GONE);
                //Log.d(TAG, "Can not download file, please check connection");
            }
        });
    }


    /*
      load post image
    */
    public void accessPostImage(final mainFeedAdapter.postViewHolder viewHolder, Post post){
        StorageReference postImage = storage.getReference("posts/images/"+post.Id);


        postImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //picasso lib load remote image
                Picasso.with(mContext).load(uri.toString()).into(viewHolder.postImage,
                        new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                viewHolder.progressBar.setVisibility(View.GONE);
                                //do smth when picture is loaded successfully
                                //viewHolder.progressBar.setVisibility(View.GONE);
                            }
                            @Override
                            public void onError() {


                                //viewHolder.progressBar.setVisibility(View.GONE);
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //viewHolder.progressBar.setVisibility(View.GONE);
                //viewHolder.progressBar.setVisibility(View.GONE);
                //Log.d(TAG, "Can not download file, please check connection");
            }
        });
    }




    //////////////////////////////////Click events handler/////////////////////////////////////

    /*
        handle click event for like
     */
    private void handleLike(final mainFeedAdapter.postViewHolder viewHolder, final Post post, final int i){
        //click like button
        viewHolder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //like object reference
                DatabaseReference likeRef  = database.getReference("likes/");
                DatabaseReference likeListRef = database.getReference("posts/"+post.Id+"/"+"likes");
                //already like -> unlike event
                if(likeBoolean[i]){
                    HashMap<String, String> map =( HashMap<String, String>)post.likes;
                    String likeId = map.get(user.Id);
                    //remove <userid, likeId> from likesList
                    likeListRef.child(user.Id).removeValue();
                    //remove Like object from database
                    likeRef.child(likeId).removeValue();
                    //set icon
                    likeBoolean[i] = false;
                    viewHolder.like.setImageDrawable(mContext.getDrawable(R.drawable.ic_action_activity));
                    Toast.makeText(mContext, "You unliked the Post!", Toast.LENGTH_SHORT).show();

                }
                //like event
                else{
                    //create like object
                    Like likeObject  = new Like(post.Id, user.Id, user.username, new Date());
                    //write like object to database
                    String likeId = likeRef.push().getKey();
                    likeRef.child(likeId).setValue(likeObject);

                    //add <UserId, LikeId> to the list in the post
                    Map<String, Object> updateValue = new HashMap<>();
                    updateValue.put(user.Id,likeId); //userId:likeId
                    likeListRef.updateChildren(updateValue);

                    //set icon
                    likeBoolean[i] = true;
                    viewHolder.like.setImageDrawable(mContext.getDrawable(R.drawable.ic_action_activity));
                    Toast.makeText(mContext, "You Liked the Post!", Toast.LENGTH_SHORT).show();

                }
            }
        });


    }


    /*
        handle click event for comment
     */
    private void handleComment(final mainFeedAdapter.postViewHolder viewHolder, final Post post){
        //click comment button
        viewHolder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentsListActivity.class);
                intent.putExtra("postId",post.Id);
                mContext.startActivity(intent);
            }
        });

    }


    /*
        handle click event for view likes
     */
    private void viewLikes(final mainFeedAdapter.postViewHolder viewHolder, final Post post){
        //view all likes
        viewHolder.likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //postId to LikeListActivity
                Intent intent = new Intent(mContext, LikesListActivity.class);
                intent.putExtra("postId",post.Id);
                mContext.startActivity(intent);

            }
        });


    }


    /*
        handle click event for view comments
     */
    private void viewComments(final mainFeedAdapter.postViewHolder viewHolder, final Post post){
        viewHolder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentsListActivity.class);
                intent.putExtra("postId",post.Id);
                mContext.startActivity(intent);

            }
        });

    }

    /*
        handle click for user profile
     */
    private void viewUserProfile(final mainFeedAdapter.postViewHolder viewHolder, final Post post){
        viewHolder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra("id",post.userId);
                mContext.startActivity(intent);

            }
        });

        viewHolder.profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra("id",post.userId);
                mContext.startActivity(intent);
            }
        });
    }



    /*
        Render the post
     */
    private void renderPost(final mainFeedAdapter.postViewHolder postViewHolder, final Post post, final int i){


        // initialize like image
        if(post.likes!=null && post.likes.containsKey(user.Id)){
            likeBoolean[i] = true;
            postViewHolder.like.setImageDrawable(mContext.getDrawable(R.drawable.ic_action_like));
        }

        //load profile image
        accessProfileImage(postViewHolder, post);
        //load post image
        accessPostImage(postViewHolder, post);


        //username
        postViewHolder.username.setText(post.username);

        //description
        if(post.description != null){
            postViewHolder.description.setText(post.description);
        }
        else{
            postViewHolder.description.setVisibility(View.GONE);
        }

        //likes
        if(post.likes != null){
            postViewHolder.likes.setText(Integer.toString(post.likes.keySet().size())+" likes");
        }
        else{
            postViewHolder.likes.setVisibility(View.GONE);
        }

        //comments
        if(post.comments != null){
            postViewHolder.comments.setVisibility(View.VISIBLE);
            postViewHolder.comments.setText("View all " +Integer.toString(post.comments.keySet().size()) +" comments");
        }
        else{
            postViewHolder.comments.setVisibility(View.GONE);
        }

        //date
        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd hh:mm");
        String toDate = dateFormat.format(post.date);
        postViewHolder.date.setText(toDate);

        //location
        if(post.location != null){

            String address = getAddress(post.location.get("latitude"),post.location.get("longitude"));
            postViewHolder.location.setText(address);
        }
        else{
            postViewHolder.location.setVisibility(View.GONE);
        }

        //click for like
        handleLike(postViewHolder, post, i);
        //click for comment
        handleComment(postViewHolder, post);
        //click for view comment
        viewComments(postViewHolder, post);
        //click for view likes
        viewLikes(postViewHolder, post);

        //click for user profile
        viewUserProfile(postViewHolder, post);


    }


    /*
        Return the address by coordinates
     */
    private String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                result.append(address.getLocality()).append("\n");
                result.append(address.getCountryName());
                //result.append(address.get)
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }




}
