package com.example.blog4u;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import id.zelory.compressor.Compressor;


public class NewPostActivity extends AppCompatActivity {

    private Toolbar newPostToolbar;
    private ImageView newPostImage;
    private Button newPostBtn;
    private ProgressBar newPostProgBar;
    private Uri postImageUri = null;
    private FirebaseDatabase database;
    private String userId;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private Bitmap compressedImageFile;




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
        newPostProgBar.bringToFront();

        newPostImage.setOnClickListener(v -> {
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setMinCropResultSize(512,512).setAspectRatio(1,1).start(NewPostActivity.this);
        });

        newPostBtn.setOnClickListener(v -> {
            String description = newPostDescription.getText().toString();
            if(!TextUtils.isEmpty(description) && postImageUri!=null ){
                newPostProgBar.setVisibility(View.VISIBLE);
                storeToDatabase(description);
            }
        });
    }

    private void storeToDatabase(String description) {
        //a random id for a new post.
        String randomPostId = UUID.randomUUID().toString();
        StorageReference postImagePath = storageReference.child("posts_images").child(randomPostId + ".jpg");
        postImagePath.putFile(postImageUri).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                //Making a thumbnail.
                File newImageFile = new File(postImageUri.getPath());
                try {
                    compressedImageFile = new Compressor(NewPostActivity.this).setMaxHeight(100).setMaxWidth(100).setQuality(2).compressToBitmap(newImageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                byte[] thumbData = baos.toByteArray();

                //uploading the thumbnail.
                StorageReference postThumbPath = storageReference.child("posts_images/thumbs").child(randomPostId + ".jpg");
                postThumbPath.putBytes(thumbData).addOnCompleteListener(v -> {
                    //getting the URI for the image and thumbnail.
                    postThumbPath.getDownloadUrl().addOnSuccessListener(thumbUri -> {
                        postImagePath.getDownloadUrl().addOnSuccessListener(uri -> {
                            DatabaseReference myRef = database.getReference("Posts");
                            Map<String, String> postMap = new HashMap<>();
                            postMap.put("description", description);
                            postMap.put("thumbnail", thumbUri.toString());
                            postMap.put("userId", userId);
                            postMap.put("timestamp", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())));
                            postMap.put("image_url", uri.toString());
                            //TODO maybe not needed a random ID
                            myRef.child(randomPostId).setValue(postMap).addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful()){
                                    Toast.makeText(NewPostActivity.this, "A new post has been created!", Toast.LENGTH_LONG).show();
                                    Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                    startActivity(mainIntent);
                                    finish();
                                } else {
                                    String error = "Exception at storing postMap into database!";
                                    Toast.makeText(NewPostActivity.this, error, Toast.LENGTH_LONG).show();
                                }
                                newPostProgBar.setVisibility(View.INVISIBLE);
                            });
                        });
                    });
                });
            } else {
                newPostProgBar.setVisibility(View.INVISIBLE);
                String error = task.getException().getMessage();
                Toast.makeText(NewPostActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
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
        storageReference = FirebaseStorage.getInstance().getReference();
    }
}