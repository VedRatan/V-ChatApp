package com.example.android.v_chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.example.android.v_chat.helper.TabAdapter;
import com.example.android.v_chat.loginandsignup.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    ViewPager mainviewPager;
    TabAdapter mainTabAdapter;
    TabLayout tabLayout;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar =findViewById(R.id.main_activity_toolbar);
        mAuth=FirebaseAuth.getInstance();
        mainviewPager=findViewById(R.id.main_tab_viewpager);
        tabLayout=findViewById(R.id.main_tabs);
        mainTabAdapter = new TabAdapter(getSupportFragmentManager());
        mainviewPager.setAdapter(mainTabAdapter);
        setSupportActionBar(toolbar);
        tabLayout.setupWithViewPager(mainviewPager);
        getSupportActionBar().setTitle("VChat");
        rootRef= FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser= mAuth.getCurrentUser();
        if(currentUser == null)
        {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
        else
        {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Loading Chats");
            progressDialog.setMessage("Please wait while we load your chats");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            updateUserStatus("online");
            verifyExistence();
        }
    }

    private void updateUserStatus(String status) {

        String curUserId = mAuth.getCurrentUser().getUid();
        String curTime, curDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy");
        curDate=dateFormat.format(calendar.getTime());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        curTime=timeFormat.format(calendar.getTime());

        HashMap<String , Object> onlineStatus = new HashMap<>();
        onlineStatus.put("time",curTime);
        onlineStatus.put("date",curDate);
        onlineStatus.put("state",status);
        rootRef.child("Users").child(curUserId).child("userState").updateChildren(onlineStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.cancel();
            }
        });


    }

    private void verifyExistence(){
        String currentID=mAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!(snapshot.child("name")).exists()){
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser currentUser= mAuth.getCurrentUser();
        if(currentUser != null)
        {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        FirebaseUser currentUser= mAuth.getCurrentUser();
        if(currentUser != null)
        {
            updateUserStatus("online");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent main=new Intent(Intent.ACTION_MAIN);
        main.addCategory(Intent.CATEGORY_DEFAULT);
        main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(main);
        FirebaseUser currentUser= mAuth.getCurrentUser();
        if(currentUser != null)
        {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser= mAuth.getCurrentUser();
        if(currentUser != null)
        {
            updateUserStatus("offline");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemid= item.getItemId();
        if(itemid==R.id.logout)
        {
            FirebaseUser currentUser= mAuth.getCurrentUser();
            if(currentUser != null)
            {
                updateUserStatus("offline");
            }
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }

        if(R.id.settings == item.getItemId())
        {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }
        return true;
    }
}