package com.mengrudaddy.instagram.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Post {

    public String username;
    public String userId;
    public String description;
    public Map<String, String> location;
    public Date date;
    public Map<String, String>comments; //list of commentId
    public Map<String, String> likes; //list of likeId


    public Post(){

    }

    public Post(String username, String userId, String description, Map<String, String> location,
                Date date,  Map<String, String> comments,
                Map<String, String> likes){
        this.username = username;
        this.userId =userId;
        this.description = description;
        this.location = location;
        this.date = date;
        this.comments = comments;
        this.likes = likes;
    }

    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("userId", userId);
        result.put("description", description);
        result.put("location", location);
        result.put("date", date);
        result.put("likes", likes);
        result.put("comments", comments);

        return result;

    }

}
