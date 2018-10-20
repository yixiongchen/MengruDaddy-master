package com.mengrudaddy.instagram.Models;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Comment {



    public String userId;
    public String content;
    public Date date;


    public Comment(){

    }

    public Comment(String userId,  String content, Date date){
        this.userId =userId;
        this.content = content;
        this.date =date;
    }


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("content", content);
        result.put("date", date);

        return result;
    }



}
