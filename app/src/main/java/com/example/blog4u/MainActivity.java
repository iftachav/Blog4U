package com.example.blog4u;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainTB;
    private FirebaseAuth mAuth;
    private FloatingActionButton addPostBtn;
    private FirebaseDatabase database;
    private String currentUserId;
    private BottomNavigationView bottomNavigationView;

    private HomeFragment homeFragment;
    private AccountFragment accountFragment;
    private NotificationsFragment notificationsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViews();
        initToolBar();
        initView();

    }


    private void initView() {
        //TODO maybe need to check if currentuser is not null from mAuth.
        replaceFragment(homeFragment);
        addPostBtn.setOnClickListener(v -> {
            Intent newPostIntent = new Intent(MainActivity.this, NewPostActivity.class);
            startActivity(newPostIntent);
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
            }
        });
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
            sendToActivity(SignInActivity.class);
        } else {
            currentUserId = mAuth.getCurrentUser().getUid();
            database.getReference("Users").child(currentUserId).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    if(!task.getResult().exists()){
                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
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
                sendToActivity(SetupActivity.class);
                return true;


            default:
                return false;
        }
    }

    private void logOut() {
        mAuth.signOut();
        sendToActivity(SignInActivity.class);
    }

    private void sendToActivity(Class <?> destination) {
        Intent mainIntent = new Intent(MainActivity.this, destination);
        startActivity(mainIntent);
        //TODO maybe need to not finish (currently the app stuck at account settings if user loged in).
        finish();
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout_main, fragment);
        fragmentTransaction.commit();
    }

    private void findViews() {
        mainTB = findViewById(R.id.tb_main);
        mAuth = FirebaseAuth.getInstance();
        addPostBtn = findViewById(R.id.fb_main);
        database = FirebaseDatabase.getInstance();
        bottomNavigationView = findViewById(R.id.bnv_main);
        homeFragment = new HomeFragment();
        notificationsFragment = new NotificationsFragment();
        accountFragment = new AccountFragment();
    }
}