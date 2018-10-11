package com.mengrudaddy.instagram.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Post {

    public String username;
    public String userId;
    public String description;
    public String latitude;
    public String longitude;
    public Date date;
    public ArrayList<String> comments;
    public ArrayList<String> likes;


    public Post(){

    }

    public Post(String username, String userId, String description, String latitude,
                String longitude, Date date,  ArrayList<String> comments,
                ArrayList<String> likes){
        this.username = username;
        this.userId =userId;
        this.description = description;
        this.latitude = latitude;
        this.longitude=longitude;
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
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("date", date);
        result.put("likes", likes);
        result.put("comments", comments);

        return result;

    }

}
