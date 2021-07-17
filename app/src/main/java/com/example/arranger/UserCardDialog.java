package com.example.arranger;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import com.example.arranger.contacts.Contact;
import com.squareup.picasso.Picasso;

public class UserCardDialog extends Dialog{


    public UserCardDialog(@NonNull Context context, Contact contact) {
        super(context);
        setContentView(R.layout.user_card_dialog);
        fillViews(contact);
    }

    private void fillViews(Contact contact){

        ImageView imageView= findViewById(R.id.user_card_image);
        TextView nameTV = findViewById(R.id.user_card_name);
        TextView placeOfWorkTV = findViewById(R.id.user_card_powork);
        TextView departmentTV = findViewById(R.id.user_card_department);
        TextView postTV = findViewById(R.id.user_card_post);

        Picasso.get().load(contact.getProfilePicUrl()).into(imageView);
        nameTV.setText(contact.getName());

        if(contact.getPlaceOfWork()!=null){
            placeOfWorkTV.setText(contact.getPlaceOfWork());
        }
        else{
            findViewById(R.id.user_card_powork_layout).setVisibility(View.GONE);
        }

        if(contact.getDepartment()!=null){
            departmentTV.setText(contact.getDepartment());
        }
        else{
            findViewById(R.id.user_card_department_layout).setVisibility(View.GONE);
        }
        if(contact.getPost()!=null){
            postTV.setText(contact.getPost());
        }
        else{

            findViewById(R.id.user_card_post_layout).setVisibility(View.GONE);
        }
    }

}
