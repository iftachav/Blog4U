package com.example.blog4u.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.blog4u.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private Button signUp;
    private Button moveToSignIn;
    private EditText signUpEmail;
    private EditText signUpPassword;
    private EditText signUpRePassword;
    private ProgressBar signUpProgBar;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        findViews();
        initButtons();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            sendToMain();
        }
    }

    private void initButtons() {
        moveToSignIn.setOnClickListener(view -> finish());

        signUp.setOnClickListener(v -> {
            String emailStr = signUpEmail.getText().toString();
            String passStr = signUpPassword.getText().toString();
            String rePassStr = signUpRePassword.getText().toString();

            if(!TextUtils.isEmpty(emailStr) && !TextUtils.isEmpty(passStr) && !TextUtils.isEmpty(rePassStr)){
                if(passStr.equals(rePassStr)){
                    signUpProgBar.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(emailStr,passStr).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Intent setupIntent = new Intent(SignUpActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();
                        } else {
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(SignUpActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                        signUpProgBar.setVisibility(View.INVISIBLE);
                    });
                } else {
                    Toast.makeText(SignUpActivity.this, "Retype Password and Password fields does not match!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void findViews() {
        signUp = findViewById(R.id.btn_signUp);
        moveToSignIn = findViewById(R.id.btn_moveToSignIn);
        signUpEmail = findViewById(R.id.et_signUpEmail);
        signUpPassword = findViewById(R.id.et_signUpPassword);
        signUpRePassword = findViewById(R.id.et_signUpRePassword);
        signUpProgBar = findViewById(R.id.pb_signUp);
        mAuth = FirebaseAuth.getInstance();
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}