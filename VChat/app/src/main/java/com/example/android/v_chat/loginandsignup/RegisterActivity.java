package com.example.android.v_chat.loginandsignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.v_chat.MainActivity;
import com.example.android.v_chat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class RegisterActivity extends AppCompatActivity {
    EditText inputemail;
    EditText inputpassword;
    EditText confirmpassword;
    Button signup;
    TextView alreadyhavingaccount;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference rootRef;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        inputemail=findViewById(R.id.register_email);
        inputpassword=findViewById(R.id.password);
        confirmpassword=findViewById(R.id.confirm_password);
        signup=findViewById(R.id.signup_button);
        alreadyhavingaccount=findViewById(R.id.back_to_login);
        mAuth=FirebaseAuth.getInstance();
        mUser= mAuth.getCurrentUser();
        rootRef= FirebaseDatabase.getInstance().getReference();

        alreadyhavingaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isInternetAvailable(RegisterActivity.this))
                    authenticate();
            }
        });
    }

    private void authenticate() {
        String email=inputemail.getText().toString();
        String password=inputpassword.getText().toString();
        String confirmation= confirmpassword.getText().toString();


        if(!email.matches(emailPattern))
        {
            inputemail.setError("please enter correct email");
        }
        else if(password.isEmpty() || password.length()<6)
        {
            inputpassword.setError("password must contain atleast 6 characters");
        }
        else if(!password.equals(confirmation))
        {
            confirmpassword.setError("password doesn't match");
        }
        else
        {

            mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                    boolean check = !task.getResult().getSignInMethods().isEmpty();
                    if(!check)
                    {
                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful())
                                {
                                    final String[] deviceToken=new String[1];
                                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                        @Override
                                        public void onComplete(@NonNull Task<String> task) {
                                            deviceToken[0]=task.getResult();
                                        }
                                    });

                                    String currrentUserId=mAuth.getCurrentUser().getUid();
                                    rootRef.child("Users").child(currrentUserId).setValue("");
                                    rootRef.child("Users").child(currrentUserId).child("device_token").setValue(deviceToken[0]);

                                    sendUserToNextActivity();
                                    Toast.makeText(RegisterActivity.this, "Signed Up Successfully",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    else
                    {
                        Toast.makeText(RegisterActivity.this, "Account already exists try logging in", Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }
    }

    private void sendUserToNextActivity() {
        Intent intent=new Intent(RegisterActivity.this, MainActivity.class);

        //This will stop to come back to this activity when user successfully signs up
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public boolean isInternetAvailable(Context context)
    {
        NetworkInfo info = (NetworkInfo) ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (info == null)
        {
            Toast.makeText(RegisterActivity.this, "no internet connection",Toast.LENGTH_SHORT).show();
            return false;
        }
        else
        {
            if(info.isConnected())
            {
                // Toast.makeText(RegisterActivity.this, "connection established",Toast.LENGTH_SHORT).show();
                return true;
            }
            else
            {
                Toast.makeText(RegisterActivity.this, "internet connection",Toast.LENGTH_SHORT).show();
                return true;
            }

        }
    }
}