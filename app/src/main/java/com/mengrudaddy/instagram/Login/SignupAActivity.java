package com.mengrudaddy.instagram.Login;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mengrudaddy.instagram.R;

public class SignupAActivity extends AppCompatActivity {

    private EditText inputEmail;
    private Button btnToSignUpB, btnSignIn;
    private DatabaseReference mDatabase;
    private String email;
    private boolean emailFlag;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_a);

        inputEmail = (EditText)findViewById(R.id.email);
        btnToSignUpB = (Button)findViewById(R.id.sign_up_button);
        btnSignIn =(Button)findViewById(R.id.sign_in_button);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        emailFlag = true;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");


        //sign in
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupAActivity.this, LoginActivity.class));
            }
        });

        //send intent (email) to signUp B activity
        btnToSignUpB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                email = inputEmail.getText().toString().trim();
                checkEmail();

            }
        });
    }

    public void sendIntent(String email){
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, SignupBActivity.class);
        i.putExtra("email_key", email);
        startActivity(i);
    }


    public void checkEmail(){

        mDatabase.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                progressBar.setVisibility(View.GONE);
                if(snapshot.exists()){
                    Toast.makeText(getApplicationContext(), "Email is exist", Toast.LENGTH_SHORT).show();
                }
                else{
                    sendIntent(email);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

    }

}
