package com.mengrudaddy.instagram.Models;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/*

   Display users following that liked photos or started following user

 */
@IgnoreExtraProperties
public class Event {

    public String Id; //event Id
    public Date date;

    public HashMap<String, String> action; //{userId, actionType, actionId}

    public Event(){

    }

    public Event(String Id,  HashMap<String, String> action, Date date ){
        this.Id = Id;
        this.action = action;
        this.date = date;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("Id", Id);
        result.put("date", date);
        result.put("action", action);

        return result;
    }

}
