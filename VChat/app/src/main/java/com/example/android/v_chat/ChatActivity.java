package com.example.android.v_chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.v_chat.helper.MessageAdapter;
import com.example.android.v_chat.helper.Messages;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    ImageButton sendMsg,sendFiles;
    ImageView chat_bg;
    String msgRecId,msRecname,msgRecImg="",messageSenderid;
    TextView username , lastSeen;
    FirebaseAuth auth;
    FirebaseStorage contactBackgroundRef;
    DatabaseReference RootRef;
    Button ConatctSettings;
    CircleImageView userImg;
    Uri fileuri;
    EditText message;
    Toolbar toolbar;
    ProgressDialog progressDialog, changeBgProgress;
    LinearLayoutManager linearLayoutManager;
    MessageAdapter messageAdapter;
    List<Messages> messagesList=new ArrayList<>();
    RecyclerView recyclerView;
    String saveCurrentTime,saveCurrentDate,checker="";
    UploadTask uploadTask;
     DatabaseReference ContactRef;
     StorageReference customChatBackgorund;
    private Uri imageuri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        auth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();
        customChatBackgorund= FirebaseStorage.getInstance().getReference().child("Chat_Bg");
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat dateFormat=new SimpleDateFormat("MMM dd,yyyy");
        SimpleDateFormat timeFormat=new SimpleDateFormat("hh:mm a");
        saveCurrentDate=dateFormat.format(calendar.getTime());
        saveCurrentTime=timeFormat.format(calendar.getTime());
        msgRecId=getIntent().getExtras().get("uid").toString();
        msRecname=getIntent().getExtras().get("name").toString();
        msgRecImg=getIntent().getExtras().get("image").toString();
        chat_bg=findViewById(R.id.chat_background);
        message=findViewById(R.id.chat_input_message);
        toolbar=findViewById(R.id.custom_chat_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        ConatctSettings=findViewById(R.id.contactsettings);
        ConatctSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[]=new CharSequence[]
                        {
                                "Go to contact settings",
                                "Change chat background",
                                "Clear Chat"
                        };

                AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select Action");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0)
                        {
                            SendToPorfileActivity();
                        }
                        if(i==1)
                        {
                            changeBackground();
                        }
                        if(i==2)
                        {
                            clearAllChat();
                        }
                    }
                });
                builder.show();


            }
        });
        username=findViewById(R.id.custom_profile_name);
        lastSeen=findViewById(R.id.custom_user_last_seen);
        userImg=findViewById(R.id.custom_profile_image);
        sendMsg=findViewById(R.id.send_message_chat);
        sendFiles=findViewById(R.id.file_attachment);
        messageSenderid=auth.getCurrentUser().getUid();
        messageAdapter=new MessageAdapter(messagesList,getApplicationContext());
        if (msgRecId!=null && msRecname!=null)
        {
            username.setText(msRecname);
            if (msgRecImg!=null)
            {
                GetImage(msgRecImg,userImg);

            }

        }
        if(msgRecId!=null)
        {
            GetBackgroundImage(msgRecId,chat_bg);
        }
        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });
        linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView=findViewById(R.id.chat_recycler_view);
        DisplayLastSeen();
        progressDialog=new ProgressDialog(this);
        changeBgProgress=new ProgressDialog(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messageAdapter);
        sendFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[]=new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "Ms Word Files"
                        };
                AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0)
                        {
                            checker="image";
                            Intent intent=new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent,"Select Image from here"),
                                    438);
                        }
                        if(i==1)
                        { checker="pdf";
                            Intent intent=new Intent();
                            intent.setType("application/pdf");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent,"Select Pdf from here"),
                                    438);
                        }
                        if(i==2)
                        {   checker="docx";
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("*/*");
                            String[] mimetypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword"};
                            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                            startActivityForResult(intent, 438);
                        }
                    }
                });
                builder.show();
            }
        });
        setRecyclerView();
    }

    private void clearAllChat() {
        progressDialog.setTitle("Clear Chat");
        progressDialog.setMessage("Deleting chats");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        StorageReference imageFilesChatRef=FirebaseStorage.getInstance().getReference().child("Image Files");
        StorageReference docfilesChatRef=FirebaseStorage.getInstance().getReference().child("Document Files");
        RootRef.child("Messages").child(messageSenderid).child(msgRecId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.cancel();
            }
        });
                messagesList.clear();
                messageAdapter.notifyItemRangeRemoved(0, messagesList.size());
                recyclerView.setAdapter(messageAdapter);

    }

    private void changeBackground() {
        checker="background_image";
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Image from here"),
                438);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");

    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("offline");
    }

    private void updateUserStatus(String status) {
        FirebaseUser currentUser=auth.getCurrentUser();
        String currentUserId= currentUser.getUid();
        RootRef.child("Users").child(currentUserId).child("userState").child("state").setValue(status);
    }


    private void setRecyclerView() {
        RootRef.child("Messages").child(messageSenderid).child(msgRecId)
                .addChildEventListener(new ChildEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Messages messages=snapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void DisplayLastSeen() {
        RootRef.child("Users").child(msgRecId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("userState").hasChild("state")) {
                    String state = snapshot.child("userState").child("state").getValue().toString();
                    String date = snapshot.child("userState").child("date").getValue().toString();
                    String time = snapshot.child("userState").child("time").getValue().toString();
                    if (state.equals("online")) {
                        lastSeen.setText("online");

                    } else if (state.equals("offline")) {

                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy");
                        String CurrentDate = dateFormat.format(calendar.getTime());
                        if (CurrentDate.equals(date)) {
                            lastSeen.setText(time.toLowerCase(Locale.ROOT));

                        } else {
                            lastSeen.setText(date);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void SendMessage() {

        String messageText=message.getText().toString();
        if (TextUtils.isEmpty(messageText))
        {
            message.setError("Please enter a message");

        }
        else
        {
            String messageSenderRef="Messages/"+messageSenderid+"/"+msgRecId;
            String messageRecieverRef="Messages/"+msgRecId+"/"+messageSenderid;
            DatabaseReference userMessageRef=RootRef.child("Messages")
                    .child(messageSenderid).child(msgRecId).push();
            String messagePushId=userMessageRef.getKey();
            Map messageTextReady = new HashMap();
            messageTextReady.put("message",messageText);
            messageTextReady.put("type","text");
            messageTextReady.put("from",messageSenderid);
            messageTextReady.put("to",msgRecId);
            messageTextReady.put("messageID",messagePushId);
            messageTextReady.put("time", saveCurrentTime);
            messageTextReady.put("date", saveCurrentDate);
            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextReady);
            messageBodyDetails.put(messageRecieverRef+"/"+messagePushId,messageTextReady);
            RootRef.updateChildren(messageBodyDetails);
            message.setText("");
        }
    }

    private void SendToPorfileActivity() {
        Intent profileIntent=new Intent(ChatActivity.this, ProfileActivity.class);
        profileIntent.putExtra("visited_uid",msgRecId);
        startActivity(profileIntent);
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
    private void GetBackgroundImage( String receiverUser, ImageView imageView)
    {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().
                child("Chat_Bg/" + receiverUser + ".jpg");
        if(storageReference!=null) {
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getApplicationContext()).load(uri).into(imageView);
                    progressDialog.cancel();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==438 && resultCode==RESULT_OK)
        {

            fileuri=data.getData();
            if(checker == "background_image")
            {
                changeBgProgress.setTitle("Updating Background");
                changeBgProgress.setMessage("Please wait while we update the chat background");
                changeBgProgress.setCanceledOnTouchOutside(false);
                changeBgProgress.show();
                imageuri=data.getData();
                String currentUser=auth.getCurrentUser().getUid();
                StorageReference filePath= customChatBackgorund.child(msgRecId+".jpg");
                ContactRef= FirebaseDatabase.getInstance().getReference().child("Contacts");

                filePath.putFile(imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                        GetBackgroundImage(msgRecId,chat_bg);

                        changeBgProgress.cancel();
                        ContactRef.child(currentUser).child(msgRecId).child("background").setValue(msgRecId);
                    }
                });

            }
            if (checker.equals("pdf") || checker.equals("docx"))
            {
                progressDialog.setTitle("Sending File");
                progressDialog.setMessage("Please wait , we are sending the file");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                StorageReference storageReference=FirebaseStorage.getInstance().getReference().child("Document Files");
                String messageSenderRef="Messages/"+messageSenderid+"/"+msgRecId;
                String messageRecieverRef="Messages/"+msgRecId+"/"+messageSenderid;
                DatabaseReference userMessageRef=RootRef.child("Messages")
                        .child(messageSenderid).child(msgRecId).push();
                String messagePushId=userMessageRef.getKey();
                StorageReference filePath= storageReference.child(messagePushId+"."+checker);
                filePath.putFile(fileuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        Map messageTextReady = new HashMap();
                        messageTextReady.put("message",messagePushId);
                        messageTextReady.put("name",fileuri.getLastPathSegment());
                        messageTextReady.put("type",checker);
                        messageTextReady.put("from",messageSenderid);
                        messageTextReady.put("to",msgRecId);
                        messageTextReady.put("messageID",messagePushId);
                        messageTextReady.put("time", saveCurrentTime);
                        messageTextReady.put("date", saveCurrentDate);
                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextReady);
                        messageBodyDetails.put(messageRecieverRef+"/"+messagePushId,messageTextReady);
                        RootRef.updateChildren(messageBodyDetails);
                        message.setText("");
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), ""+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        int progress = (int) ((100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                    }
                });


            }
            else if (checker.equals("image"))
            {
                progressDialog.setTitle("Sending File");
                progressDialog.setMessage("Please wait , we are sending the file");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                StorageReference storageReference=FirebaseStorage.getInstance().getReference().child("Image Files");
                String messageSenderRef="Messages/"+messageSenderid+"/"+msgRecId;
                String messageRecieverRef="Messages/"+msgRecId+"/"+messageSenderid;
                DatabaseReference userMessageRef=RootRef.child("Messages")
                        .child(messageSenderid).child(msgRecId).push();
                String messagePushId=userMessageRef.getKey();
                StorageReference filePath= storageReference.child(messagePushId+".jpg");
                uploadTask=filePath.putFile(fileuri);
                uploadTask.continueWith(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        Map messageTextReady = new HashMap();
                        messageTextReady.put("message",messagePushId);
                        messageTextReady.put("name",fileuri.getLastPathSegment());
                        messageTextReady.put("type",checker);
                        messageTextReady.put("from",messageSenderid);
                        messageTextReady.put("to",msgRecId);
                        messageTextReady.put("messageID",messagePushId);
                        messageTextReady.put("time", saveCurrentTime);
                        messageTextReady.put("date", saveCurrentDate);
                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextReady);
                        messageBodyDetails.put(messageRecieverRef+"/"+messagePushId,messageTextReady);
                        RootRef.updateChildren(messageBodyDetails);
                        message.setText("");
                        progressDialog.dismiss();
                    }
                });

            }
        }
        else
        {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Nothing is Selected", Toast.LENGTH_SHORT).show();
        }
    }
}