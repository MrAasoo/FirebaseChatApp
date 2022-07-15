package com.aasoo.firebasechatapp.login_signup;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aasoo.firebasechatapp.MainActivity;
import com.aasoo.firebasechatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

public class RegisterActivity extends AppCompatActivity {

    private EditText regEmail, regPassword;
    private TextView alreadyHaveAccount;
    private Button createAccount;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initialize();

        //init Firebase auth and database ref
        mAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference();


        // already have account
        alreadyHaveAccount.setOnClickListener(view -> sendUserToLoginActivity());

        // Create account
        createAccount.setOnClickListener(view -> createAccountWithEmailPassword());

    }

    private void createAccountWithEmailPassword() {
        String userEmail = regEmail.getText().toString().trim();
        String userPassword = regPassword.getText().toString();

        //Check email and password for empty
        if (TextUtils.isEmpty(userEmail) && TextUtils.isEmpty(userPassword)) {
            regEmail.setError("Please enter email id");
            regPassword.setError("Please enter password");
            regEmail.requestFocus();
        } else if (TextUtils.isEmpty(userEmail)) {
            regEmail.setError("Please enter email id");
            regEmail.requestFocus();
        } else if (TextUtils.isEmpty(userPassword)) {
            regPassword.setError("Please enter password");
            regPassword.requestFocus();
        } else if (!(TextUtils.isEmpty(userEmail)) && !(TextUtils.isEmpty(userPassword))) {
            // show progress dialog
            progressDialog.setTitle("Create new account");
            progressDialog.setMessage("Please wait, while we are creating new account...");
            progressDialog.setCancelable(false);
            progressDialog.create();
            progressDialog.show();

            // create account using email and password
            mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // device token
                            final String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            // current user id
                            String userId = mAuth.getCurrentUser().getUid();

                            // save user id and device token
                            mReference.child("Users").child(userId).setValue(" ");
                            mReference.child("Users").child(userId).child("device_token").setValue(deviceToken);
                            sendUserToMainActivity();
                            Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Account not created", Toast.LENGTH_SHORT).show();
                        }

                        //dismiss progress dialog
                        progressDialog.cancel();
                        progressDialog.dismiss();
                    });

        }

    }

    private void sendUserToLoginActivity() {
        onBackPressed();
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initialize() {
        regEmail = findViewById(R.id.reg_email);
        regPassword = findViewById(R.id.reg_password);
        alreadyHaveAccount = findViewById(R.id.already_account);
        createAccount = findViewById(R.id.reg_button);
        progressDialog = new ProgressDialog(this);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}