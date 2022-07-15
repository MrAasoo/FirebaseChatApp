package com.aasoo.firebasechatapp.login_signup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.opengl.ETC1;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aasoo.firebasechatapp.MainActivity;
import com.aasoo.firebasechatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.installations.FirebaseInstallations;

import java.util.concurrent.TimeUnit;

public class PhoneLogInActivity extends AppCompatActivity {

    PhoneAuthProvider.OnVerificationStateChangedCallbacks callback;
    String verificationId;
    PhoneAuthProvider.ForceResendingToken token;
    private EditText phoneNumber, verificationCode;
    private Button btnVerificationCode, btnVerify, btnEmail;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_log_in);

        initialize();
        mAuth = FirebaseAuth.getInstance();

        btnEmail.setOnClickListener(view -> onBackPressed());

        btnVerificationCode.setOnClickListener(view -> {
            //Check phone number not empty
            String phone = phoneNumber.getText().toString();
            if (TextUtils.isEmpty(phone)) {
                phoneNumber.setError("Please enter phone number");
                phoneNumber.requestFocus();
            } else {

                progressDialog.setTitle("Phone verification");
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.create();
                progressDialog.show();

                //Firebase phone auth
                PhoneAuthOptions phoneAuthOptions = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91" + phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(PhoneLogInActivity.this)
                        .setCallbacks(callback)
                        .build();
                PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
                mAuth.setLanguageCode("en");

            }
        });

        callback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                progressDialog.cancel();
                progressDialog.dismiss();
                verificationCode.setText(phoneAuthCredential.getSmsCode());
                signInWithPhoneCredentials(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e(PhoneLogInActivity.class.getSimpleName(), "onVerificationFailed: ", e);
                progressDialog.cancel();
                progressDialog.dismiss();
                Toast.makeText(PhoneLogInActivity.this, "Invalid Phone Number! Please enter valid phone number", Toast.LENGTH_SHORT).show();
                //Change UI visibility
                phoneNumber.setVisibility(View.VISIBLE);
                verificationCode.setVisibility(View.GONE);
                btnVerificationCode.setVisibility(View.VISIBLE);
                btnVerify.setVisibility(View.GONE);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(PhoneLogInActivity.this, "Invalid request : " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(PhoneLogInActivity.this, "Limit Expired : " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken ftoken) {
                progressDialog.dismiss();
                progressDialog.dismiss();
                verificationId = s;
                token = ftoken;
                Toast.makeText(PhoneLogInActivity.this, "Code sent", Toast.LENGTH_SHORT).show();
                //Change UI visibility
                phoneNumber.setVisibility(View.GONE);
                verificationCode.setVisibility(View.VISIBLE);
                btnVerificationCode.setVisibility(View.GONE);
                btnVerify.setVisibility(View.VISIBLE);
            }
        };

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = verificationCode.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    verificationCode.setError("Please enter verification code");
                    verificationCode.requestFocus();
                } else {
                    progressDialog.setTitle("Code verification");
                    progressDialog.setMessage("Please wait, while we are verifying code...");
                    progressDialog.setCancelable(false);
                    progressDialog.create();
                    progressDialog.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                    signInWithPhoneCredentials(credential);
                }
            }
        });

    }

    private void initialize() {
        phoneNumber = findViewById(R.id.phone_number);
        verificationCode = findViewById(R.id.phone_verification_code);
        btnVerificationCode = findViewById(R.id.verification_code_button);
        btnVerify = findViewById(R.id.verify_code_button);

        //Change UI visibility
        phoneNumber.setVisibility(View.VISIBLE);
        verificationCode.setVisibility(View.GONE);
        btnVerificationCode.setVisibility(View.VISIBLE);
        btnVerify.setVisibility(View.GONE);

        btnEmail = findViewById(R.id.email);

        progressDialog = new ProgressDialog(this);
    }

    public void signInWithPhoneCredentials(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    String userId = mAuth.getCurrentUser().getUid();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
                    reference.child(userId).child("device_token").setValue(deviceToken);
                    Toast.makeText(PhoneLogInActivity.this, "You are successfully logged in", Toast.LENGTH_SHORT).show();
                    sendUserToMainActivity();
                } else {
                    Log.e(PhoneLogInActivity.class.getSimpleName(), "task fail: ", task.getException());
                }
                progressDialog.cancel();
                progressDialog.dismiss();
            }
        });
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(PhoneLogInActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}