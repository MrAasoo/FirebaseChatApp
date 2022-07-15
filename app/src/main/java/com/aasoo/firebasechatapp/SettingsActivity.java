package com.aasoo.firebasechatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aasoo.firebasechatapp.login_signup.RegisterActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    public static final int GALLERY_CODE = 1;
    private EditText userName, userStatus;
    private Button btnUpdate;
    private CircleImageView userImage;
    private FirebaseAuth mAuth;
    private DatabaseReference mReference;
    private StorageReference mUserProfileStorage, mStorage;
    private String image, currentUser;
    private String timeUploaded, valid;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initialize();
        //retrieve data
        retrieveData();

        //Update settings
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!TextUtils.isEmpty(userName.getText().toString().trim()) && !TextUtils.isEmpty(userStatus.getText().toString().trim())) {
                    updateSettings();
                    sendUserToMainActivity();
                } else if (TextUtils.isEmpty(userName.getText().toString().trim()) && TextUtils.isEmpty(userStatus.getText().toString().trim())) {
                    userName.setError("Enter user name");
                    userStatus.setError("Can not be empty");
                    userName.requestFocus();
                } else if (TextUtils.isEmpty(userName.getText().toString().trim())) {
                    userName.setError("Enter user name");
                    userName.requestFocus();
                } else if (TextUtils.isEmpty(userStatus.getText().toString().trim())) {
                    userStatus.setError("Can not be empty");
                    userStatus.requestFocus();
                }
            }
        });

        //profile image
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image from here"), GALLERY_CODE);
            }
        });

    }

    private void retrieveData() {
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateSettings() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("user_id", currentUser);
        map.put("user_name", userName.getText().toString().trim());
        map.put("user_status", userStatus.getText().toString().trim());
        mReference.child("Users").child(currentUser).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                }
            }
        });
    }

    private void initialize() {
        userName = findViewById(R.id.user_name_edit_text);
        userStatus = findViewById(R.id.user_status_edit_text);
        userImage = findViewById(R.id.user_image);
        btnUpdate = findViewById(R.id.update_button);

        //user
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();

        //database
        mReference = FirebaseDatabase.getInstance().getReference();

        //Storage
        mStorage = FirebaseStorage.getInstance().getReference().child("Profile Image");
        mUserProfileStorage = mStorage.child(currentUser + ".jpg");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && requestCode == RESULT_OK && data != null) {
            CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                progressDialog.setTitle("Set profile image");
                progressDialog.setMessage("please wait while updating account...");
                progressDialog.setCancelable(false);
                progressDialog.create();
                progressDialog.show();
                Uri resultUri = result.getUri();
                mUserProfileStorage.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.cancel();
                        progressDialog.dismiss();
                        getImage();
                        mReference.child("Users").child(currentUser).child("user_image").setValue(image);
                        Toast.makeText(SettingsActivity.this, "Image updated", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void getImage() {
        mUserProfileStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                image = uri.toString();
                Glide.with(getApplicationContext()).load(uri).into(userImage);
            }
        });
    }
}