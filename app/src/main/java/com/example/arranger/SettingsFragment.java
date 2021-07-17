package com.example.arranger;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment{

    private final String TAG ="SettingsFragment";

    private ImageView imageView;

    private EditText nameET;
    private EditText placeOfWorkET;
    private EditText departmentET;
    private EditText postET;
    private Button submitButton;
    private Bitmap bitmap;

    private boolean isEditing = false;
    private boolean isImageChanged = false;

//Каждый пользователь отображается отдельно

    public SettingsFragment() {
        // Required empty public constructor
    }


    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
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
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        initializeViews(view);
        setListenerForUserData();
        return view;
    }

    private void initializeViews(View view){
        imageView =  view.findViewById(R.id.message_image_view);
        nameET =  view.findViewById(R.id.settings_name);
        placeOfWorkET =  view.findViewById(R.id.settings_place_of_work);
        departmentET =  view.findViewById(R.id.settings_department);
        postET =  view.findViewById(R.id.settings_post);
        submitButton =  view.findViewById(R.id.settings_button);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Image view clicked!");
                getActivity().startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI),MainActivity.GET_FROM_GALLERY);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Submit button clicked!");
                if(!nameET.getText().toString().equals("")){
                    setNewData();
                    setUiEnabled(false);
                    Toast.makeText(getActivity(),"Изменения внесены",Toast.LENGTH_LONG);
                }
                else {
                    Toast.makeText(getActivity(),"Введите имя!",Toast.LENGTH_LONG);
                }
            }
        });
    }

    private void setNewData(){
        if(bitmap!=null){
            UserManager.setProfilePicture(bitmap);
        }
        UserManager.getInstance().getUsersReference().child(FirebaseAuth.getInstance().getUid()).child("name").setValue(nameET.getText().toString());
        UserManager.getInstance().getUsersReference().child(FirebaseAuth.getInstance().getUid()).child("placeOfWork").setValue(placeOfWorkET.getText().toString());
        UserManager.getInstance().getUsersReference().child(FirebaseAuth.getInstance().getUid()).child("department").setValue(departmentET.getText().toString());
        UserManager.getInstance().getUsersReference().child(FirebaseAuth.getInstance().getUid()).child("post").setValue(postET.getText().toString());
    }

    public void updateInformation(User user){
        if(user.name!=null){
            nameET.setText(user.name);
        }
        if(user.placeOfWork!=null){
            placeOfWorkET.setText(user.placeOfWork);
        }
        if(user.department!=null){
            departmentET.setText(user.department);
        }
        if(user.post!=null){
            postET.setText(user.post);
        }
        if(user.profilePicUrl!=null){
            Picasso.get().load(user.profilePicUrl).into(imageView);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.settings_menu,menu);
        Log.d(TAG,"SettingsMenu inflated");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.settings_edit){
            isEditing = !isEditing;
            setUiEnabled(isEditing);
        }
        if(item.getItemId() == R.id.menu_log_out){
            Log.d(TAG,"Log out pressed");
            FirebaseAuth.getInstance().signOut();
            UserManager.setInstanceNull();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void setImage(Bitmap image){
        bitmap=image;
        imageView.setImageBitmap(image);
        isImageChanged = true;
    }

    public void setImage(String url){
        isImageChanged = true;
    }

    private void setUiEnabled(boolean state){
        imageView.setEnabled(state);
        nameET.setEnabled(state);
        placeOfWorkET.setEnabled(state);
        departmentET.setEnabled(state);
        postET.setEnabled(state);
        if(state) {
            submitButton.setVisibility(View.VISIBLE);
        }
        else{
            submitButton.setVisibility(View.INVISIBLE);
        }
    }

    public void setListenerForUserData(){
        UserManager.getInstance().getUsersReference().child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                updateInformation(user);
                UserManager.getInstance().setCurrentUser(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}