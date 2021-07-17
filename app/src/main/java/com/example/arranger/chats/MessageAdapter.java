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

import com.example.arranger.R;
import com.example.arranger.UserManager;
import com.squareup.picasso.Picasso;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> implements View.OnClickListener{

    LayoutInflater inflater;
    RecyclerView messagesRecyclerView;
    MessagesFragment messagesFragment;

    public MessageAdapter(Context context, RecyclerView messagesRecyclerView, MessagesFragment messagesFragment){
        this.inflater = LayoutInflater.from(context);
        this.messagesRecyclerView = messagesRecyclerView;
        this.messagesFragment = messagesFragment;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.chat_list_item, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = UserManager.getInstance().getMessagesList().get(position);
        holder.chatName.setText(chat.getName());
        if(chat.getLastMessage()!=null) {
            holder.lastMessage.setText(chat.getLastMessage().content);
        }
        Picasso.get().load(chat.getImageUrl()).into(holder.chatIcon);
    }

    @Override
    public int getItemCount() {
        return UserManager.getInstance().getMessagesList().size();
    }

    @Override
    public void onClick(View view) {
        int itemPosition = messagesRecyclerView.getChildLayoutPosition(view);
        Chat chat = UserManager.getInstance().getMessagesList().get(itemPosition);
        messagesFragment.startChatActivity(chat.getChatUid());
        Log.d("Messages fragment","Clicked");
        //ChatActivity.startNewChat(contact);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        final ImageView chatIcon;
        final TextView chatName, lastMessage;
        ViewHolder(View view){
            super(view);
            chatIcon = view.findViewById(R.id.chat_image);
            chatName = view.findViewById(R.id.chat_name);
            lastMessage = view.findViewById(R.id.chat_last_message);
        }
    }

    public void removeAll(){
        while (UserManager.getInstance().getMessagesList().size()!=0){
            UserManager.getInstance().getMessagesList().remove(0);
        }
    }

}
