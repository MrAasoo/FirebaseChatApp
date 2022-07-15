package com.aasoo.firebasechatapp.login_signup;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aasoo.firebasechatapp.MainActivity;
import com.aasoo.firebasechatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LogInActivity extends AppCompatActivity {

    private TextView newAccount, forgotPassword;
    private Button phoneLogin, btnLoging;
    private EditText loginEmail, loginPassword;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        initialize();
        mAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference().child("Users");


        newAccount.setOnClickListener(view -> sendUserToRegisterAccount());
        phoneLogin.setOnClickListener(view -> sendUserToPhoneLogIn());
        forgotPassword.setOnClickListener(view -> sendLinkToMail());
        btnLoging.setOnClickListener(view -> userLogin());


    }

    private void sendLinkToMail() {
        if (loginEmail.getText().toString().matches("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$") && loginEmail.getText().toString().length() > 8) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Forgot password ?")
                    .setMessage("Press Yes to receive the reset link.")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mAuth.sendPasswordResetEmail(loginEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LogInActivity.this, "Password reset email link has been sent to your email.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(LogInActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                    }
                                    progressDialog.cancel();
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create()
                    .show();
        } else {
            loginEmail.setError("Please enter valid email.");
            loginEmail.requestFocus();
        }

    }

    private void userLogin() {
        String userEmail = loginEmail.getText().toString().trim();
        String userPassword = loginPassword.getText().toString();

        //Check email and password for empty
        if (TextUtils.isEmpty(userEmail) && TextUtils.isEmpty(userPassword)) {
            loginEmail.setError("Please enter email id");
            loginPassword.setError("Please enter password");
            loginEmail.requestFocus();
        } else if (TextUtils.isEmpty(userEmail)) {
            loginEmail.setError("Please enter email id");
            loginEmail.requestFocus();
        } else if (TextUtils.isEmpty(userPassword)) {
            loginPassword.setError("Please enter password");
            loginPassword.requestFocus();
        } else if (!(TextUtils.isEmpty(userEmail)) && !(TextUtils.isEmpty(userPassword))) {
            // show progress dialog
            progressDialog.setTitle("Logging in");
            progressDialog.setMessage("Please wait, while we are logging into your account...");
            progressDialog.setCancelable(false);
            progressDialog.create();
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // device token
                        final String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        // current user id
                        String userId = mAuth.getCurrentUser().getUid();

                        // save device token
                        mReference.child(userId).child("device_token").setValue(deviceToken);
                        sendUserToMainActivity();
                        Toast.makeText(LogInActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LogInActivity.this, "Login failed!", Toast.LENGTH_SHORT).show();
                        Log.e(LogInActivity.class.getSimpleName(), "onComplete : error - ", task.getException());
                    }

                    //dismiss progress dialog
                    progressDialog.cancel();
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void initialize() {
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        newAccount = findViewById(R.id.new_account);
        forgotPassword = findViewById(R.id.forgot_password);
        btnLoging = findViewById(R.id.login_button);
        phoneLogin = findViewById(R.id.phone_number);

        progressDialog = new ProgressDialog(this);
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(LogInActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendUserToPhoneLogIn() {
        Intent intent = new Intent(LogInActivity.this, PhoneLogInActivity.class);
        startActivity(intent);
    }

    private void sendUserToRegisterAccount() {
        Intent intent = new Intent(LogInActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}