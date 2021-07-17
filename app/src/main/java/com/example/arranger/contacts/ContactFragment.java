package com.example.arranger.contacts;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.arranger.AddContactActivity;
import com.example.arranger.MainActivity;
import com.example.arranger.R;
import com.example.arranger.UserCardDialog;
import com.example.arranger.UserManager;
import com.example.arranger.chats.MessagesFragment;
import com.example.arranger.groupChats.GroupChatsFragment;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContactFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactFragment extends Fragment {

    RecyclerView contactsView;
    ContactAdapter adapter;
    ContactsManager contactsManager;

    private static final String TAG = "Contacts fragment";
    public boolean startingNewChat = false;
    public boolean startingNewGroupChat = false;


    MenuItem check;
    public boolean isRunning;

    public ContactFragment() {
        // Required empty public constructor
    }

    public static ContactFragment newInstance() {
        Log.d(TAG,"New instance of contacts fragment has been created");
        return new ContactFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contacts, container, false);
        initializeRecyclerView(v);

        contactsManager = new ContactsManager(this);

        Log.d(TAG,"Contacts view created!!!");
        return v;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.new_message_menu,menu);
        Log.d(TAG,"AddContactMenu inflated");
        check = menu.findItem(R.id.check);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.add_new_contact){
            addNewContact();
        }
        if(item.getItemId()==R.id.check){
            GroupChatsFragment fragment = (GroupChatsFragment) getParentFragmentManager().findFragmentByTag(MainActivity.GROUP_CHATS_FRAGMENT);
            fragment.createNewChat();
            dischargeContactsPick();

            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction.hide(getParentFragmentManager().findFragmentByTag(MainActivity.CONTACTS_FRAGMENT));
            fragmentTransaction.show(getParentFragmentManager().findFragmentByTag(MainActivity.GROUP_CHATS_FRAGMENT));
            fragmentTransaction.commit();

        }
        return super.onOptionsItemSelected(item);
    }

    public void setCheckVisibility(boolean state){
        check.setVisible(state);
    }

    private void initializeRecyclerView(View v){
        contactsView = v.findViewById(R.id.contactsRV);
        contactsView.setHasFixedSize(true);
        contactsView.setLayoutManager(new LinearLayoutManager(getContext()));

        contactsView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });

        adapter = new ContactAdapter(getContext(),contactsView,this);
        contactsView.setAdapter(adapter);

        isRunning = true;

        Thread refresher = new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    while (isRunning){
                        Thread.sleep(10000);
                        if(getActivity()!=null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refreshContactsList();
                                }
                            });
                        }
                    }
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        refresher.start();

        Log.d(TAG,"Recycler view has been initialized!");
    }

    public void dischargeContactsPick(){
        setCheckVisibility(false);
        for(Contact contact: UserManager.getInstance().getContactList()){
            contact.setPickedForGroupChat(false);
        }
        refreshContactsList();
    }

    public void refreshContactsList(){
        adapter.notifyDataSetChanged();
    }

    private void addNewContact(){
        Intent intent = new Intent(getActivity(), AddContactActivity.class);
        getActivity().startActivityForResult(intent,MainActivity.ADD_CONTACT);
    }

    public void addUserByEmail(final String email){
        contactsManager.addUserByEmail(email);
    }


    public void onContactSelected(Contact contact, int itemPosition){
        Intent data = new Intent();
        data.putExtra("uid",contact.getUid());
        data.putExtra("name",contact.getName());
        data.putExtra("imageUrl",contact.getProfilePicUrl());
        /*setResult(RESULT_OK,data);
        finish();*/
        if(startingNewChat){
            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction.hide(this);
            fragmentTransaction.show(getParentFragmentManager().findFragmentByTag(MainActivity.MESSAGES_FRAGMENT));
            fragmentTransaction.commit();
            ((MessagesFragment)getParentFragmentManager().findFragmentByTag(MainActivity.MESSAGES_FRAGMENT)).startNewChat(contact);
            startingNewChat = false;
            ((MainActivity)getActivity()).setTitle("Сообщения");
        }
        else if(startingNewGroupChat){
            setCheckVisibility(true);
            if(contact.isPickedForGroupChat()){
                ((GroupChatsFragment)getParentFragmentManager().
                        findFragmentByTag(MainActivity.GROUP_CHATS_FRAGMENT)).
                        removeContactForNewChat(contact.getUid());
                contact.setPickedForGroupChat(false);
            }
            else {
                contact.setPickedForGroupChat(true);
                ((GroupChatsFragment)getParentFragmentManager().
                        findFragmentByTag(MainActivity.GROUP_CHATS_FRAGMENT)).
                        addContactForNewChat(contact.getUid());
            }
            adapter.notifyItemChanged(itemPosition);
        }
        else{
            UserCardDialog dialog = new UserCardDialog(getActivity(),contact);
            dialog.show();
        }
    }
}