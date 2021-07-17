package com.example.arranger.groupChats;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.arranger.UserManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupChatsManager {
    private final String TAG = "GroupChatsManager";

    GroupChatsFragment groupChatsFragment;
    List<String> chatUIDs;

    public GroupChatsManager(GroupChatsFragment groupChatsFragment){
        this.groupChatsFragment = groupChatsFragment;
        chatUIDs = new ArrayList<>();

        setOnChatListener();
    }

    private void setOnChatListener(){
        UserManager.getInstance().
                getGroupChatsReference().
                child(FirebaseAuth.getInstance().
                        getCurrentUser().getUid()).
                addValueEventListener(new OnGroupChatDataChange());
    }

    class OnGroupChatDataChange implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot chatUIDSnapshot:snapshot.getChildren()) {
                String chatUID = chatUIDSnapshot.getKey();
                if (!chatUIDs.contains(chatUID)) {
                    chatUIDs.add(chatUID);
                    DatabaseReference databaseReference = UserManager.getInstance().getGroupChatsReference().child(chatUID).child("members");
                    databaseReference.get().addOnCompleteListener(new OnChatReceived(chatUID));

                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }

    class OnChatReceived implements OnCompleteListener<DataSnapshot>{

        String chatUID;
        OnChatReceived(String chatUID){
            this.chatUID = chatUID;
        }

        @Override
        public void onComplete(@NonNull Task<DataSnapshot> task) {
            if(task.isSuccessful()) {
                Log.d(TAG, "Received new group chat UID from database! " + chatUID);
                List<String> membersUIDs = new ArrayList<>();
                for (DataSnapshot snapshot:task.getResult().getChildren()){
                    membersUIDs.add(snapshot.getValue().toString());
                }

                GroupChat groupChat = new GroupChat(chatUID,membersUIDs,"","",null);
                groupChat.setFragment(groupChatsFragment);
                UserManager.getInstance().getGroupChatList().add(groupChat);
            }
        }
    }
}
