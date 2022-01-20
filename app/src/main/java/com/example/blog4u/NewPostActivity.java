package com.example.blog4u;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class NewPostActivity extends AppCompatActivity {

    private Toolbar newPostToolbar;
    private ImageView newPostImage;
    private Button newPostBtn;
    private ProgressBar newPostProgBar;
    private Uri postImageUri = null;
    private FirebaseDatabase database;
    private String userId;
    private FirebaseAuth firebaseAuth;



    //TODO maybe need to change the multiline to not be multiLined.
    private EditText newPostDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        
        findViews();
        initView();


    }

    private void initView() {
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newPostImage.setOnClickListener(v -> {
            //TODO need to use crop/pick image here just like in setup activity.
            //TODO also need to copy onActivityResult func and make some changes - part9 4:50~ and from that to get the new post Uri and set the new post image Uri from that
            Toast.makeText(NewPostActivity.this, "Need to put here image crop", Toast.LENGTH_LONG).show();
        });

        newPostBtn.setOnClickListener(v -> {
            String description = newPostDescription.getText().toString();

            if(!TextUtils.isEmpty(description) /*&& postImageUri!=null /*TODO after implementing image crop*/){
                newPostProgBar.setVisibility(View.VISIBLE);
                //TODO need to implement here putfile into firebase storage - part9 12:30~
                //TODO after that, can use Compressor for thumbnails - part 10 7:40~

                withoutImageStore(description);

            }
        });
    }

    private void withoutImageStore(String description) {
        DatabaseReference myRef = database.getReference("Posts");
        //a random id for a new post.
        String randomPostId = UUID.randomUUID().toString();//FieldValue.serverTimestamp().toString();

        Map<String, String> postMap = new HashMap<>();
        postMap.put("description", description);
        postMap.put("userId", userId);
        postMap.put("timestamp", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())));
        //TODO need to add here the image uri
//        postMap.put("image_url", postImageUri);

        //TODO maybe not needed a random ID
        myRef.child(randomPostId).setValue(postMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(NewPostActivity.this, "A new post has been created!", Toast.LENGTH_LONG).show();


                    Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                } else {
                    String error = "Exception at storing postMap into database!";
                    Toast.makeText(NewPostActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
        //TODO maybe need to put in isSuccessful
        newPostProgBar.setVisibility(View.INVISIBLE);
    }


    private void findViews() {
        newPostToolbar = findViewById(R.id.tb_new_post);
        newPostDescription = findViewById(R.id.et_multiLine_new_post);
        newPostBtn = findViewById(R.id.btn_new_post);
        newPostImage = findViewById(R.id.iv_new_post);
        newPostProgBar = findViewById(R.id.pb_newPost);
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();
    }
}