package com.example.arranger.utils;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

public class DatabaseUploader implements OnCompleteListener<Void> {

    Activity activity;

    private final String TAG = "FirebaseDatabase";
    private final int UPLOADING_OBJECT = 0;

    int uploadType;

    public DatabaseUploader(Activity activity){
        this.activity = activity;
    }

    public void uploadObject(Object object, DatabaseReference reference){
        Log.d(TAG,"Starting upload...");
        uploadType = UPLOADING_OBJECT;
        reference.setValue(object).addOnCompleteListener(activity,this);
    }

    public void uploadObject(Object object, DatabaseReference reference, OnCompleteListener onCompleteListener){
        Log.d(TAG,"Starting upload...");
        uploadType = UPLOADING_OBJECT;
        reference.setValue(object).addOnCompleteListener(activity,onCompleteListener);
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if(task.isSuccessful()){
            Log.d(TAG,"Uploaded successfully");
        }
        else{
            Log.d(TAG,"Upload failed");
            Log.e(TAG, task.getException().getMessage());
        }
    }
}
