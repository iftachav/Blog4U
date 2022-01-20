package com.example.blog4u;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private Toolbar setupToolBar;
    private CircleImageView setupImage;
    private EditText setupEditText;
    private Button setupBtn;
    private FirebaseAuth firebaseAuth;
    private ProgressBar setupProgressBar;
//    private FirebaseFirestore firebaseFirestore;
//    private StorageReference storageReference;
    private FirebaseDatabase database ;
    private String userId;
    private Boolean isChanged = false;



    private Uri mainImageUri = null;

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
                } else {//TODO need to check a new image crop. (part 5 min-5:30~)
                    Toast.makeText(SetupActivity.this, "Need to put here image crop", Toast.LENGTH_LONG).show();
//                    ImagePicker.Companion.with(SetupActivity.this)
//                            .cropOval()
//                            .compress(200)
//                            .start();
                }
            } else {
                //TODO do the same as previous else because permission already granted in these versions.
            }
        });

        setupProgressBar.setVisibility(View.VISIBLE);
        setupBtn.setEnabled(false);
        //Retrieving the user's name and picture if exists.
        database.getReference("Users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        Toast.makeText(SetupActivity.this, "Data exists! ", Toast.LENGTH_LONG).show();
                        String name = task.getResult().child("name").getValue().toString();
                        setupEditText.setText(name);


                        //TODO after adding pick image
//                        String image = task.getResult().child("image").getValue().toString();
//                        mainImageUri = Uri.parse(image);


                        //TODO after adding pick image
                        //while glide is loading the image we put the default image at the imageview instead of blank.
//                        RequestOptions placeHolderRequest = new RequestOptions();
//                        placeHolderRequest.placeholder(R.drawable.default_profile);
//                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeHolderRequest).load(image).into(setupImage);


                    } else {
                        Toast.makeText(SetupActivity.this, "Data does not exists! ", Toast.LENGTH_LONG).show();
                    }
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Database Error: " + error, Toast.LENGTH_LONG).show();
                }
                setupProgressBar.setVisibility(View.INVISIBLE);
                setupBtn.setEnabled(true);
            }
        });

        setupBtn.setOnClickListener(v -> {
            String userName = setupEditText.getText().toString();
            setupProgressBar.setVisibility(View.VISIBLE);
            onlyNameStore(userName);




            //TODO after adding pick image
//            if(isChanged) {//TODO need to change to true after we use the image picker
//                if (TextUtils.isEmpty(userName) /*&& mainImageUri != null/*TODO relevant to the image crop and not mandatory*/) {
//                    userId = firebaseAuth.getCurrentUser().getUid();
//
//                    StorageReference imagePath = storageReference.child("profile_images").child(userId + ".jpg");
//                    imagePath.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                            if (task.isSuccessful()) {
//                                storeDatabase(task, userName);
//                            } else {
//                                String error = task.getException().getMessage();
//                                Toast.makeText(SetupActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
//                                setupProgressBar.setVisibility(View.INVISIBLE);
//                            }
//
//                        }
//                    });
//
//                }
//            } else {
//                storeDatabase(null, userName);
//            }
        });
    }

    private void onlyNameStore(String userName) {
        DatabaseReference myRef = database.getReference("Users");

        myRef.child(userId).child("name").setValue(userName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(SetupActivity.this, "The user name has been updated!", Toast.LENGTH_LONG).show();
                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                } else {
                    String error = "Exception at storing name into database!";
                    Toast.makeText(SetupActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
        setupProgressBar.setVisibility(View.INVISIBLE);
    }

//    private void storeDatabase(Task<UploadTask.TaskSnapshot> task, String userName) {
//        String downloadUri;
//        if(task != null) {
////      Uri donloadUri = task.getResult().getMetadata().getReference().getDownloadUrl();
//            downloadUri = task.getResult().getMetadata().getReference().getDownloadUrl().toString();
//            Toast.makeText(SetupActivity.this, "The image is uploaded!", Toast.LENGTH_LONG).show();
//        } else {//TODO need to check with image picker and than maybe this could be Uri.
//            downloadUri = mainImageUri.toString();
//        }
//
//        DatabaseReference myRef = database.getReference("Users");
//
//        if(myRef.child(userId).child("name").setValue(userName).isComplete() && myRef.child(userId).child("image").setValue(downloadUri).isComplete()){
//            Toast.makeText(SetupActivity.this, "The user settings have been updated!", Toast.LENGTH_LONG).show();
//            Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
//            startActivity(mainIntent);
//            finish();
//        } else {
//            String error = "Exception at storing name or image url into database!";
//            Toast.makeText(SetupActivity.this, error, Toast.LENGTH_LONG).show();
//        }
//        setupProgressBar.setVisibility(View.INVISIBLE);
//    }

    private void findViews() {
        setupToolBar = findViewById(R.id.tb_setup);
        setupImage = findViewById(R.id.imv_setup);
        setupBtn = findViewById(R.id.btn_setup);
        setupEditText = findViewById(R.id.et_setup);
        setupProgressBar = findViewById(R.id.pb_setup);
        firebaseAuth = FirebaseAuth.getInstance();
//        firebaseFirestore = FirebaseFirestore.getInstance();
//        storageReference = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();

        userId = firebaseAuth.getCurrentUser().getUid();
    }
}