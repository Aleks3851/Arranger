package com.example.arranger;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arranger.contacts.Contact;
import com.example.arranger.groupChats.GroupChat;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GroupCardDialog  extends Dialog {

    RecyclerView membersRV;
    List<Contact>members;


    public GroupCardDialog(@NonNull Context context, GroupChat chat) {
        super(context);
        setContentView(R.layout.group_card_dialog);
        initializeViews(chat);
    }
    private void initializeViews(GroupChat chat){
        ImageView imageView= findViewById(R.id.group_card_image);
        TextView membersCountTV = findViewById(R.id.group_card_count);
        membersRV = findViewById(R.id.group_card_rv);
        membersRV.setHasFixedSize(true);
        membersRV.setLayoutManager(new LinearLayoutManager(getContext()));

        Picasso.get().load(chat.getImageUrl()).into(imageView);
        membersCountTV.setText("Количество участников: " + (chat.getMembers().size()+1));


        this.members = new ArrayList<>();
        members.add(new Contact(UserManager.getInstance().getCurrentUser().name,UserManager.getInstance().getCurrentUser().profilePicUrl,1));
        members.addAll(chat.getMembers());
        GroupCardAdapter adapter = new GroupCardAdapter(getContext(),members);
        membersRV.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
