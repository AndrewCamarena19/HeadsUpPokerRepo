package com.andyisdope.headsuppoker.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.andyisdope.headsuppoker.R;
import com.andyisdope.headsuppoker.Utilities.ShowProgress;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText Email, Password;
    private FirebaseUser user;
    private FirebaseDatabase fdb;
    private EditText UserID;
    private DatabaseReference UserTable;
    private Intent intent;
    private View LoginActivity, ShowProgressView;
    private Button signIn, register;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        InitUI();
        SetListeners();


    }

    private void SetListeners() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = firebaseAuth -> {
            user = firebaseAuth.getCurrentUser();
            if (user != null) {
                intent.putExtra("UID", user.getUid());
                Log.d("AuthState", "onAuthStateChanged:signed_in:" + user.getUid());
            } else {
                // User is signed out
                Log.d("AuthState", "onAuthStateChanged:signed_out");
            }

        };
    }

    private void InitUI() {
        fdb = FirebaseDatabase.getInstance();
        intent = new Intent(getBaseContext(), RoomLobby.class);
        Email = findViewById(R.id.EmailText);
        Password = findViewById(R.id.PasswordText);
        UserID = findViewById(R.id.UserText);
        signIn = findViewById(R.id.SignIn);
        signIn.setOnClickListener(view -> Signin(Email.getText().toString(), Password.getText().toString()));
        register = findViewById(R.id.Register);
        register.setOnClickListener(view -> {
            if (!UserID.getText().toString().equals("") || !Password.getText().toString().equals("") || !Email.getText().toString().equals(""))
                CreateUser(Email.getText().toString(), Password.getText().toString());
            else
                Toast.makeText(getBaseContext(), "Enter all fields to create an account", Toast.LENGTH_SHORT).show();
        });
    }

    private void Signin(String email, String password) {
        LoginActivity = findViewById(R.id.activity_login);
        ShowProgressView = findViewById(R.id.login_progress);
        ShowProgress.showProgress(true, LoginActivity, ShowProgressView);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
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
                });
    }

    private void CreateUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
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
