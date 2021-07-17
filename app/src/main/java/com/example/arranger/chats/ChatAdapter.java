package com.example.arranger.chats;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arranger.ChatActivity;
import com.example.arranger.R;
import com.example.arranger.User;
import com.example.arranger.UserManager;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.Calendar;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{

    private final static int MESSAGE_TO = 0;
    private final static int MESSAGE_FROM = 1;

    LayoutInflater inflater;
    RecyclerView chatRecyclerView;
    ChatActivity chatActivity;
    User user;
    Chat chat;

    Calendar calendar;


    public ChatAdapter(Context context, RecyclerView chatRecyclerView, ChatActivity chatActivity, User user, Chat chat){
        this.inflater = LayoutInflater.from(context);
        this.chatRecyclerView = chatRecyclerView;
        this.chatActivity = chatActivity;
        this.user = user;
        this.chat = chat;
        calendar = Calendar.getInstance();
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        //Log.d("ChatActivity","create View Holder");
        switch (viewType){
            case MESSAGE_TO:
                view = inflater.inflate(R.layout.chat_message_to, parent, false);
                return new ViewHolder(view);
            case MESSAGE_FROM:
                view = inflater.inflate(R.layout.chat_message_from,parent,false);
                return new ViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        //Log.d("ChatActivity","getting item view type");
        if(chat.getMessageList().get(position).senderId.equals(FirebaseAuth.getInstance().getUid())){
            return MESSAGE_TO;
        }
        return MESSAGE_FROM;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        switch(holder.getItemViewType()){
            case MESSAGE_TO:
                //Log.d("ChatActivity","messageTO");
                if(UserManager.getInstance().getCurrentUser()!=null) {
                    Picasso.get().load(UserManager.getInstance().getCurrentUser().profilePicUrl).into(holder.icon);
                    Log.d("ChatActivity","user is not null");
                }
                holder.content.setText(chat.getMessageList().get(position).content);
                /*if(chat.getMessageList().get(position).timestamp!=0) {
                    calendar.setTimeInMillis(chat.getMessageList().get(position).timestamp);
                    holder.date.setText(
                            String.format("%d.%d %d:%d",
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    calendar.get(Calendar.HOUR),
                                    calendar.get(Calendar.MINUTE)));
                }
                else{
                    holder.date.setText("");
                }*/
                break;
            case MESSAGE_FROM:
                Picasso.get().load(chat.getImageUrl()).into(holder.icon);
                holder.content.setText(chat.getMessageList().get(position).content);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return chat.getMessageList().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        final ImageView icon;
        final TextView content;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.message_image_view);
            content = itemView.findViewById(R.id.message_content);
        }
    }
}
