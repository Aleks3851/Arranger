package com.example.arranger.chats;


public class Message {
    public String senderId;
    public String content;
    public int type;
    public long timestamp;

    Message(){};

    public Message(String senderId,String content,int type){
        this.senderId = senderId;
        this.content = content;
        this.type= type;
    }

    public String getContent() {
        return content;
    }
}
