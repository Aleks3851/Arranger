package com.example.arranger;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.arranger.chats.MessagesFragment;
import com.example.arranger.contacts.Contact;
import com.example.arranger.contacts.ContactFragment;
import com.example.arranger.groupChats.GroupChatsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ServerValue;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int ADD_CONTACT = 0;
    public static final int GET_FROM_GALLERY = 1;
    public static final int START_NEW_GROUP_CHAT = 2;


    public static final String CONTACTS_FRAGMENT = "contacts_fragment";
    public static final String MESSAGES_FRAGMENT = "messages_fragment";
    public static final String GROUP_CHATS_FRAGMENT = "group_chats_fragment";
    public static final String SETTINGS_FRAGMENT = "settings_fragment";

    private final String TAG = "MainActivity";

    public String currentFragment;

    ImageButton contactsButton;
    ImageButton messagesButton;
    ImageButton settingsButton;
    ImageButton groupChatsButton;

    ImageButton currentButton;


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            startLoginActivity();
            return;
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(uid == null){
            startLoginActivity();
            return;
        }

        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){
                    startLoginActivity();
                }
            }
        });

        UserManager.getInstance();

        Intent stickyService = new Intent(this, CloseService.class);
        startService(stickyService);

        setContentView(R.layout.main_activity);
        addFragmentsToContainer();

        contactsButton = findViewById(R.id.contacts_button);
        groupChatsButton = findViewById(R.id.group_chats_button);
        messagesButton = findViewById(R.id.messages_button);
        settingsButton = findViewById(R.id.settings_button);

        contactsButton.setOnClickListener(this);
        groupChatsButton.setOnClickListener(this);
        messagesButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);

        currentButton = contactsButton;
        contactsButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight), android.graphics.PorterDuff.Mode.SRC_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){

        Log.d(TAG,"On activity result");

        if(data == null){
            Log.d(TAG,"Data is null");
            return;
        }
        if(requestCode == ADD_CONTACT){
            String email = data.getStringExtra("email");
            if(!email.isEmpty() && !email.equals(FirebaseAuth.getInstance().getUid())) {
                ((ContactFragment) (getSupportFragmentManager().findFragmentByTag(CONTACTS_FRAGMENT))).addUserByEmail(email);
            }
        }
        if(requestCode == GET_FROM_GALLERY){
            Uri selectedImage = data.getData();
            Bitmap userImage;
            Log.d(TAG,"Trying to get image");
            try {
                userImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                if(userImage != null){
                    Log.d(TAG,"Image was selected");
                    ((SettingsFragment)getSupportFragmentManager().findFragmentByTag(SETTINGS_FRAGMENT)).setImage(userImage);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(requestCode == START_NEW_GROUP_CHAT){
            ContactFragment contactFragment = (ContactFragment)getSupportFragmentManager().findFragmentByTag(CONTACTS_FRAGMENT);
            contactFragment.dischargeContactsPick();
            contactFragment.startingNewGroupChat = false;

            currentFragment = GROUP_CHATS_FRAGMENT;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getSupportFragmentManager().findFragmentByTag(MESSAGES_FRAGMENT)!=null) {
            ((MessagesFragment) getSupportFragmentManager().findFragmentByTag(MESSAGES_FRAGMENT)).refreshMessagesList();
        }
    }

    @Override
    protected void onDestroy() {
        if(UserManager.getInstance().getCurrentUser()!=null) {
            UserManager.getInstance().getUsersReference().child(FirebaseAuth.getInstance().getUid()).child("status").setValue(ServerValue.TIMESTAMP);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    private void startLoginActivity(){
        ContactFragment contactFragment = (ContactFragment)getSupportFragmentManager().findFragmentByTag(CONTACTS_FRAGMENT);
        if(contactFragment!=null){
            contactFragment.isRunning = false;
        }
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void addFragmentsToContainer(){
        Fragment contactsFragment = ContactFragment.newInstance();
        Fragment groupChatsFragment = GroupChatsFragment.newInstance();
        Fragment messagesFragment = MessagesFragment.newInstance();
        Fragment settingsFragment = SettingsFragment.newInstance();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_container,contactsFragment,CONTACTS_FRAGMENT);
        fragmentTransaction.add(R.id.fragment_container,groupChatsFragment,GROUP_CHATS_FRAGMENT);
        fragmentTransaction.add(R.id.fragment_container, messagesFragment,MESSAGES_FRAGMENT);
        fragmentTransaction.add(R.id.fragment_container,settingsFragment,SETTINGS_FRAGMENT);
        fragmentTransaction.show(contactsFragment);
        fragmentTransaction.hide(groupChatsFragment);
        fragmentTransaction.hide(messagesFragment);
        fragmentTransaction.hide(settingsFragment);
        fragmentTransaction.commit();

        setTitle("Контакты");
        currentFragment = CONTACTS_FRAGMENT;
    }

    @Override
    public void onClick(View v) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(fragmentManager.findFragmentByTag(currentFragment));



        if(v.getId() == R.id.contacts_button){
            fragmentTransaction.show(fragmentManager.findFragmentByTag(CONTACTS_FRAGMENT));
            ((ContactFragment)fragmentManager.findFragmentByTag(CONTACTS_FRAGMENT)).refreshContactsList();
            currentFragment = CONTACTS_FRAGMENT;
            setTitle("Контакты");
            Log.d(TAG,"Contacts button has been clicked!");
        }
        else if(v.getId()==R.id.messages_button){
            fragmentTransaction.show(fragmentManager.findFragmentByTag(MESSAGES_FRAGMENT));
            fragmentTransaction.hide(getSupportFragmentManager().findFragmentByTag(CONTACTS_FRAGMENT));
            currentFragment = MESSAGES_FRAGMENT;
            setTitle("Сообщения");
            Log.d(TAG,"Chats button has been clicked!");
        }
        else if(v.getId()==R.id.settings_button){
            fragmentTransaction.show(fragmentManager.findFragmentByTag(SETTINGS_FRAGMENT));
            fragmentTransaction.hide(getSupportFragmentManager().findFragmentByTag(CONTACTS_FRAGMENT));
            currentFragment = SETTINGS_FRAGMENT;
            setTitle("Настройки");
            Log.d(TAG,"Settings button has been clicked!");
        }
        else if(v.getId()==R.id.group_chats_button){
            fragmentTransaction.show(fragmentManager.findFragmentByTag(GROUP_CHATS_FRAGMENT));
            fragmentTransaction.hide(getSupportFragmentManager().findFragmentByTag(CONTACTS_FRAGMENT));
            currentFragment = GROUP_CHATS_FRAGMENT;
            setTitle("Групповые чаты");
            Log.d(TAG,"Group chats button has been clicked!");
        }
        fragmentTransaction.commit();
        onButtonPressed((ImageButton) v);
    }

    public void setTitle(String title){
        getSupportActionBar().setTitle(title);
    }

    private void onButtonPressed(ImageButton button){
        if(button==currentButton){
            return;
        }
        button.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight), android.graphics.PorterDuff.Mode.SRC_IN);
        currentButton.setColorFilter(null);
        currentButton = button;
    }
}
