package com.example.blog4u.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

//import com.github.drjacky.imagepicker.ImagePicker;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.blog4u.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private Toolbar setupToolBar;
    private CircleImageView setupImage;
    private EditText setupEditText;
    private Button setupBtn;
    private FirebaseAuth firebaseAuth;
    private ProgressBar setupProgressBar;
    private StorageReference storageReference;
    private FirebaseDatabase database ;
    private String userId;
    private Boolean isChanged = false;
    private Uri mainImageUri = null;
    private boolean newUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        findViews();
        initView();
    }

    private void initView() {
        setSupportActionBar(setupToolBar);
        getSupportActionBar().setTitle("Account Setup");

        setupImage.setOnClickListener(v -> {
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(SetupActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE} , 1);
                } else {
                    bringImagePicker();
                }
            } else {
                bringImagePicker();
            }
        });

        setupProgressBar.setVisibility(View.VISIBLE);
        setupBtn.setEnabled(false);
        //Retrieving the user's name and picture if exists.
        getUserData();


        setupBtn.setOnClickListener(v -> {
            String userName = setupEditText.getText().toString();
            if (!TextUtils.isEmpty(userName) && mainImageUri != null) {
                setupProgressBar.setVisibility(View.VISIBLE);
                if(isChanged) {
                    userId = firebaseAuth.getCurrentUser().getUid();
                    setupProgressBar.setVisibility(View.VISIBLE);
                    StorageReference imagePath = storageReference.child("profile_images").child(userId + ".jpg");
                    imagePath.putFile(mainImageUri).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            imagePath.getDownloadUrl().addOnSuccessListener(uri -> storeToDatabase(task, userName, uri));
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(SetupActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                            setupProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                } else {
                    storeToDatabase(null, userName, mainImageUri);
                }
            }
        });
    }

    private void getUserData() {
        database.getReference("Users").child(userId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(task.getResult().exists()){
                    Toast.makeText(SetupActivity.this, "Data exists! ", Toast.LENGTH_LONG).show();
                    String name = task.getResult().child("name").getValue().toString();
                    setupEditText.setText(name);
                    String image = task.getResult().child("image").getValue().toString();

                    //while glide is loading the image we put the default image at the imageview instead of blank.
                    RequestOptions placeHolderRequest = new RequestOptions();
                    placeHolderRequest.placeholder(R.drawable.default_profile);
                    Glide.with(SetupActivity.this).setDefaultRequestOptions(placeHolderRequest).load(image).into(setupImage);
                } else {
                    Toast.makeText(SetupActivity.this, "User data does not exists! ", Toast.LENGTH_LONG).show();
                }
            } else {
                String error = task.getException().getMessage();
                Toast.makeText(SetupActivity.this, "Database Error: " + error, Toast.LENGTH_LONG).show();
            }
            setupProgressBar.setVisibility(View.INVISIBLE);
            setupBtn.setEnabled(true);
        });
    }

    private void bringImagePicker() {
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(SetupActivity.this);
    }


    private void storeToDatabase(Task<UploadTask.TaskSnapshot> task, String userName, Uri downloadUri) {
        DatabaseReference myRef = database.getReference("Users");
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", userName);
        userMap.put("image", downloadUri.toString());
        if(newUser) {
            userMap.put("postsCount", "0");
            storeMap(myRef, userMap);
        } else{
            myRef.child(userId).child("postsCount").get().addOnCompleteListener(task12 -> {
                String currentCountStr = task12.getResult().getValue().toString();
                userMap.put("postsCount", currentCountStr);
                storeMap(myRef, userMap);
            });
        }
    }

    private void storeMap(DatabaseReference myRef, Map<String, String> userMap){
        myRef.child(userId).setValue(userMap).addOnCompleteListener(task1 -> {
            if(task1.isSuccessful()){
                Toast.makeText(SetupActivity.this, "The user settings have been updated!", Toast.LENGTH_LONG).show();
                Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            } else {
                String error = "Exception at storing name or image url into database!";
                Toast.makeText(SetupActivity.this, error, Toast.LENGTH_LONG).show();
            }
            setupProgressBar.setVisibility(View.INVISIBLE);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();
                setupImage.setImageURI(mainImageUri);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(SetupActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void findViews() {
        setupToolBar = findViewById(R.id.tb_setup);
        setupImage = findViewById(R.id.imv_setup);
        setupBtn = findViewById(R.id.btn_setup);
        setupEditText = findViewById(R.id.et_setup);
        setupProgressBar = findViewById(R.id.pb_setup);
        newUser = getIntent().getBooleanExtra("new", false);
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();
    }
}