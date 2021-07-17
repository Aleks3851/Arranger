package com.example.arranger.chats;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.arranger.ChatActivity;
import com.example.arranger.UserManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class Chat {
    private String chatUid;
    private String userUid;
    private String imageUrl;
    private String name;
    private Message lastMessage;

    private ChatActivity myActivity;
    private MessagesFragment myFragment;
    private List<Message> messageList;
    int messageCount;
    private final String TAG = "Chat";

    private final DatabaseReference chatReference;

    private DESKeySpec keySpec;
    private SecretKeyFactory keyFactory;
    private SecretKey key;
    Cipher cipher;

    //Base64.encodeToString(cipher.doFinal(cleartext),Base64.DEFAULT) и Base64.decode(encryptedPwd, Base64.DEFAULT)

    Chat(String uid,String userUid, String name, String imageUrl, Message lastMessage){
        this.chatUid = uid;
        this.userUid = userUid;
        this.imageUrl = imageUrl;
        this.name = name;
        this.lastMessage = lastMessage;
        messageList = new ArrayList<>();
        chatReference = UserManager.getInstance().getMessagesReference().child(chatUid);
        onChatCreated();
        Log.d(TAG,"Constructor");
    }


    private void initializeCipher(){
        try {
            keySpec = new DESKeySpec(chatUid.getBytes(StandardCharsets.UTF_8));
            keyFactory = SecretKeyFactory.getInstance("DES");
            key = keyFactory.generateSecret(keySpec);
            cipher = Cipher.getInstance("DES");
        } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public String getEncodedString(String text) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE,key);
            byte[]bytes = text.getBytes("UTF-8");
            String encodedMessage = Base64.encodeToString(cipher.doFinal(bytes), Base64.DEFAULT);
            cipher.doFinal();
            Log.d(TAG, "Encoded message: " + encodedMessage);
            return encodedMessage;

        } catch (UnsupportedEncodingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            e.printStackTrace();
            return text;
        }
    }


    public String decodeString(String encodedString){
        byte[] encrypedPwdBytes = Base64.decode(encodedString,Base64.DEFAULT);
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] plainTextPwdBytes = (cipher.doFinal(encrypedPwdBytes));
            return new String(plainTextPwdBytes,"UTF-8");
        } catch (BadPaddingException | InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return encodedString;
        }
    }


        public void setImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setName(String name) {
        this.name = name;
        chatReference.child("name").setValue(name);
    }

    public void setChatActivity(ChatActivity activity){
        this.myActivity = activity;
    }

    public void setMessagesFragment(MessagesFragment messagesFragment){
        this.myFragment = messagesFragment;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setLastMessage(Message lastMessage){
        this.lastMessage = lastMessage;
        if(UserManager.getInstance().getMessagesList().size()>1) {
            UserManager.getInstance().getMessagesList().remove(this);
            UserManager.getInstance().getMessagesList().add(0, this);
        }
        notifyMessagesAdapter();
        notifyChatActivity();
    }

    public Message getLastMessage(){
        return lastMessage;
    }

    public String getChatUid() {
        return chatUid;
    }

    public String getUserUid(){return userUid;}

    public List<Message> getMessageList(){
        return messageList;
    }

    private void onChatCreated(){
        Log.d(TAG,chatReference.toString());
        initializeCipher();
        chatReference.child("messageCount").addValueEventListener(new OnMessageCountDataChange());
        chatReference.child("messages").addValueEventListener(new OnMessageReceived());
    }

    private void notifyMessagesAdapter(){
        if(myFragment!=null){
            myFragment.refreshMessagesList();
        }
    }

    private void notifyChatActivity(){
        if(myActivity!=null){
            myActivity.refreshRecyclerView();
        }
    }

    class OnMessageCountDataChange implements ValueEventListener {

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

                if(messageList.size()<messageNumber) {
                    Message message = dataSnapshot.getValue(Message.class);
                    message.content = decodeString(message.content);
                    messageList.add(message);
                    Chat.this.setLastMessage(message);
                    if(message.timestamp != 0){
                        Date date = new Date(message.timestamp + 10800000);
                        Log.d(TAG,date.toString());
                        //Log.d(TAG, String.format("%s-%s-%s", date.getYear(),date.getMonth(), date.getDay()));
                    }
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }
}
