package com.example.arranger.groupChats;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arranger.R;
import com.example.arranger.User;
import com.example.arranger.contacts.Contact;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.Calendar;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.ViewHolder>{
    private final static int MESSAGE_TO = 0;
    private final static int MESSAGE_FROM = 1;

    LayoutInflater inflater;
    RecyclerView chatRecyclerView;
    GroupChatActivity chatActivity;
    User user;
    GroupChat chat;

    Calendar calendar;


    GroupChatAdapter(Context context, RecyclerView chatRecyclerView, GroupChatActivity chatActivity, User user, GroupChat chat){
        this.inflater = LayoutInflater.from(context);
        this.chatRecyclerView = chatRecyclerView;
        this.chatActivity = chatActivity;
        this.user = user;
        this.chat = chat;
        calendar = Calendar.getInstance();
    }

    @NonNull
    @Override
    public GroupChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        //Log.d("ChatActivity","create View Holder");
        switch (viewType){
            case MESSAGE_TO:
                view = inflater.inflate(R.layout.chat_message_to, parent, false);
                return new GroupChatAdapter.ViewHolder(view);
            case MESSAGE_FROM:
                view = inflater.inflate(R.layout.chat_message_from,parent,false);
                return new GroupChatAdapter.ViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        //Log.d("ChatActivity","getting item view type");
        if(chat.getMessages().get(position).senderId.equals(FirebaseAuth.getInstance().getUid())){
            return MESSAGE_TO;
        }
        return MESSAGE_FROM;
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatAdapter.ViewHolder holder, int position) {
        Log.d("ChatActivity","getting item view type");
        switch(holder.getItemViewType()){
            case MESSAGE_TO:
                //Log.d("ChatActivity","messageTO");
                if(user.profilePicUrl!=null) {
                    Picasso.get().load(user.profilePicUrl).into(holder.icon);
                }
                holder.content.setText(chat.getMessages().get(position).content);
                holder.name.setText(user.name);
                break;
            case MESSAGE_FROM:
                String imageUrl = "";
                for (Contact contact:chat.getMembers()){
                    if(contact.getUid().equals(chat.getMessages().get(position).senderId)){
                        imageUrl = contact.getProfilePicUrl();
                        holder.name.setText(contact.getName());
                    }
                }
                if(!imageUrl.equals("")) {
                    Picasso.get().load(imageUrl).into(holder.icon);
                }
                holder.content.setText(chat.getMessages().get(position).content);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return chat.getMessages().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        final ImageView icon;
        final TextView content;
        final TextView name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.message_image_view);
            content = itemView.findViewById(R.id.message_content);
            name = itemView.findViewById(R.id.message_sender_name);
        }
    }
}
