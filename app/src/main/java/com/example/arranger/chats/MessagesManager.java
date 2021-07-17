package com.example.arranger.chats;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.arranger.User;
import com.example.arranger.UserManager;
import com.example.arranger.contacts.Contact;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessagesManager {
    MessagesFragment messagesFragment;
    List<String> chatUIDS;

    private final String TAG = "MessagesManager";

    public MessagesManager(MessagesFragment messagesFragment){
        this.messagesFragment = messagesFragment;
        chatUIDS = new ArrayList<>();
        addListenerForUserChats();
    }

    private void addListenerForUserChats(){
        UserManager.getInstance()
                .getChatsReference()
                .child(FirebaseAuth
                        .getInstance()
                        .getUid())
                .addValueEventListener(new OnMessagesDataChange());
    }

    public void startNewChat(Contact contact){
        Chat chat = new Chat(UUID.randomUUID().toString(),contact.getUid(),contact.getName(),contact.getProfilePicUrl(),null);
        chat.setMessagesFragment(messagesFragment);
        addChatToDatabase(chat);
    }

    private void addChatToDatabase(Chat chat){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String uid = FirebaseAuth.getInstance().getUid();
        DatabaseReference reference = database.getReference("/chats/" + uid +"/" + chat.getChatUid());
        reference.setValue(true);
        reference = database.getReference("/chats/" + chat.getUserUid() +"/" + chat.getChatUid());
        reference.setValue(true);
        reference = UserManager.getInstance().getMessagesReference().child(chat.getChatUid()).child("members").child("0");
        reference.setValue(uid);
        reference = reference.getParent().child("1");
        reference.setValue(chat.getUserUid());
        messagesFragment.startChatActivity(chat.getChatUid());
    }

    class OnMessagesDataChange implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot chatUIDSnapshot:snapshot.getChildren()) {
                String chatUID = chatUIDSnapshot.getKey();
                if (!chatUIDS.contains(chatUID)) {
                    chatUIDS.add(chatUID);
                    DatabaseReference databaseReference = UserManager.getInstance().getMessagesReference().child(chatUID).child("members");
                    databaseReference.get().addOnCompleteListener(new OnChatDataReceived(chatUID));
                    Log.d(TAG, "Received new chatUID from database! " + chatUID);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }

    class OnChatDataReceived implements OnCompleteListener<DataSnapshot>{
        String chatUID;
        OnChatDataReceived(String chatUID){
            this.chatUID = chatUID;
        }
        @Override
        public void onComplete(@NonNull Task<DataSnapshot> task) {
            for (DataSnapshot userSnapshot:task.getResult().getChildren()){
                if(!userSnapshot.getValue().equals(FirebaseAuth.getInstance().getUid())){
                    DatabaseReference databaseReference = UserManager.getInstance().getUsersReference().child(userSnapshot.getValue().toString());
                    databaseReference.get().addOnCompleteListener(new OnUserDataReceived(chatUID));
                }
            }
        }
    }

    class OnUserDataReceived implements OnCompleteListener<DataSnapshot>{
        String chatUID;
        OnUserDataReceived(String chatUID){
            this.chatUID = chatUID;
        }
        @Override
        public void onComplete(@NonNull Task<DataSnapshot> task) {
            if(task.isSuccessful()){
                User user = task.getResult().getValue(User.class);
                if(user!=null) {
                    for (String chatUid:chatUIDS){
                        if(chatUid.equals(task.getResult().getKey())){
                            return;
                        }
                    }
                    Log.d(TAG,user.toString());
                    chatUIDS.add(chatUID);
                    Chat chat = new
                            Chat(chatUID,task.getResult().getKey(),
                            user.name,user.profilePicUrl,null);
                    chat.setMessagesFragment(messagesFragment);
                    UserManager.getInstance().getMessagesList().add(chat);
                }
                messagesFragment.refreshMessagesList();
            }
        }
    }

    class OnMessageReceived implements OnCompleteListener<DataSnapshot>{
        Chat chat;
        OnMessageReceived(Chat chat){
            this.chat = chat;
        }
        @Override
        public void onComplete(@NonNull Task<DataSnapshot> task) {
            if(task.isSuccessful()){
                Message message = task.getResult().getValue(Message.class);
                chat.setLastMessage(message);
                messagesFragment.refreshMessagesList();
            }
        }
    }
}
