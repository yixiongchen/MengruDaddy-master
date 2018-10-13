package com.mengrudaddy.instagram.Models;


import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

// [START blog_user_class]
@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public List<String> following; //the user is following
    public List<String> followers; // follow the user
    public List<String> posts; //posts list


    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, List<String> Following , List<String> Followers,
                List<String> Posts ) {
        this.username = username;
        this.email = email;
        this.following = Following;
        this.followers = Followers;
        this.posts = Posts;
    }

}
// [END blog_user_class]