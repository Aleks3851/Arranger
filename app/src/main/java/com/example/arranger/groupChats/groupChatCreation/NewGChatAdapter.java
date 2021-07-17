package com.example.arranger.groupChats.groupChatCreation;

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
import com.example.arranger.contacts.Contact;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NewGChatAdapter extends RecyclerView.Adapter<NewGChatAdapter.ViewHolder>{


    List<Contact> contactList;
    LayoutInflater inflater;
    RecyclerView contactsView;

    public NewGChatAdapter(Context context,List<Contact>contactList, RecyclerView contactsView){
        this.inflater = LayoutInflater.from(context);
        this.contactList = contactList;
        this.contactsView = contactsView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("ContactsFragment","create");
        View view = inflater.inflate(R.layout.contact_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("ContactsFragment","bind view holder");
        Contact contact = contactList.get(position);
        holder.contactName.setText(contact.getName());
        Picasso.get().load(contact.getProfilePicUrl()).into(holder.contactIcon);
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{
        final ImageView contactIcon;
        final TextView contactName, lastMessage;
        ViewHolder(View view){
            super(view);
            contactIcon = view.findViewById(R.id.contact_image);
            contactName = view.findViewById(R.id.contact_name);
            lastMessage = view.findViewById(R.id.contact_status);
        }
    }

    public void removeAll(){
        while (contactList.size()!=0){
            contactList.remove(0);
        }
    }
}
