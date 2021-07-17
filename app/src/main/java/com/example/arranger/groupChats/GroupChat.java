package com.example.arranger.groupChats;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.arranger.User;
import com.example.arranger.UserManager;
import com.example.arranger.chats.Message;
import com.example.arranger.contacts.Contact;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GroupChat {


    private static final String TAG = "GroupChat";

    private String chatUid;
    private List<String>memberUIDs;
    private List<Contact>members;
    private String imageUrl;
    private String name;
    private Message lastMessage;

    private final DatabaseReference chatReference;

    private GroupChatsFragment fragment;
    private GroupChatActivity activity;

    private int messageCount = 0;
    private List<Message>messages;

    /*private ChatActivity myActivity;
    private MessagesFragment myFragment;
    private List<Message> messageList;
    int messageCount;
    private final String TAG = "Chat";

    private final DatabaseReference chatReference;*/

    public GroupChat(String uid,List<String> membersUids, String name, String imageUrl, Message lastMessage){


        this.chatUid = uid;
        this.memberUIDs = membersUids;
        this.imageUrl = imageUrl;
        this.name = name;
        this.lastMessage = lastMessage;
        /*messageList = new ArrayList<>();
        chatReference = UserManager.getInstance().getMessagesReference().child(chatUid);
        onChatCreated();
        Log.d(TAG,"Constructor");*/

        chatReference = UserManager.getInstance().getMessagesReference().child(chatUid);

        messages = new ArrayList<>();
        members = new ArrayList<>();

        setMembersListener();
        setChatNameListener();
        setChatPicListener();
        setMessagesListener();
    }



    public void setImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setLastMessage(Message lastMessage){
        if(UserManager.getInstance().getGroupChatList().size()>1) {
            UserManager.getInstance().getGroupChatList().remove(this);
            UserManager.getInstance().getGroupChatList().add(0, this);
        }
        this.lastMessage = lastMessage;
    }

    public void setFragment(GroupChatsFragment fragment){
        this.fragment = fragment;
    }

    public void setChatActivity(GroupChatActivity activity){
        this.activity = activity;
        if(messages == null||messages.size()==0){
            return;
        }
    }

    public Message getLastMessage(){
        return lastMessage;
    }

    public List<Message> getMessages(){
        return messages;
    }

    public String getChatUid() {
        return chatUid;
    }

    public List<Contact>getMembers(){return members;}

    private void setMembersListener(){
       DatabaseReference reference = chatReference.child("memberUIDs");
       chatReference.child("memberUIDs").addValueEventListener(new OnMembersDataChange());
       Log.d(TAG,reference.toString());
    }

    private void setChatNameListener(){
        chatReference.child("name").addValueEventListener(new OnChatNameChange());
    }

    private void setChatPicListener(){
        chatReference.child("pic").addValueEventListener(new OnChatPicChange());
    }

    private void setMessagesListener(){
        chatReference.child("messageCount").addValueEventListener(new OnMessageCountChange());
        chatReference.child("messages").addValueEventListener(new OnMessageReceived());
    }

    private void refreshActivity(){
        if(activity!=null){
            activity.refreshRecyclerView();
        }
    }

    private void refreshFragment(){
        if(fragment!=null){
            fragment.refreshRecyclerView();
        }
    }

    class OnMembersDataChange implements ValueEventListener{

        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                String uid = dataSnapshot.getValue(String.class);
                Log.d(TAG,"New member on "  + chatUid + ": " + uid);
                if(!memberUIDs.contains(uid)){
                    for(Contact contact:UserManager.getInstance().getContactList()){
                        if(contact.getUid().equals(uid)){
                            members.add(contact);
                            break;
                        }
                    }
                }
                else{
                    UserManager.
                            getInstance().
                            getUsersReference().
                            child(uid).
                            get().
                            addOnCompleteListener(new OnUnknownUserDownloaded(uid));
                }
            }
            refreshFragment();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }

    class OnUnknownUserDownloaded implements OnCompleteListener<DataSnapshot> {

        String userUID;

        OnUnknownUserDownloaded(String uid){
            userUID = uid;
        }

        @Override
        public void onComplete(@NonNull Task task) {
            if(task.isSuccessful()){
                try {
                    User user = (User)task.getResult();
                    members.add(new Contact(userUID,user.profilePicUrl,user.name));
                    refreshFragment();
                    refreshActivity();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    class OnChatNameChange implements ValueEventListener{

        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            name = snapshot.getValue().toString();
            refreshFragment();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }

    class OnChatPicChange implements ValueEventListener{

        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            imageUrl = snapshot.getValue().toString();
            refreshFragment();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }

    class OnMessageCountChange implements ValueEventListener{

        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.getValue()!=null) {
                messageCount = snapshot.getValue(Integer.class);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }

    class OnMessageReceived implements ValueEventListener{

        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                int messageNumber;
                if(dataSnapshot == null || dataSnapshot.getKey().equals("null")){
                    messageNumber = 0;
                }
                else{
                    messageNumber = Integer.parseInt(dataSnapshot.getKey());
                }

                if(messages.size()<messageNumber) {
                    Message message = dataSnapshot.getValue(Message.class);
                    messages.add(message);
                    GroupChat.this.setLastMessage(message);
                    if(message.timestamp != 0){
                        Date date = new Date(message.timestamp + 10800000);
                        Log.d(TAG,date.toString());
                        //Log.d(TAG, String.format("%s-%s-%s", date.getYear(),date.getMonth(), date.getDay()));
                    }
                }
            }
            refreshFragment();
            refreshActivity();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }
}
