package com.mengrudaddy.instagram.Login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mengrudaddy.instagram.Home.MainActivity;
import com.mengrudaddy.instagram.Models.User;
import com.mengrudaddy.instagram.R;

import java.util.HashMap;

public class SignupBActivity extends AppCompatActivity {

    private EditText inputUsername;
    private String username;
    private  android.support.design.widget.TextInputEditText inputPassword;
    private String inputEmail;
    private Button btnSignIn, btnSignUp;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    public static final String INTENT_KEY = "email_key";

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_b);


        // Get email address from intent
        inputEmail = getIntent().getStringExtra(INTENT_KEY);
        if (inputEmail == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }


        btnSignIn = (Button) findViewById(R.id.sign_in_button);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        inputUsername = (EditText) findViewById(R.id.username);
        inputPassword = (android.support.design.widget.TextInputEditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        //sign in
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupBActivity.this, LoginActivity.class));
            }
        });

        //sign up
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String password = inputPassword.getText().toString().trim();
                username = inputUsername.getText().toString().trim();
                signUp(inputEmail, password);

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }



    public void signUp(String email, String password){
        if (TextUtils.isEmpty(username)) {
            username = usernameFromEmail(email);
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        //create user
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignupBActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Toast.makeText(SignupBActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignupBActivity.this, "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            onAuthSuccess(username, task.getResult().getUser());
                        }
                    }
                });

    }


    public void onAuthSuccess(String username, FirebaseUser user){
        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail(),"");
        // Go to MainActivity
        startActivity(new Intent(SignupBActivity.this, MainActivity.class));
        finish();
    }

    public void writeNewUser(String userId, String name, String email,String description) {
        User user = new User(userId, name, email, description,new HashMap<String, String>(), new HashMap<String, String>(),
                new HashMap<String, String>());
        mDatabase.child("users").child(userId).setValue(user);
    }


    public String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }






}
