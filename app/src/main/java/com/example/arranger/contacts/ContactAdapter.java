package com.example.arranger.contacts;

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
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> implements View.OnClickListener {

    ContactFragment contactsFragment;
    List<Contact> contactList;
    LayoutInflater inflater;
    RecyclerView contactsView;
    Context context;

    public ContactAdapter(Context context,RecyclerView contactsView,ContactFragment contactsFragment){
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.contactList = UserManager.getInstance().getContactList();
        this.contactsView = contactsView;
        this.contactsFragment = contactsFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("ContactsFragment","create");
        View view = inflater.inflate(R.layout.contact_list_item, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("ContactsFragment","bind view holder");
        Contact contact = contactList.get(position);
        holder.contactName.setText(contact.getName());
        if(contact.isPickedForGroupChat()){
            holder.contactIcon.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_check_24));
        }
        else{
            Picasso.get().load(contact.getProfilePicUrl()).into(holder.contactIcon);
        }
        if(contact.getStatus()!=0){
            if(contact.getStatus()==1){
                holder.status.setText("В сети");
            }
            else{
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(contact.getStatus() + 10800000);
                String tmp = String.format("Был(а) в сети: %d.%d в %d:%d",
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.HOUR),
                        calendar.get(Calendar.MINUTE));
                holder.status.setText(tmp);
            }
        }
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    @Override
    public void onClick(View view) {
        int itemPosition = contactsView.getChildLayoutPosition(view);
        Contact contact = contactList.get(itemPosition);
        contactsFragment.onContactSelected(contact,itemPosition);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        final ImageView contactIcon;
        final TextView contactName, status;
        ViewHolder(View view){
            super(view);
            contactIcon = view.findViewById(R.id.contact_image);
            contactName = view.findViewById(R.id.contact_name);
            status = view.findViewById(R.id.contact_status);
        }
    }

    public void removeAll(){
        while (contactList.size()!=0){
            contactList.remove(0);
        }
    }
}
