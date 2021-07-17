package com.example.arranger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class AddContactActivity extends Activity implements View.OnClickListener {

    EditText emailET;
    Button btnOK;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact_activity);

        emailET = findViewById(R.id.emailET);
        btnOK = findViewById(R.id.btnOK);
        btnOK.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.putExtra("email", emailET.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }
}
