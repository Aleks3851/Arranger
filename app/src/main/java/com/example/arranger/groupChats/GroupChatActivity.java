package com.example.arranger.groupChats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.arranger.GroupCardDialog;
import com.example.arranger.R;
import com.example.arranger.UserManager;
import com.example.arranger.chats.Chat;
import com.example.arranger.chats.ChatAdapter;
import com.example.arranger.chats.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.squareup.picasso.Picasso;

public class GroupChatActivity extends AppCompatActivity implements View.OnClickListener {

    private final int TEXT_MESSAGE = 0;
    private final String TAG = "ChatActivity";

    private Button sendMessageButton;
    private EditText messageEditText;

    private RecyclerView messagesRecyclerView;
    private GroupChatAdapter adapter;

    private TextView interlocutorNameTextView;
    private ImageView interlocutorImageView;
    private GroupChat groupChat;


    private DatabaseReference chatReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        initializeViews();
        Bundle arguments = getIntent().getExtras();
        if(arguments!=null) {
            String chatUID =  arguments.getString("chatUID");
            for(GroupChat chat:UserManager.getInstance().getGroupChatList()){
                if(chat.getChatUid().equals(chatUID)){
                    this.groupChat = chat;
                    break;
                }
            }
            initializeRecyclerView(groupChat);
            startNewChat(groupChat);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        groupChat.setChatActivity(null);
    }

    private void initializeViews(){
        messageEditText = findViewById(R.id.message_edit_text);
        sendMessageButton = findViewById(R.id.send_message_button);
        sendMessageButton.setOnClickListener(this);
        interlocutorNameTextView = findViewById(R.id.interlocutor_name);
        interlocutorNameTextView.setOnClickListener(this);
        interlocutorImageView = findViewById(R.id.interlocutor_image);
        interlocutorImageView.setOnClickListener(this);
    }

    private void initializeRecyclerView(GroupChat chat){
        messagesRecyclerView = findViewById(R.id.chat_recycler_view);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        messagesRecyclerView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        adapter = new GroupChatAdapter(getApplicationContext(),messagesRecyclerView,this, UserManager.getInstance().getCurrentUser(),chat);
        messagesRecyclerView.setAdapter(adapter);
        refreshRecyclerView();
    }

    public void refreshRecyclerView(){
        adapter.notifyDataSetChanged();
        messagesRecyclerView.scrollToPosition(groupChat.getMessages().size()-1);
    }

    public void startNewChat(GroupChat chat){
        this.groupChat = chat;
        groupChat.setChatActivity(this);
        Picasso.get().load(chat.getImageUrl()).into(interlocutorImageView);
        //Log.d("MessagesActivity",chatListItem.getName());
        interlocutorNameTextView.setText(chat.getName());
        chatReference = UserManager.getInstance().getMessagesReference().child(chat.getChatUid());
        /*chatReference.child("messageCount").addValueEventListener(new OnMessageCountDataChange());
        chatReference.child("messages").addValueEventListener(new OnMessageReceived());*/
    }

    private void startGroupInfoDialog(){
        GroupCardDialog dialog = new GroupCardDialog(this,groupChat);
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.send_message_button){
            Log.d(TAG,"Send message button pressed");
            sendMessage();
        }
        if(view.getId() == R.id.interlocutor_image){
            startGroupInfoDialog();
        }
        if(view.getId() == R.id.interlocutor_name){
            startGroupInfoDialog();
        }
    }

    private void sendMessage(){
        String message = messageEditText.getText().toString();
        if(!message.equals("")&&!message.trim().equals("")) {
            writeMessageToDataBase(new Message(FirebaseAuth.getInstance().getUid(), message, TEXT_MESSAGE));
            messageEditText.setText("");
        }
    }

    private void writeMessageToDataBase(final Message message){
        final DatabaseReference messageCountRef = chatReference.child("messageCount");


        messageCountRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                int data;
                if (currentData.getValue() == null) {
                    data = 1;
                }
                else{
                    data = currentData.getValue(Integer.class) + 1;
                }
                currentData.setValue(data);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                DatabaseReference databaseReference = chatReference.child("messages").child(String.valueOf(currentData.getValue()));
                databaseReference.setValue(message);
                databaseReference.child("timestamp").setValue(ServerValue.TIMESTAMP);
            }
        });
    }
}