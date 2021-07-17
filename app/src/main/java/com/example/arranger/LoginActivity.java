package com.example.arranger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText emailET,passwordET;
    private Button submit;
    private TextView makeAccountLink;


    private final static String TAG = "LoginActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        initializeViews();
    }

    private void initializeViews(){
        emailET = findViewById(R.id.editTextTextEmailAddress);
        passwordET = findViewById(R.id.editTextTextPassword);
        submit = findViewById(R.id.buttonSubmit);
        makeAccountLink = findViewById(R.id.textViewMakeAccount);
        submit.setOnClickListener(this);
        makeAccountLink.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.equals(findViewById(R.id.buttonSubmit))){
            String email = emailET.getText().toString();
            String password = passwordET.getText().toString();
            signIn(email, password);
        }
        else if(view.equals(findViewById(R.id.textViewMakeAccount))){
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        }
    }

    private void signIn(String email,String password){
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,e.getMessage());
                        Toast.makeText(getApplicationContext(),"Failure",Toast.LENGTH_LONG).show();
                    }
                });
    }
}