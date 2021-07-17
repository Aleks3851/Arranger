package com.example.arranger.chats;

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

import com.example.arranger.ChatActivity;
import com.example.arranger.LoginActivity;
import com.example.arranger.MainActivity;
import com.example.arranger.R;
import com.example.arranger.UserManager;
import com.example.arranger.contacts.Contact;
import com.example.arranger.contacts.ContactFragment;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MessagesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessagesFragment extends Fragment{


    RecyclerView messagesView;
    MessageAdapter messageAdapter;
    MessagesManager messagesManager;


    private final String TAG = "MessagesFragment";

    public MessagesFragment() {}


    public static MessagesFragment newInstance() {
        MessagesFragment messagesFragment = new MessagesFragment();
        Bundle arguments = new Bundle();
        //arguments.putSerializable(PARAM_MAIN_ACTIVITY,mainActivity);
        messagesFragment.setArguments(arguments);
        return messagesFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        initializeRecyclerView(view);
        messagesManager = new MessagesManager(this);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.user_menu,menu);
        Log.d(TAG,"UserMenu inflated");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.menu_write_message){
            Log.d(TAG,"Write message pressed");
            ((ContactFragment)getParentFragmentManager().findFragmentByTag(MainActivity.CONTACTS_FRAGMENT)).startingNewChat = true;
            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction.hide(this);
            fragmentTransaction.show(getParentFragmentManager().findFragmentByTag(MainActivity.CONTACTS_FRAGMENT));
            fragmentTransaction.commit();
            ((MainActivity)getActivity()).setTitle("Выберите контакт");
        }
        return super.onOptionsItemSelected(item);
    }


    private void initializeRecyclerView(View view){
        messagesView = view.findViewById(R.id.chatsRV);
        messagesView.setHasFixedSize(true);
        messagesView.setLayoutManager(new LinearLayoutManager(getContext()));

        messagesView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });

        messageAdapter = new MessageAdapter(getContext(),messagesView,this);
        messagesView.setAdapter(messageAdapter);
        Log.d(TAG,"Recycler view has been initialized!");
    }

    public void refreshMessagesList(){
        messageAdapter.notifyDataSetChanged();
        Log.d(TAG,"Recycler view has been refreshed!");
        Log.d(TAG,"Size of chats list: " + UserManager.getInstance().getMessagesList().size());
    }

    public void startNewChat(Contact contact){
        for(Chat chat:UserManager.getInstance().getMessagesList()){
            if(chat.getUserUid().equals(contact.getUid())){
                startChatActivity(chat.getChatUid());
                return;
            }
        }
        messagesManager.startNewChat(contact);
    }

    public void startChatActivity(String chatUID){
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("chatUID", chatUID);
        startActivity(intent);
    }

}