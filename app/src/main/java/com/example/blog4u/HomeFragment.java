package com.example.blog4u;

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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
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

    private void initBlogs() {
        //TODO maybe need to put if user is logged in here.
        DatabaseReference myref = database.getReference("Posts");

//        Query firstQuery = myref.orderByChild("timestamp").limitToLast(1);

        myref.orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                BlogPost blogPost = makeNewBlogFromSnapshot(snapshot);


                //TODO need to change to descending order. part14 12:30~
                //positioning the newest element first (to be shown first on feed).
                blogList.add(0,blogPost);
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

    private BlogPost makeNewBlogFromSnapshot(DataSnapshot snapshot) {
        Map<String,Object> currentMap = (HashMap<String,Object>) snapshot.getValue();
        String description = String.valueOf(currentMap.get("description"));
        String thumbnail = String.valueOf(currentMap.get("thumbnail"));
        String image_url = String.valueOf(currentMap.get("image_url"));
        String userId = String.valueOf(currentMap.get("userId"));
        String timestamp = String.valueOf(currentMap.get("timestamp"));
        String blogId = String.valueOf(snapshot.getKey());
        String likesCount = String.valueOf(currentMap.get("likesCount"));
        return new BlogPost(userId,image_url,description,thumbnail,timestamp, likesCount, blogId);
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