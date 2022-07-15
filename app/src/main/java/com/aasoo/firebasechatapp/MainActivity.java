package com.aasoo.firebasechatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.aasoo.firebasechatapp.helper.TabAccesorAdapter;
import com.aasoo.firebasechatapp.login_signup.LogInActivity;
import com.aasoo.firebasechatapp.login_signup.RegisterActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    ViewPager mainViewPager;
    TabAccesorAdapter tabAccesorAdapter;
    TabLayout mainTabLayout;

    private FirebaseAuth mAuth;
    private DatabaseReference mReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

        //Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Firebase ChatApp");

        //view pager
        tabAccesorAdapter = new TabAccesorAdapter(getSupportFragmentManager());
        mainViewPager.setAdapter(tabAccesorAdapter);
        mainTabLayout.setupWithViewPager(mainViewPager);

        //init Firebase auth and database ref
        mAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference();


    }

    private void initialize() {
        toolbar = findViewById(R.id.main_activity_toolbar);
        mainViewPager = findViewById(R.id.main_tab_viewPager);
        mainTabLayout = findViewById(R.id.main_tabs);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            mAuth.signOut();
            sendUserToLoginActivity();
        }
    }

    private void sendUserToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendUserToSettingsActivity() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.main_logout:
                mAuth.signOut();
                sendUserToLoginActivity();
                break;

            case R.id.main_settings:
                sendUserToSettingsActivity();
                break;
        }
        return true;
    }
}