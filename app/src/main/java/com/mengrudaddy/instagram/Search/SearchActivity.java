package com.mengrudaddy.instagram.Search;

/*
SearchActivity.java
This class is activity to search users
 */

import android.content.Context;
import android.os.Bundle;
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
import java.util.List;
import java.util.Locale;


public class SearchActivity extends AppCompatActivity{
    private static final String TAG = "SearchActivity";
    private Context context=SearchActivity.this;
    private static final int ACTIVITY_NUM=1;


    EditText editTextName;
    ImageButton buttonSearch;

    RecyclerView mResultList;
    userListAdapter adapter;
    final FirebaseDatabase database =  FirebaseDatabase.getInstance();

    DatabaseReference databaseUsers;


    List<User> userList;




    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Log.d("Search:", "Created Search Activity");
        //setUpBottomNavigView();

        hideSoftKeyboard();
        //setUpBottomNavigView();

        userList = new ArrayList<>();

        mResultList = (RecyclerView)findViewById(R.id.result_list);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));

        //adapter = new userListAdapter(this, R.layout.list_layout, userList);
        //mResultList.setAdapter(adapter);

        editTextName = (EditText) findViewById(R.id.search_field);
        buttonSearch = (ImageButton) findViewById(R.id.search_btn);


        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextName.getText().toString().toLowerCase(Locale.getDefault());
                searchForMatch(username);
            }
        });

    }

    private void initTextListener(){
        Log.d(TAG, "initTextListener: initializing");

        userList = new ArrayList<>();

        editTextName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {




            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void RecommandUsers(){
        Log.d(TAG,"start to show recommandations");
        userList.clear();

        DatabaseReference reference = database.getReference("users");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String uId = currentUser.getUid();

        Log.d(TAG, "start to search");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleDataSnapshot: dataSnapshot.getChildren()){
//                    singleDataSnapshot;

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void searchForMatch(String keyword){
        Log.d(TAG, "searchForMatch: searching for a match: " + keyword);
        userList.clear();
        //update the users list view
        if(keyword.length() ==0){

            Log.d(TAG,"null input");

        }else{
            DatabaseReference reference = database.getReference("users");
//            Query query = reference
//                    .orderByChild("username").equalTo(keyword);
            Log.d(TAG,"Search for the text");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: found user:" + singleSnapshot.getValue(User.class).toString());
                        String name = singleSnapshot.getValue(User.class).username;

                        if (name.equals(keyword)){
                            userList.add(singleSnapshot.getValue(User.class));
                        }
                        //update the users list view
                        updateUsersList();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void updateUsersList(){
        Log.d(TAG, "updateUsersList: updating users list");

        adapter = new userListAdapter(SearchActivity.this, R.layout.list_layout, userList);

        mResultList.setAdapter(adapter);

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



}

