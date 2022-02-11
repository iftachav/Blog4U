package com.example.blog4u.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.blog4u.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private Button signInBtn;
    private Button moveToSignUpBtn;
    private EditText signInEmail;
    private EditText signInPassword;
    private FirebaseAuth mAuth;
    private ProgressBar signInProgBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        findViews();
        initButtons();


    }

    private void initButtons() {
        moveToSignUpBtn.setOnClickListener(v -> myStartAcitivity(SignUpActivity.class));

        signInBtn.setOnClickListener(v -> {
            String emailStr = signInEmail.getText().toString();
            String passStr = signInPassword.getText().toString();

            if(!TextUtils.isEmpty(emailStr) && !TextUtils.isEmpty(passStr)){
                signInProgBar.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(emailStr,passStr).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        //TODO maybe can send straight to feed (or maybe the feed is the main activity).
                        sendToMain();
                    } else {
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(SignInActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                    signInProgBar.setVisibility(View.INVISIBLE);
                });
            }
        });
    }

    private void myStartAcitivity(Class<?> activityClass) {
        Intent myIntent = new Intent(this, activityClass);
        startActivity(myIntent);
    }

    private void findViews() {
        signInBtn = findViewById(R.id.btn_singIn);
        moveToSignUpBtn = findViewById(R.id.btn_moveToSignUp);
        signInEmail = findViewById(R.id.et_signInEmail);
        signInPassword = findViewById(R.id.et_signInPassword);
        mAuth = FirebaseAuth.getInstance();
        signInProgBar = findViewById(R.id.pb_singIn);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //TODO not sure this is needed.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //TODO maybe can send straight to feed?
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Blog4U!")
                .setMessage("Are you sure you want to close this app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }
}