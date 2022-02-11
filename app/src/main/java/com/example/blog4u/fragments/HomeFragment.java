package com.example.blog4u.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.blog4u.etc.BlogPost;
import com.example.blog4u.R;
import com.example.blog4u.adapters.BlogRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {


    private RecyclerView blogListRecycleView;
    private List<BlogPost> blogList;
    private FirebaseDatabase database;
    private BlogRecyclerAdapter blogRecyclerAdapter;



    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        findViews(view);
        initBlogs();
        return view;
    }

    public void initBlogs() {
        database.getReference("Posts").orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                BlogPost blogPost = makeNewBlogFromSnapshot(snapshot);

                //positioning the newest element first (to be shown first on feed).
                boolean addedToList = false;
                if(blogList.size() == 0){
                    blogList.add(0,blogPost);
                } else {
                    String date = blogPost.getTimestamp();
                    try {
                        Date dateFromPost = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(date);
                        for (int i = 0; i < blogList.size(); i++) {
                            Date currentDateFromPost = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(blogList.get(i).getTimestamp());
                            if (dateFromPost.after(currentDateFromPost)) {
                                blogList.add(i, blogPost);
                                addedToList = true;
                                break;
                            }
                        }
                        if(!addedToList)
                            blogList.add(blogList.size(),blogPost);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                blogRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                BlogPost blogPost = makeNewBlogFromSnapshot(snapshot);
                for (int i = 0; i < blogList.size(); i++) {
                    if(blogList.get(i).getBlogId().equals(blogPost.getBlogId())){
                        blogList.set(i,blogPost);
                        break;
                    }
                }
                blogRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d("pttt", "itemRemoved");
                BlogPost blogPost = makeNewBlogFromSnapshot(snapshot);
                String currentUserId = blogPost.getUserId();
                decreasePostsCount(currentUserId);
                for (int i = 0; i < blogList.size(); i++) {
                    if(blogList.get(i).getBlogId().equals(blogPost.getBlogId())){
                        blogList.remove(i);
                        break;
                    }
                }
                blogRecyclerAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("pttt", "canceled download posts");

            }
        });
    }

    private void decreasePostsCount(String userId) {
        database.getReference("Users").child(userId).child("postsCount").get().addOnCompleteListener(task -> {
            int currentPostsCount = Integer.parseInt(task.getResult().getValue().toString());
            currentPostsCount-=1;
            if(currentPostsCount<0)
                currentPostsCount=0;
            database.getReference("Users").child(userId).child("postsCount").setValue(currentPostsCount);
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

    private void findViews(View view) {
        blogListRecycleView = view.findViewById(R.id.blog_posts_listview_home);
        blogList = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blogList);
        blogListRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        blogListRecycleView.setAdapter(blogRecyclerAdapter);

    }
}