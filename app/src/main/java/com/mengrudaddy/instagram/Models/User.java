package com.mengrudaddy.instagram.Models;


import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;
import java.util.Map;

// [START blog_user_class]
@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public Map<String, String> following; //the user is following
    public Map<String, String> followers; // follow the user
    public Map<String, String> posts; //posts list
    public Map<String, String> postLikes;


    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, Map<String, String> Following , Map<String, String> Followers,
                Map<String, String> Posts ) {
        this.username = username;
        this.email = email;
        this.following = Following;
        this.followers = Followers;
        this.posts = Posts;
    }

}
// [END blog_user_class]