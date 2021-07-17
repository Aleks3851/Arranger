package com.example.arranger.contacts;

import androidx.annotation.NonNull;

import com.example.arranger.MainActivity;
import com.example.arranger.UserManager;
import com.example.arranger.chats.Chat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;


@IgnoreExtraProperties
public class Contact{
    private String uid;
    private String name;
    private String profilePicUrl;
    private String placeOfWork;
    private String department;
    private String post;
    private long status;

    public Contact(){}

    public Contact(String name, String profilePicUrl, long status){
        this.name = name;
        this.profilePicUrl = profilePicUrl;
        this.status = status;
    }

    public Contact(String uid,String profilePicUrl, String name){
        setUid(uid);
        this.profilePicUrl = profilePicUrl;
        this.name = name;
    }

    @Exclude
    private boolean pickedForGroupChat = false;
    MainActivity mainActivity;

    public void setUid(String uid) {
        this.uid = uid;
        UserManager.getInstance().getUsersReference().child(uid).addValueEventListener(new OnUserDataChange());
    }

    public String getUid(){return uid;}

    public long getStatus() {
        return status;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public String getName() {
        return name;
    }

    public void setPickedForGroupChat(boolean pickedForGroupChat) {
        this.pickedForGroupChat = pickedForGroupChat;
    }

    public boolean isPickedForGroupChat() {
        return pickedForGroupChat;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "uid='" + uid + '\'' +
                ", profilePicUrl='" + profilePicUrl + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", pickedForGroupChat=" + pickedForGroupChat +
                '}';
    }

    public String getPlaceOfWork() {
        return placeOfWork;
    }

    public String getDepartment() {
        return department;
    }

    public String getPost() {
        return post;
    }

    class OnUserDataChange implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            Contact contact = snapshot.getValue(Contact.class);
            name = contact.getName();
            profilePicUrl = contact.getProfilePicUrl();
            for(Chat chat:UserManager.getInstance().getMessagesList()){
                if(chat.getUserUid().equals(uid)){
                    chat.setName(name);
                    chat.setImage(profilePicUrl);
                }
            }
            placeOfWork = contact.getPlaceOfWork();
            department = contact.getDepartment();
            post = contact.getPost();
            status = contact.getStatus();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }

}
