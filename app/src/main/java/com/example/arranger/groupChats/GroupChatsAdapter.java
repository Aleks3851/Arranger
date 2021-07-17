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
import com.example.arranger.UserManager;
import com.example.arranger.chats.Chat;
import com.example.arranger.chats.ChatAdapter;
import com.example.arranger.chats.MessageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

public class GroupChatsAdapter extends RecyclerView.Adapter<GroupChatsAdapter.ViewHolder> implements View.OnClickListener {


    LayoutInflater inflater;
    private RecyclerView groupChatsRV;
    private GroupChatsFragment groupChatsFragment;

    public GroupChatsAdapter(Context context,RecyclerView groupChatsRV,GroupChatsFragment groupChatsFragment){
        this.inflater = LayoutInflater.from(context);
        this.groupChatsRV = groupChatsRV;
        this.groupChatsFragment = groupChatsFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.chat_list_item, parent, false);
        view.setOnClickListener(this);
        return new GroupChatsAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupChat chat = UserManager.getInstance().getGroupChatList().get(position);
        holder.chatName.setText(chat.getName());
        if(chat.getLastMessage()!=null) {
            holder.lastMessage.setText(chat.getLastMessage().content);
        }
        if(chat.getImageUrl()!=null && !chat.getImageUrl().equals("")) {
            Picasso.get().load(chat.getImageUrl()).into(holder.chatIcon);
        }
    }

    @Override
    public int getItemCount() {
        return UserManager.getInstance().getGroupChatList().size();
    }

    @Override
    public void onClick(View view) {
        int itemPosition = groupChatsRV.getChildLayoutPosition(view);
        GroupChat chat = UserManager.getInstance().getGroupChatList().get(itemPosition);
        groupChatsFragment.startChatActivity(chat.getChatUid());
        Log.d("Messages fragment","Clicked");
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        final ImageView chatIcon;
        final TextView chatName, lastMessage;
        ViewHolder(View view){
            super(view);
            chatIcon = view.findViewById(R.id.chat_image);
            chatName = view.findViewById(R.id.chat_name);
            lastMessage = view.findViewById(R.id.chat_last_message);
        }
    }
}
