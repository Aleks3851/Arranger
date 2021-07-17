package com.example.arranger.groupChats.groupChatCreation;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arranger.LoadingDialog;
import com.example.arranger.MainActivity;
import com.example.arranger.R;
import com.example.arranger.User;
import com.example.arranger.UserManager;
import com.example.arranger.contacts.Contact;
import com.example.arranger.contacts.ContactAdapter;
import com.example.arranger.groupChats.GroupChat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CreateGroupChatActivity extends AppCompatActivity {

    ImageView chatImage;
    TextView chatMemberCountTV;
    EditText chatNameET;
    RecyclerView membersRV;
    String[]memberUIDs;

    NewGChatAdapter adapter;
    List<Contact>members;

    Bitmap chatImageBitmap;

    LoadingDialog dialog;

    private final String TAG = "CreateGroupChatActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_chat);

        Intent intent = getIntent();

        Bundle extras = intent.getExtras();

        if(extras!=null){
            memberUIDs = extras.getStringArray("memberUIDs");
            members = new ArrayList<>();
            getContactsFromUIDs();
        }

        initializeViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_gchat_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.check && checkFields()){
            Log.d(TAG,"Menu option check");
            String name = chatNameET.getText().toString();
            dialog = new LoadingDialog(this);
            dialog.show();
            setChatPicture(chatImageBitmap);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MainActivity.GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                chatImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                if(chatImageBitmap != null){
                    Log.d(TAG,"Image was selected");
                    chatImage.setImageBitmap(chatImageBitmap);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void setChatPicture(Bitmap bitmap){
        final String fileUID = UUID.randomUUID().toString();
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageReference = storage.getReference().child("/"+ fileUID);

        Log.d(TAG,"Starting image upload...");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        byte[] data = baos.toByteArray();

        storageReference.putBytes(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Log.d(TAG,"Completed");
                //uploadProfilePicInformation(storageReference);

                storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>(){
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        String chatUID = UUID.randomUUID().toString();

                        DatabaseReference reference = UserManager.getInstance().getMessagesReference().child(chatUID);

                        reference.child("memberUIDs").setValue(Arrays.asList(memberUIDs));
                        reference.child("name").setValue(chatNameET.getText().toString());
                        reference.child("pic").setValue(task.getResult().toString());
                        for (String memberUID:memberUIDs) {
                            bindUserWithChat(memberUID,chatUID);
                        }
                        dialog.dismiss();
                        finish();
                    }
                });

            }
        });
    }


    private void initializeViews(){
        chatImage = findViewById(R.id.create_gchat_image_view);
        chatImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), MainActivity.GET_FROM_GALLERY);
            }
        });
        chatMemberCountTV = findViewById(R.id.create_gchat_member_count);
        chatNameET = findViewById(R.id.create_gchat_name_edit_text);

        chatMemberCountTV.setText(String.format("Количество участников: %d", members.size()));

        initializeRecyclerView();
    }

    private void initializeRecyclerView(){
        membersRV = findViewById(R.id.create_gchat_recycler_view);
        membersRV.setHasFixedSize(true);
        membersRV.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        membersRV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });

        adapter = new NewGChatAdapter(getApplicationContext(),members,membersRV);
        membersRV.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private boolean checkFields(){
        Log.d(TAG,"Checking fields");
        if(chatImageBitmap == null){
            Toast.makeText(this,"Выберите картинку для чата",Toast.LENGTH_LONG).show();
            return false;
        }
        if(chatNameET.getText().toString().equals("")){
            Toast.makeText(this,"Выберите имя чата",Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void getContactsFromUIDs(){
        User user = UserManager.getInstance().getCurrentUser();
        members.add(new Contact(FirebaseAuth.getInstance().getUid(),user.profilePicUrl,user.name));
        for (String memberUID : memberUIDs) {
            Log.d("Group","Member: " + memberUID);
            for (Contact contact : UserManager.getInstance().getContactList()) {
                if (contact.getUid().equals(memberUID)) {
                    members.add(contact);
                }
            }
        }
    }

    private void bindUserWithChat(String userUID,String chatUID){
        DatabaseReference reference = UserManager.getInstance().getGroupChatsReference().child(userUID).child(chatUID);
        reference.setValue(true);
    }
}
