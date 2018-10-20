package com.mengrudaddy.instagram.Search;

/*
SearchActivity.java
This class is activity to search users
 */

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mengrudaddy.instagram.Adapter.userListAdapter;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;
import com.mengrudaddy.instagram.utils.BottomNavigHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import info.debatty.java.stringsimilarity.NGram;


public class SearchActivity extends AppCompatActivity{
    private static final String TAG = "SearchActivity";
    private Context context=SearchActivity.this;
    private static final int ACTIVITY_NUM=1;

    NGram ng = new NGram(2);

    EditText editTextName;
    ImageButton buttonSearch;

    private TextView headtitle;

    RecyclerView mResultList;
    userListAdapter adapter;
    final FirebaseDatabase database =  FirebaseDatabase.getInstance();
    FirebaseUser currentUser;

    DatabaseReference databaseUsers;


    List<User> userList;
    List<User> allUsers;

    //current user
    User current = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Log.d("Search:", "Created Search Activity");

        hideSoftKeyboard();
        setUpBottomNavigView();


        userList = new ArrayList<>();


        editTextName = (EditText) findViewById(R.id.search_field);
        buttonSearch = (ImageButton) findViewById(R.id.search_btn);

        mResultList = (RecyclerView)findViewById(R.id.result_list);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));
        headtitle=(TextView)findViewById(R.id.heading_label);

        RecommandUsers();

        //adapter = new userListAdapter(this, R.layout.list_layout, userList);
        //mResultList.setAdapter(adapter);

        //mResultList = (RecyclerView) findViewById(R.id.result_list);

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                headtitle.setText("Search...");
                String username = editTextName.getText().toString().toLowerCase(Locale.getDefault());
                searchForMatch(username);
            }
        });

    }


    private void RecommandUsers(){
        Log.d(TAG,"start to show recommandations");
        final List<User>  allUsers = new ArrayList<>();

        DatabaseReference reference = database.getReference("users");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String uId = currentUser.getUid();

        Log.d(TAG, "start to recommend");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleDataSnapshot: dataSnapshot.getChildren()){
                    User user = singleDataSnapshot.getValue(User.class);
                    allUsers.add(user);
                    if (user.Id.compareTo(uId) == 0) {
                        current = user;
                    }
                }

                if(current.following!=null){

                    List<String> currentFollow = getListByMap(current.following, false);
                    /*
                        Suggested user algorithm:
                        Search for users that have at least two same followings and not followed by
                        the current user

                     */
                    for(int i=0; i< allUsers.size(); i++){
                        if (allUsers.get(i).following != null && current.following != null){
                            List<String> follow = getListByMap(allUsers.get(i).following, false);
                            follow.retainAll(currentFollow);
                            int num = follow.size();
                            if ((num>=2 && !allUsers.get(i).username.equals(current.username)) &&
                                    !allUsers.get(i).following.containsValue(current.Id)){
                                userList.add(allUsers.get(i));
                            }}
                    }

                    updateUsersList();

                    if (userList.size()==0){
                        Log.d(TAG,"recommend popular users for users who do not have common" +
                                "friends with other users");
                        userList.clear();
                        DatabaseReference reference = database.getReference("users");
                        reference.addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                HashMap<User, Integer> suggested = new HashMap<>();
                                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){

                                    User user = singleSnapshot.getValue(User.class);
                                    if(user.followers!=null){
                                        suggested.put(user, user.followers.keySet().size());
                                    }
                                    else{
                                        suggested.put(user, 0);
                                    }
                                }
                                HashMap<User, Integer> sortedMap = sortByValue(suggested);


                                userList = new ArrayList<>(sortedMap.keySet());
                                Collections.reverse(userList);

                                for(int i =0 ; i<userList.size(); i++) {
                                    if(userList.get(i).username.equals(current.username)){
                                        userList.remove(i);
                                    }
                                }

                                //update the users list view
                                updateUsersList();
                                headtitle.setText("Popular users");

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                }else{
                    if (userList.size()==0){
                        Log.d(TAG,"recommend popular users for new users");
                        userList.clear();
                        DatabaseReference reference = database.getReference("users");
                        reference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                HashMap<User, Integer> suggested = new HashMap<>();
                                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){

                                    User user = singleSnapshot.getValue(User.class);
                                    if(user.followers!=null){
                                        suggested.put(user, user.followers.keySet().size());
                                    }
                                    else{
                                        suggested.put(user, 0);
                                    }
                                }
                                HashMap<User, Integer> sortedMap = sortByValue(suggested);

                                userList = new ArrayList<>(sortedMap.keySet());
                                Collections.reverse(userList);

                                for(int i =0 ; i<userList.size(); i++) {
                                    if(userList.get(i).username.equals(current.username)){
                                        userList.remove(i);
                                    }
                                }

                                //update the users list view
                                updateUsersList();
                                headtitle.setText("Popular users");

                                Toast.makeText(getApplicationContext(), "Sorry, we cannot recommend friends" +
                                        " for you. You can make some friends from the popular list!", Toast.LENGTH_LONG).show();

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    private void searchForMatch(final String keyword){
        Log.d(TAG, "searchForMatch: searching for a match: " + keyword);

        userList.clear();
        final HashMap<User, Float> results = new HashMap<>();

        DatabaseReference reference = database.getReference("users");

        if(keyword.isEmpty()) {

            Log.d(TAG, "null input");
            Toast.makeText(getApplicationContext(), "The search bar should not be empty", Toast.LENGTH_LONG).show();

            //search key is not empty

        }else{

            Log.d(TAG,"Search for the text");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override

                public void onDataChange(DataSnapshot dataSnapshot) {

                    for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){

                        Log.d(TAG, "onDataChange: found user:" + singleSnapshot.getValue(User.class).toString());
                        User user = singleSnapshot.getValue(User.class);

                        float a = (float) ng.distance(user.username,keyword);

                        if(!user.username.equals(current.username)&&ng.distance(user.username,keyword)<0.5){
                            results.put(user, (float) ng.distance(user.username,keyword));
                        }
                    }

                    HashMap<User,Float> sortedMap = sortByValue2(results);
                    userList = new ArrayList<>(sortedMap.keySet());
                    updateUsersList();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public static List<User> getListByMap2(Map<User, Float> map) {
        List<User> list = new ArrayList<User>();
        List<Float> list1 = new ArrayList<Float>();

        Iterator<User> it = map.keySet().iterator();
        while (it.hasNext()) {
            User key = it.next();
            list.add(key);
        }

        return list;


    }

    public static List<String> getListByMap(Map<String, String> map,
                                            boolean isKey) {
        List<String> list = new ArrayList<String>();

        Iterator<String> it = map.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next().toString();
            if (isKey) {
                list.add(key);
            } else {
                list.add(map.get(key));
            }
        }

        return list;
    }

    private void updateUsersList(){
        Log.d(TAG, "updateUsersList: updating users list");

        adapter = new userListAdapter(SearchActivity.this, R.layout.list_layout, userList);

        mResultList.setAdapter(adapter);
        headtitle.setText("Search result");


    }


    private void hideSoftKeyboard(){
        if(getCurrentFocus() != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    /*
    Bottom Navigation Set up
     */

    private void setUpBottomNavigView(){
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        Log.d(TAG, "setUpBottomNavigView: "+bottomNavigationView);
        BottomNavigHelper.setUp(bottomNavigationView);
        BottomNavigHelper.NavigEnable(context,bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem mItem = menu.getItem(ACTIVITY_NUM);
        mItem.setChecked(true);
        mItem.setEnabled(false);

    }

    @Override
    protected void  onStart(){
        super.onStart();
        setUpBottomNavigView();

    }


    // function to sort hashmap by values
    public static HashMap<User, Integer> sortByValue(HashMap<User, Integer> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<User, Integer> > list =
                new LinkedList<Map.Entry<User, Integer> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<User, Integer> >() {
            public int compare(Map.Entry<User, Integer> o1,
                               Map.Entry<User, Integer> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<User, Integer> temp = new LinkedHashMap<User, Integer>();
        for (Map.Entry<User, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    // function to sort hashmap by values
    public static HashMap<User, Float> sortByValue2(HashMap<User, Float> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<User, Float> > list =
                new LinkedList<Map.Entry<User, Float> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<User, Float> >() {
            public int compare(Map.Entry<User, Float> o1,
                               Map.Entry<User, Float> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<User, Float> temp = new LinkedHashMap<User, Float>();
        for (Map.Entry<User, Float> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }



}

