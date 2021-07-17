package com.example.arranger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arranger.utils.DatabaseUploader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener{

    ImageView image;
    EditText emailET;
    EditText passwordET;
    EditText nameET;
    Button submitButton;
    Bitmap userImage;

    final String TAG = "RegistrationActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_activity);
        initializeViews();
    }

    private void initializeViews(){
        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        nameET = findViewById(R.id.nameET);
        submitButton = findViewById(R.id.buttonSubmitRegistration);
        image = findViewById(R.id.registration_image_view);

        submitButton.setOnClickListener(this);
        image.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonSubmitRegistration:
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();
                createUser(email,password, this);
                break;
            case R.id.registration_image_view:
                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI),MainActivity.GET_FROM_GALLERY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"Selecting image");
        if(requestCode == MainActivity.GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                userImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                if(userImage != null){
                    Log.d(TAG,"Image was selected");
                    image.setImageBitmap(userImage);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void createUser(final String email, String password, final Context context){
       FirebaseAuth instance = FirebaseAuth.getInstance();

       instance.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(context,"Registration successful",Toast.LENGTH_LONG).show();
                        Log.d(TAG, "User with email: " + email + " has been registered successfully");
                        onUserCreated();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"Registration failed",Toast.LENGTH_LONG).show();
                        Log.d(TAG, "registration failed, email: " + email);
                    }
                }).isSuccessful();
    }

    private void onUserCreated(){
        sendDataToDatabase();

        if(userImage != null){
            UserManager.setProfilePicture(userImage);
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void sendDataToDatabase(){
        String uid = FirebaseAuth.getInstance().getUid();
        String email = emailET.getText().toString();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("/users/"+uid);
        User user = new User(email,nameET.getText().toString());
        DatabaseUploader uploader = new DatabaseUploader(this);

        uploader.uploadObject(user,reference);
    }
}
