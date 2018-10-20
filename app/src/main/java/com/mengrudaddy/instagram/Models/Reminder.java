package com.mengrudaddy.instagram.Models;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/*  Activity Feed:
    Display activity of users that current user are following
*/

@IgnoreExtraProperties
public class Reminder {

    public String Id;
    public HashMap<String, String> action; //{userId, actionType, actionId}
    public Date date;

    public Reminder(){

    }

    public Reminder(String Id, HashMap<String, String> action, Date date){
        this.Id=Id;
        this.action= action;
        this.date = date;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("Id", Id);
        result.put("action", action);
        result.put("date", date);
        return result;
    }


}
