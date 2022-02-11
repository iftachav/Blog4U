package com.example.blog4u.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.blog4u.R;
import com.example.blog4u.fragments.AccountFragment;
import com.example.blog4u.fragments.HomeFragment;
import com.example.blog4u.fragments.NotificationsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainTB;
    private FirebaseAuth mAuth;
    private FloatingActionButton addPostBtn;
    private FirebaseDatabase database;
    private String currentUserId;
    private BottomNavigationView bottomNavigationView;
    private ProgressBar mainProgBar;
    private HomeFragment homeFragment;
    private AccountFragment accountFragment;
    private NotificationsFragment notificationsFragment;
    private boolean newUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViews();
        initToolBar();
        if(mAuth.getCurrentUser() != null)
            initView();

    }


    private void initView() {
        mainProgBar.bringToFront();
        mainProgBar.setVisibility(View.VISIBLE);
        replaceFragment(homeFragment);
        addPostBtn.setOnClickListener(v -> {
            Intent newPostIntent = new Intent(MainActivity.this, NewPostActivity.class);
            startActivity(newPostIntent);
        });


        initializeFragment();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch(item.getItemId()){
                case R.id.bottom_action_home:
                    replaceFragment(homeFragment);
                    return true;

                case R.id.bottom_action_account:
                    replaceFragment(accountFragment);
                    return true;

                case R.id.bottom_action_notifications:
                    replaceFragment(notificationsFragment);
                    return true;

                default:
                    return false;

            }
        });
    }

    private void initializeFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.frameLayout_main, homeFragment);
        fragmentTransaction.add(R.id.frameLayout_main, notificationsFragment);
        fragmentTransaction.add(R.id.frameLayout_main, accountFragment);

        fragmentTransaction.hide(notificationsFragment);
        fragmentTransaction.hide(accountFragment);
        fragmentTransaction.commit();
        mainProgBar.setVisibility(View.INVISIBLE);
    }

    private void initToolBar() {
        setSupportActionBar(mainTB);
        getSupportActionBar().setTitle("Blog4U");
    }



    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            sendToActivity(SignInActivity.class, true);
            finish();
        } else {
            currentUserId = mAuth.getCurrentUser().getUid();
            database.getReference("Users").child(currentUserId).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    if(!task.getResult().exists()){
                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                        setupIntent.putExtra("new",true);
                        startActivity(setupIntent);
                        finish();
                    }
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                }

            });


        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.action_logout_btn:
                logOut();
                return true;
            case R.id.action_settings_btn:
                sendToActivity(SetupActivity.class, newUser);
                return true;


            default:
                return false;
        }
    }

    private void logOut() {
        mAuth.signOut();
        sendToActivity(SignInActivity.class, newUser);
        finish();
    }

    private void sendToActivity(Class <?> destination, boolean newUser) {
        Intent mainIntent = new Intent(MainActivity.this, destination);
        mainIntent.putExtra("new",newUser);
        startActivity(mainIntent);
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(fragment == homeFragment){
            fragmentTransaction.hide(accountFragment);
            fragmentTransaction.hide(notificationsFragment);
        } else if(fragment == accountFragment) {
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(notificationsFragment);
        } else if(fragment == notificationsFragment) {
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(accountFragment);
        }
        fragmentTransaction.show(fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Blog4U!")
                .setMessage("Are you sure you want to close this app?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    finish();
                    System.exit(0);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void findViews() {
        mainTB = findViewById(R.id.tb_main);
        mAuth = FirebaseAuth.getInstance();
        addPostBtn = findViewById(R.id.fb_main);
        database = FirebaseDatabase.getInstance();
        bottomNavigationView = findViewById(R.id.bnv_main);
        mainProgBar = findViewById(R.id.pb_main);
        homeFragment = new HomeFragment();
        notificationsFragment = new NotificationsFragment();
        accountFragment = new AccountFragment();
    }
}