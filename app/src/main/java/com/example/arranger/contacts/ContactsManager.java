package com.example.arranger.contacts;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.arranger.User;
import com.example.arranger.UserManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContactsManager {
    ContactFragment contactsFragment;
    List<String> contactUIDS;

    private final String TAG = "Contact manager";

    public ContactsManager(ContactFragment contactsFragment){
        this.contactsFragment = contactsFragment;
        contactUIDS = new ArrayList<>();
        addListenerForUserContacts();
    }

    private void addListenerForUserContacts(){
        UserManager.getInstance()
                .getContactsReference()
                .child(FirebaseAuth
                        .getInstance()
                        .getUid())
                .addValueEventListener(new OnContactsDataChange());
    }

    public void addUserByEmail(final String email){
        DatabaseReference reference = UserManager.getInstance().getUsersReference();
        reference.addListenerForSingleValueEvent(new AddByEmailSingleEventListener(email));
    }

    private void addContact(final String contactUid){
        if(contactUid.isEmpty()){
            Log.d(TAG, "Uid not found");
            return;
        }
        Log.d(TAG, "Uid found");
        String userUid = FirebaseAuth.getInstance().getUid();
        bindContacts(userUid,contactUid);
        bindContacts(contactUid,userUid);
        Toast.makeText(contactsFragment.getActivity(),"Пользователь добавлен",Toast.LENGTH_LONG).show();
    }

    private void bindContacts(String uid1, String uid2){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("/contacts/"+ uid1 +"/"+ uid2);
        reference.setValue(true);
    }


    class AddByEmailSingleEventListener implements ValueEventListener {
        /*TODO:
        Переделать этот класс полностью
        после реализации поиска через семантическое дерево
         */

        String email;
        AddByEmailSingleEventListener(String email){
            this.email = email;
        }
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                User user = dataSnapshot.getValue(User.class);
                if(user!=null) {
                    if (user.email.equals(email)) {
                        addContact(String.valueOf(dataSnapshot.getKey()));
                        Log.d(TAG, dataSnapshot.getKey());
                        return;
                    }
                }
            }
            Toast.makeText(contactsFragment.getActivity(),"Пользователь не найден",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }


    class OnContactsDataChange implements ValueEventListener{
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot contactUIDSnapshot:snapshot.getChildren()) {
                String contactUID = contactUIDSnapshot.getKey();
                if (!contactUIDS.contains(contactUID)) {
                    DatabaseReference userReference = UserManager.getInstance().getUsersReference().child(contactUID);
                    contactUIDS.add(contactUID);
                    Contact contact = new Contact();
                    contact.setUid(contactUID);
                    UserManager.getInstance().getContactList().add(contact);

                    //userReference.get().addOnCompleteListener(new OnContactDataDownloaded(contactUID));
                    Log.d(TAG, "Received new contactUID from database! " + contactUID);
                    Log.d(TAG, "ContactsUID size " + contactUIDS.size());
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }
   /* class OnContactDataDownloaded implements OnCompleteListener<DataSnapshot>{
        String contactUID;
        OnContactDataDownloaded(String contactUID){
            this.contactUID = contactUID;
        }
        @Override
        public void onComplete(@NonNull Task<DataSnapshot> task) {
            if(task.isSuccessful()){
                Contact contact = task.getResult().getValue(Contact.class);
                Log.d(TAG,task.getResult().toString());
                if(contact!=null) {
                    Log.d(TAG, contact.toString());
                    contact.setUid(contactUID);
                    UserManager.getInstance().getContactList().add(contact);
                }
                contactsFragment.refreshContactsList();
            }
        }*/
}

