package com.example.arranger;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arranger.R;
import com.example.arranger.UserCardDialog;
import com.example.arranger.UserManager;
import com.example.arranger.chats.Chat;
import com.example.arranger.chats.ChatAdapter;
import com.example.arranger.chats.Message;
import com.example.arranger.contacts.Contact;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    private final int TEXT_MESSAGE = 0;
    private final String TAG = "ChatActivity";

    private Button sendMessageButton;
    private EditText messageEditText;

    private RecyclerView messagesRecyclerView;
    private ChatAdapter adapter;

    private TextView interlocutorNameTextView;
    private ImageView interlocutorImageView;
    private Chat chat;


    private DatabaseReference chatReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);
        initializeViews();
        Bundle arguments = getIntent().getExtras();
        if(arguments!=null) {
            String chatUID =  arguments.getString("chatUID");
            for(Chat chat:UserManager.getInstance().getMessagesList()){
                if(chat.getChatUid().equals(chatUID)){
                    this.chat = chat;
                    break;
                }
            }
            if(chat==null){
                finish();
            }
            initializeRecyclerView(chat);
            startNewChat(chat);
        }

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

    private void initializeRecyclerView(Chat chat){
        messagesRecyclerView = findViewById(R.id.chat_recycler_view);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        messagesRecyclerView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        adapter = new ChatAdapter(getApplicationContext(),messagesRecyclerView,this,UserManager.getInstance().getCurrentUser(),chat);
        messagesRecyclerView.setAdapter(adapter);
        refreshRecyclerView();
    }

    public void refreshRecyclerView(){
        if(chat!=null && chat.getMessageList()!=null) {
            adapter.notifyDataSetChanged();
            messagesRecyclerView.scrollToPosition(chat.getMessageList().size() - 1);
        }
    }

    public void startNewChat(Chat chat){
        this.chat = chat;
        if(chat!=null) {
            chat.setChatActivity(this);
            Picasso.get().load(chat.getImageUrl()).into(interlocutorImageView);
            //Log.d("MessagesActivity",chatListItem.getName());
            interlocutorNameTextView.setText(chat.getName());
            chatReference = UserManager.getInstance().getMessagesReference().child(chat.getChatUid());
        /*chatReference.child("messageCount").addValueEventListener(new OnMessageCountDataChange());
        chatReference.child("messages").addValueEventListener(new OnMessageReceived());*/
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.send_message_button){
            Log.d(TAG,"Send message button pressed");
            sendMessage();
        }
        if(view.getId()==R.id.interlocutor_name || view.getId()==R.id.interlocutor_image){
            for(Contact contact:UserManager.getInstance().getContactList()){
                if(contact.getUid().equals(chat.getUserUid())){
                    UserCardDialog dialog = new UserCardDialog(this,contact);
                    dialog.show();
                }
            }
        }
    }

    private void sendMessage(){
        String message = messageEditText.getText().toString();
        if(!message.equals("") && !message.trim().equals("")) {
            String encodedMessage = chat.getEncodedString(message);
            writeMessageToDataBase(new Message(FirebaseAuth.getInstance().getUid(), encodedMessage, TEXT_MESSAGE));
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
                refreshRecyclerView();
            }
        });
    }

    public ChatAdapter getAdapter(){
        return adapter;
    }

/*
    class OnMessageCountDataChange implements ValueEventListener{

        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.getValue()!=null) {
                messageCount = snapshot.getValue(Integer.class);
                Log.d(TAG, "message count changed: " + messageCount);
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
                    chat.setLastMessage(message.getContent());
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "message count: " + messageCount);
                    messagesRecyclerView.scrollToPosition(messages.size()-1);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }*/
}
