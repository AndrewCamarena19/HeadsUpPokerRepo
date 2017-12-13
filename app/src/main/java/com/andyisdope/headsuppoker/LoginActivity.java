package com.andyisdope.headsuppoker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText Email, Password;
    private FirebaseUser user;
    private FirebaseDatabase fdb;
    private String UID, Username;
    private EditText UserID;
    private DatabaseReference UserTable;
    private Double BankRoll;
    private Intent intent;
    private View LoginActivity, ShowProgressView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        Email = (EditText) findViewById(R.id.EmailText);
        intent = new Intent(getBaseContext(), RoomLobby.class);
        Password = (EditText) findViewById(R.id.PasswordText);
        Button signIn = (Button) findViewById(R.id.SignIn);
        fdb = FirebaseDatabase.getInstance();
        UserID = (EditText) findViewById(R.id.UserText);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Signin(Email.getText().toString(), Password.getText().toString());
            }
        });
        Button register = (Button) findViewById(R.id.Register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!UserID.getText().toString().equals("") || !Password.getText().toString().equals("") || !Email.getText().toString().equals(""))
                    CreateUser(Email.getText().toString(), Password.getText().toString());
                else
                    Toast.makeText(getBaseContext(), "Enter all fields to create an account", Toast.LENGTH_SHORT).show();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    intent.putExtra("UID", user.getUid());
                    Log.d("thing", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("thing", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

    }

    private void Signin(String email, String password) {
        LoginActivity = findViewById(R.id.activity_login);
        ShowProgressView = findViewById(R.id.login_progress);
        ShowProgress.showProgress(true, LoginActivity, ShowProgressView);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("things", "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("things", "signInWithEmail", task.getException());
                            Toast.makeText(getBaseContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        } else {
                            ShowProgress.showProgress(false, LoginActivity, ShowProgressView);
                            startActivity(intent);
                        }
                    }
                });
    }

    private void CreateUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("things", "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(getBaseContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        } else {
                            Map<String, String> map = new HashMap<>();
                            UserTable = fdb.getReference().child("Users").child(user.getUid());
                            map.put("BankRoll", 10000.0 + "");
                            map.put("Username", UserID.getText().toString());
                            UserTable.setValue(map);

                        }

                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
