package com.example.arranger;

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

import com.example.arranger.contacts.Contact;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;

public class GroupCardAdapter extends RecyclerView.Adapter<GroupCardAdapter.ViewHolder> implements View.OnClickListener {

    List<Contact>members;
    LayoutInflater inflater;

    GroupCardAdapter(Context context, List<Contact> members){
        this.members = members;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("ContactsFragment","create");
        View view = inflater.inflate(R.layout.contact_list_item, parent, false);
        view.setOnClickListener(this);
        return new GroupCardAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("ContactsFragment","bind view holder");
        Contact contact = members.get(position);
        holder.contactName.setText(contact.getName());
        Picasso.get().load(contact.getProfilePicUrl()).into(holder.contactIcon);
        if(contact.isPickedForGroupChat()){
            holder.contactName.setBackgroundColor(Color.GREEN);
        }
        else{
            holder.contactName.setBackgroundColor(Color.TRANSPARENT);
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
        return members.size();
    }

    @Override
    public void onClick(View v) {

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView contactIcon;
            final TextView contactName, status;
            ViewHolder(View view){
                super(view);
                contactIcon = view.findViewById(R.id.contact_image);
                contactName = view.findViewById(R.id.contact_name);
                status = view.findViewById(R.id.contact_status);
            }
    }
}
