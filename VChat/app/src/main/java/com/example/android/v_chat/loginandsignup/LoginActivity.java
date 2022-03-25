package com.example.android.v_chat.loginandsignup;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {
    TextView register;
    EditText inputemail;
    EditText inputpassword;
    TextView forgotpassword;
    TextView phoneAuthentication;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    Button login;
    DatabaseReference rootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        inputemail= findViewById(R.id.login_email);
        inputpassword=findViewById(R.id.login_password);
        register=findViewById(R.id.register);
        login = findViewById(R.id.login_button);
        forgotpassword=findViewById(R.id.forgot_password);
        phoneAuthentication=findViewById(R.id.phone_login);
        mAuth=FirebaseAuth.getInstance();
        rootRef= FirebaseDatabase.getInstance().getReference();
        mUser= mAuth.getCurrentUser();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isInternetAvailable(LoginActivity.this))
                {
                    giveAccess();
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });


        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, PasswordRecovery.class));
            }
        });

        phoneAuthentication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, PhoneLoginActivity.class));
            }
        });

        if(mAuth.getCurrentUser() != null) {
        sendUserToNextActivity();
        }


    }

    private void giveAccess() {
        String email = inputemail.getText().toString();
        String password = inputpassword.getText().toString();
        if (!email.matches(emailPattern)) {
            inputemail.setError("please enter correct email");
        }

        else if (password.isEmpty() || password.length() < 6) {
            inputpassword.setError("password must contain atleast 6 characters");
        }

        else {
            mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                    boolean check = !task.getResult().getSignInMethods().isEmpty();
                    if(!check)
                    {
                        Toast.makeText(LoginActivity.this, "account doesn't exist please create one", Toast.LENGTH_SHORT).show();
                    }

                    else
                    {

                        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
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
                                    rootRef.child(currrentUserId).child("device_token").setValue(deviceToken[0]).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            sendUserToNextActivity();
                                            Toast.makeText(LoginActivity.this, "signed in",Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                                else
                                {

                                    Toast.makeText(LoginActivity.this, "incorrect email or password",Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }
                }
            });
        }
    }

    private void sendUserToNextActivity() {
        Intent intent= new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }


    public boolean isInternetAvailable(Context context)
    {
        NetworkInfo info = (NetworkInfo) ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (info == null)
        {
            Toast.makeText(LoginActivity.this, "no internet connection",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(LoginActivity.this, "internet connection",Toast.LENGTH_SHORT).show();
                return true;
            }

        }
    }
}