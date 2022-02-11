package com.example.blog4u.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.blog4u.etc.BlogPost;
import com.example.blog4u.R;
import com.example.blog4u.adapters.AccountRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class AccountFragment extends Fragment {

    private RecyclerView accountListRecycleView;
    private List<String> accountList;
    private FirebaseDatabase database;
    private AccountRecyclerAdapter accountRecyclerAdapter;
    private FirebaseAuth firebaseAuth;
    private CircleImageView userImage;
    private TextView userName;
    private TextView postsCount;
    private Context context;


    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        findViews(view);
        initImages();
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = getActivity().getApplicationContext();
    }

    private void initImages() {
        database.getReference("Posts").orderByChild("timestamp").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                BlogPost blogPost = makeNewBlogFromSnapshot(snapshot);
                if(blogPost.getUserId().equals(firebaseAuth.getCurrentUser().getUid())){
                    accountList.add(blogPost.getImage_url());
                    refreshPostsCount(blogPost.getUserId());
                    accountRecyclerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                BlogPost blogPost = makeNewBlogFromSnapshot(snapshot);
//                refreshPostsCount(blogPost.getUserId());
//                accountRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//                Log.d("pttt", "itemRemoved");
                BlogPost blogPost = makeNewBlogFromSnapshot(snapshot);
                for (int i = 0; i < accountList.size(); i++) {
                    if(accountList.get(i).equals(blogPost.getImage_url())){
                        accountList.remove(i);
                        break;
                    }
                }
                accountRecyclerAdapter.notifyDataSetChanged();
                refreshPostsCount(blogPost.getUserId());


//                Fragment currentFragment = getActivity().getFragmentManager().findFragmentById(R.id.AccountFragment.);
//
//                if (currentFragment instanceof AccountFragment) {
//                    FragmentTransaction fragTransaction =   (getActivity()).getFragmentManager().beginTransaction();
//                    fragTransaction.detach(currentFragment);
//                    fragTransaction.attach(currentFragment);
//                    fragTransaction.commit();
//                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void refreshPostsCount(String uid) {
        database.getReference("Users").child(uid).child("postsCount").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String currentPostsCount = task.getResult().getValue().toString();
                postsCount.setText(currentPostsCount);
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        postsCount.setText(currentPostsCount);
//                    }
//                });
            }
        });
    }

    private BlogPost makeNewBlogFromSnapshot(DataSnapshot snapshot) {
        Map<String,Object> currentMap = (HashMap<String,Object>) snapshot.getValue();
        String description = String.valueOf(currentMap.get("description"));
        String thumbnail = String.valueOf(currentMap.get("thumbnail"));
        String image_url = String.valueOf(currentMap.get("image_url"));
        String userId = String.valueOf(currentMap.get("userId"));
        String timestamp = String.valueOf(currentMap.get("timestamp"));
        String blogId = String.valueOf(snapshot.getKey());
        String likesCount = String.valueOf(currentMap.get("likesCount"));
        String commentsCount = String.valueOf(currentMap.get("commentsCount"));
        return new BlogPost(userId,image_url,description,thumbnail,timestamp, likesCount, blogId, commentsCount);
    }


    private void setUserData(String uid) {
        database.getReference("Users").child(uid).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                String name =task.getResult().child("name").getValue().toString();
                String currentPostsCount =task.getResult().child("postsCount").getValue().toString();
                Log.d("pttt", name);
                userName.setText(name);
                postsCount.setText(currentPostsCount);
                Log.d("pttt", currentPostsCount);

                String userImageUrl = task.getResult().child("image").getValue().toString();
                RequestOptions placeHolderOption = new RequestOptions();
                placeHolderOption.placeholder(R.drawable.default_profile);
//                Log.d("pttt", userImageUrl+" "+userImage);
                Glide.with(this).applyDefaultRequestOptions(placeHolderOption).load(userImageUrl).into(userImage);
            }
        });
    }


    private void findViews(View view) {
        accountListRecycleView = view.findViewById(R.id.account_list_view);
        accountList = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        accountRecyclerAdapter = new AccountRecyclerAdapter(accountList);
        accountListRecycleView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        accountListRecycleView.setAdapter(accountRecyclerAdapter);
        firebaseAuth = FirebaseAuth.getInstance();
        userImage = view.findViewById(R.id.circle_imv_account);
        userName = view.findViewById(R.id.tv_account_user_name);
        postsCount = view.findViewById(R.id.tv_account_posts_count);
        setUserData(firebaseAuth.getCurrentUser().getUid());

    }


}