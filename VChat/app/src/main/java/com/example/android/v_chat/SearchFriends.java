package com.example.android.v_chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.v_chat.helper.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchFriends extends AppCompatActivity {

    RecyclerView searchFriendsList;
    Toolbar toolbar;
    DatabaseReference userref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friends);

        searchFriendsList = findViewById(R.id.findfriendsrecyclerview);
        toolbar=findViewById(R.id.search_friends_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
        userref= FirebaseDatabase.getInstance().getReference().child("Users");
        searchFriendsList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(userref,Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts,FindFriendViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Contacts model) {
                holder.userName.setText(model.getName());
                holder.userStatus.setText(model.getStatus());
                if (model.getImage()!=null)
                {
                    GetImage(model.getImage(),holder.profileImage);
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String user=getRef(position).getKey();
                        Intent profileIntent=new Intent(SearchFriends.this,ProfileActivity.class);
                        profileIntent.putExtra("visited_uid",user);
                        startActivity(profileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.user_profile,parent,false);
                FindFriendViewHolder viewHolder=new FindFriendViewHolder(view);
                return viewHolder;
            }
        };

        searchFriendsList.setAdapter(adapter);
        adapter.startListening();
    }
    public static class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;
        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_profile_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            userStatus.setVisibility(View.VISIBLE);
        }
    }
    private void GetImage(String currentUser, CircleImageView imageView) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().
                child("Profile Images/" + currentUser + ".jpg");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(imageView);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent(SearchFriends.this,MainActivity.class);
        startActivity(intent);
    }
}
