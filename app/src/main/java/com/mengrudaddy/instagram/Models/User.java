package com.mengrudaddy.instagram.Models;


import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

// [START blog_user_class]
@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public String image;
    public String description;
    public Map<String, String> following; //the user is following
    public Map<String, String> followers; // follow the user
    public Map<String, String> posts; //posts list
    public String Id;



    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String Id, String username, String email, String description,Map<String, String> Following ,
                Map<String, String> Followers,
                Map<String, String> Posts) {
        this.Id= Id;
        this.username = username;
        this.email = email;
        this.description = description;
        this.following = Following;
        this.followers = Followers;
        this.posts = Posts;
        //this.image = image;

    }

}
// [END blog_user_class]