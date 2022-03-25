package com.example.android.v_chat.loginandsignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.SignInMethodQueryResult;

public class PasswordRecovery extends AppCompatActivity {
    private Button sendMail;
    private EditText mail;
    private String email;
    private TextView backtologin;
    private FirebaseAuth mAuth;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recovery);

        mAuth=FirebaseAuth.getInstance();
        backtologin=findViewById(R.id.tologinpage);
        mail=findViewById(R.id.email);
        sendMail=findViewById(R.id.getmail);

        backtologin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PasswordRecovery.this,LoginActivity.class));
            }
        });

        sendMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validatePassword();
            }
        });
    }

    private void validatePassword() {

        email=mail.getText().toString();
        if(email.isEmpty())
            mail.setError("required field");
        else if(!email.matches(emailPattern))
        {
            mail.setError("please enter correct email");
        }
        else
        {

            mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                    boolean check = !task.getResult().getSignInMethods().isEmpty();
                    if(!check)
                    {
                        Toast.makeText(PasswordRecovery.this, "Account doesn't exists try with different mail id", Toast.LENGTH_SHORT).show();

                    }

                    else {
                        sendResetPassMail();
                    }

                }
            });
        }
    }

    private void sendResetPassMail() {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(PasswordRecovery.this, "please check your mail inbox", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(PasswordRecovery.this, LoginActivity.class));
                    finish();
                }
                else
                {
                    Toast.makeText(PasswordRecovery.this, "sorry, unable to send the reset mail", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}