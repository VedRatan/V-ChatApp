package com.example.android.v_chat.loginandsignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.v_chat.MainActivity;
import com.example.android.v_chat.R;
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

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    EditText phoneNumber, verificationCode;
    Button sendVerificationCode, verify;
    String verifyBySystem;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        phoneNumber=findViewById(R.id.phone_number);
        verificationCode=findViewById(R.id.verification_code);
        sendVerificationCode=findViewById(R.id.send_verification_code);
        verify=findViewById(R.id.verify);
        mAuth=FirebaseAuth.getInstance();


         sendVerificationCode.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 String phone=phoneNumber.getText().toString();
                 Log.d("ved", "phonr number is "+phone);
                 if(phoneNumber.length()!=10)
                     phoneNumber.setError("Invalid phone number");
                 else if(phoneNumber.equals(""))
                 {
                     phoneNumber.setError("enter the phone number first");
                 }

                 sendVerificationCodeToUser(phone);
             }
         });

         verify.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 String code = verificationCode.getText().toString().trim();
                 if(code.isEmpty() || code.length()<6)
                 {
                     verificationCode.setError("wrong OTP");
                     verificationCode.requestFocus();
                     return;
                 }
                 verifyCode(code);
             }
             });



    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {



        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            String code= credential.getSmsCode();
            verificationCode.setText(code);
            if(code!=null)
            {
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.


            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(PhoneLoginActivity.this, "Verifcation Failed", Toast.LENGTH_SHORT).show();
                // Invalid request
            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Toast.makeText(PhoneLoginActivity.this, "SMS Quota For Today Has Been Exceeded", Toast.LENGTH_SHORT).show();

            }

            // Show a message and update the UI
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Toast.makeText(PhoneLoginActivity.this, "Verification Code Sent", Toast.LENGTH_SHORT).show();

            // Save verification ID and resending token so we can use them later

            sendVerificationCode.setVisibility(View.GONE);

            verifyBySystem=verificationId;
        }
    };


    private void sendVerificationCodeToUser(String phone) {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91"+phone)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCode(String code)
    {

        PhoneAuthCredential phoneAuthCredential=PhoneAuthProvider.getCredential(verifyBySystem, code);
        signInByCredentials(phoneAuthCredential);
    }

    private void signInByCredentials(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(PhoneLoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(PhoneLoginActivity.this, "plugged in to nirvana", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(PhoneLoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(PhoneLoginActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}