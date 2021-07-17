package com.example.arranger.groupChats;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
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

import com.example.arranger.MainActivity;
import com.example.arranger.R;
import com.example.arranger.contacts.ContactFragment;
import com.example.arranger.groupChats.groupChatCreation.CreateGroupChatActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GroupChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupChatsFragment extends Fragment {

    RecyclerView groupChatsView;
    GroupChatsAdapter groupChatAdapter;
    GroupChatsManager groupChatsManager;
    List<String> newChatMembers;

    private final String TAG = "GroupChatsFragment";

    public GroupChatsFragment() {}

    public static GroupChatsFragment newInstance() {
        GroupChatsFragment fragment = new GroupChatsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_chats, container, false);
        groupChatsManager = new GroupChatsManager(this);
        initializeRecyclerView(view);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.group_chats_menu,menu);
        Log.d(TAG,"group chats menu inflated");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.create_new_group_chat){
            newChatMembers = new ArrayList<>();
            pickContacts();
        }
        return super.onOptionsItemSelected(item);
    }

    private void pickContacts(){
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        ContactFragment contactFragment = (ContactFragment) getParentFragmentManager().findFragmentByTag(MainActivity.CONTACTS_FRAGMENT);

        contactFragment.startingNewGroupChat = true;

        fragmentTransaction.hide(this);
        fragmentTransaction.show(contactFragment);
        fragmentTransaction.commit();
    }

    public void refreshRecyclerView(){
        groupChatAdapter.notifyDataSetChanged();
    }

    public void createNewChat(){
        Intent intent = new Intent(getActivity(), CreateGroupChatActivity.class);
        newChatMembers.add(0,FirebaseAuth.getInstance().getUid());

        String[]array = new String[newChatMembers.size()];
        newChatMembers.toArray(array);

        intent.putExtra("memberUIDs",array);
        getActivity().startActivityForResult(intent,MainActivity.START_NEW_GROUP_CHAT);
    }

    public void addContactForNewChat(String contactUID){
        newChatMembers.add(contactUID);
        Log.d(TAG,"Contact added size:" + newChatMembers.size());
    }

    public void removeContactForNewChat(String contactUID){
        newChatMembers.remove(contactUID);
        Log.d(TAG,"Contact removed size:" + newChatMembers.size());
    }

    private void initializeRecyclerView(View view){
        groupChatsView = view.findViewById(R.id.group_chats_rv);
        groupChatsView.setHasFixedSize(true);
        groupChatsView.setLayoutManager(new LinearLayoutManager(getContext()));

        groupChatsView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });

        groupChatAdapter = new GroupChatsAdapter(getContext(),groupChatsView,this);
        groupChatsView.setAdapter(groupChatAdapter);
        Log.d(TAG,"Recycler view has been initialized!");
    }

    public void startChatActivity(String chatUID){
        Intent intent = new Intent(getContext(), GroupChatActivity.class);
        intent.putExtra("chatUID", chatUID);
        startActivity(intent);
    }
}