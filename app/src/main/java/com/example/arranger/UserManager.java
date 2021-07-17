package com.example.arranger;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.arranger.chats.Chat;
import com.example.arranger.contacts.Contact;
import com.example.arranger.groupChats.GroupChat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserManager {

    private static final String TAG = "UserManager";
    private final DatabaseReference messagesReference;
    private final DatabaseReference chatsReference;
    private final DatabaseReference usersReference;
    private final DatabaseReference contactsReference;
    private final DatabaseReference groupChatsReference;
    private final List<Contact> contactList;
    private final List<Chat> messagesList;
    private final List<GroupChat> groupChatList;
    private User currentUser;

    private static UserManager instance;

    private UserManager(){
        instance = this;
        messagesReference = FirebaseDatabase.getInstance().getReference("/messages/");
        chatsReference = FirebaseDatabase.getInstance().getReference("/chats/");
        usersReference = FirebaseDatabase.getInstance().getReference("/users/");
        contactsReference = FirebaseDatabase.getInstance().getReference("/contacts/");
        groupChatsReference = FirebaseDatabase.getInstance().getReference("/groupchats/");
        contactList = new ArrayList<>();
        messagesList = new ArrayList<>();
        groupChatList = new ArrayList<>();
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            usersReference.child(FirebaseAuth.getInstance().getUid()).child("status").setValue(1);
            setListenerForUserData();
        }
    }

    public static UserManager getInstance() {
        if(instance == null){
            instance = new UserManager();
        }
        return instance;
    }

    public static void setInstanceNull() {
        if(instance != null){
            instance = null;
        }
    }

    public void setListenerForUserData(){
        usersReference.child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                Log.d(TAG,currentUser.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void setProfilePicture(Bitmap bitmap){
        final String uid = UUID.randomUUID().toString();
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageReference = storage.getReference().child("/"+ uid);

        Log.d(TAG,"Starting image upload...");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        byte[] data = baos.toByteArray();

        storageReference.putBytes(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Log.d(TAG,"Completed");
                uploadProfilePicInformation(storageReference);
            }
        });
    }

    public DatabaseReference getMessagesReference(){
        return messagesReference;
    }

    public DatabaseReference getChatsReference(){
        return chatsReference;
    }

    public DatabaseReference getUsersReference(){
        return usersReference;
    }
    public DatabaseReference getContactsReference(){
        return contactsReference;
    }

    public DatabaseReference getGroupChatsReference(){return groupChatsReference;}

    public List<Contact> getContactList(){
        return contactList;
    }

    public List<Chat> getMessagesList(){return messagesList;}

    public List<GroupChat> getGroupChatList() {
        return groupChatList;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public User getCurrentUser(){
        return currentUser;
    }

    private static void uploadProfilePicInformation(StorageReference storageReference){
        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                FirebaseAuth instance = FirebaseAuth.getInstance();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                String userUid = instance.getCurrentUser().getUid();
                DatabaseReference databaseReference = database.getReference("/users/"+userUid+"/profilePicUrl");
                //removePreviousProfilePic(databaseReference);
                databaseReference.setValue(task.getResult().toString());

            }
        });
    }

    private static void removePreviousProfilePic(DatabaseReference reference){
        reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String url = snapshot.getValue(String.class);
                        /*TODO:
                         * Написать этот метод
                         * при загрузке нового изображения профиля
                         * старое должно удаляться
                         */
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private static void getCurrentUserReference(){}
}
