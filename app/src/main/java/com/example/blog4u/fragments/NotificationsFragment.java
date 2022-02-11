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

import com.example.blog4u.etc.Notification;
import com.example.blog4u.R;
import com.example.blog4u.adapters.NotificationRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsFragment extends Fragment {

    private RecyclerView notificationListRecycleView;
    private List<Notification> notificationList;
    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private NotificationRecyclerAdapter notificationRecyclerAdapter;

    public NotificationsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        findViews(view);
        initNotifications();
        return view;
    }

    private void initNotifications() {
        database.getReference("Notifications").child(firebaseAuth.getCurrentUser().getUid()).orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("pttt", ""+snapshot);
                Notification notification= makeNewNotificationFromSnapshot(snapshot);
                notificationList.add(0,notification);
                notificationRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private Notification makeNewNotificationFromSnapshot(DataSnapshot snapshot) {
        Map<String,Object> currentMap = (HashMap<String,Object>) snapshot.getValue();
        String description = String.valueOf(currentMap.get("description"));
        String timestamp = String.valueOf(currentMap.get("timestamp"));
        String userActedImage = String.valueOf(currentMap.get("userActedImage"));
        return new Notification(userActedImage, description, timestamp);
    }

    private void findViews(View view) {
        notificationListRecycleView = view.findViewById(R.id.blog_notifications_listview);
        notificationList = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        notificationRecyclerAdapter = new NotificationRecyclerAdapter(notificationList);
        notificationListRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        notificationListRecycleView.setAdapter(notificationRecyclerAdapter);
        firebaseAuth = FirebaseAuth.getInstance();
    }
}