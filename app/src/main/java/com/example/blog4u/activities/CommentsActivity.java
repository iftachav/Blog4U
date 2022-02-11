package com.example.blog4u.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.blog4u.R;
import com.example.blog4u.adapters.CommentRecyclerAdapter;
import com.example.blog4u.etc.Comment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommentsActivity extends AppCompatActivity {

    private Toolbar commentsToolbar;
    private EditText commentField;
    private ImageView commentPostBtn;
    private String blogPostId;
    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private String userId;

    private RecyclerView commentsListView;
    private List<Comment> commentsList;
    private CommentRecyclerAdapter commentRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        findViews();
        getComments();
        initButton();
    }

    private void getComments() {
        DatabaseReference myref = database.getReference("Comments").child(blogPostId);
        myref.orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Comment comment = makeNewCommentFromSnapshot(snapshot);

                //positioning the newest element first (to be shown first on feed).
                boolean addedToList = false;
                if(commentsList.size() == 0){
                    commentsList.add(0,comment);
                } else {
                    String date = comment.getTimestamp();
                    try {
                        Date dateFromPost = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(date);
                        for (int i = 0; i < commentsList.size(); i++) {
                            Date currentDateFromPost = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(commentsList.get(i).getTimestamp());
                            if (dateFromPost.after(currentDateFromPost)) {
                                commentsList.add(i, comment);
                                addedToList = true;
                                break;
                            }
                        }
                        if(!addedToList)
                            commentsList.add(commentsList.size(),comment);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
//                commentsList.add(0,comment);
//                Log.d("pttt", comment.getMessage());
                commentRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                Comment comment = makeNewCommentFromSnapshot(snapshot);
//                for (int i = 0; i < commentsList.size(); i++) {
//                    if(commentsList.get(i).getCommentId().equals(comment.getCommentId())){
//                        commentsList.set(i,comment);
//                        break;
//                    }
//                }
//                commentRecyclerAdapter.notifyDataSetChanged();
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

    private Comment makeNewCommentFromSnapshot(DataSnapshot snapshot) {
        Map<String,Object> currentMap = (HashMap<String,Object>) snapshot.getValue();
        String message = String.valueOf(currentMap.get("message"));
        String userId = String.valueOf(currentMap.get("userId"));
        String timestamp = String.valueOf(currentMap.get("timestamp"));
        String commentId = String.valueOf(currentMap.get("commentId"));
//        Log.d("pttt", message+userId+timestamp+commentId);
        return new Comment(message, userId, timestamp, commentId);
    }

    private void initButton() {
        commentPostBtn.setOnClickListener(v -> {
            String commentMessage = commentField.getText().toString();
            String commentId = UUID.randomUUID().toString();
            if(!commentMessage.isEmpty()){
                Map<String,String> commentsMap = new HashMap<>();
                commentsMap.put("message", commentMessage);
                commentsMap.put("userId", userId);
                commentsMap.put("commentId", commentId);
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date date = new Date();
                commentsMap.put("timestamp", String.valueOf(formatter.format(date)));
                database.getReference("Comments").child(blogPostId).child(commentId).setValue(commentsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            commentField.setText("");
                            increaseCommentsCount();

                        } else {
                            String error = "Exception at storing commentsMap into database!";
                            Toast.makeText(CommentsActivity.this, error, Toast.LENGTH_LONG).show();
                        }
                    }
                });


                //TODO adding notification
                database.getReference("Users").child(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        String userActedName = task.getResult().child("name").getValue().toString();
                        String userActedImage = task.getResult().child("image").getValue().toString();
                        database.getReference("Posts").child(blogPostId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                Map<String, String> notificationMap = new HashMap<>();
                                String postName = task.getResult().child("description").getValue().toString();
                                if(postName.length()>=30){
                                    postName = postName.substring(0,25);
                                    notificationMap.put("description", userActedName+" commented on your post "+postName+"...");
                                } else {
                                    notificationMap.put("description", userActedName+" commented on your post "+postName);
                                }
                                notificationMap.put("userActedImage", userActedImage);
                                //made date at specific format
                                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                Date date = new Date();
                                notificationMap.put("timestamp", String.valueOf(formatter.format(date)));
                                String blogUserId = task.getResult().child("userId").getValue().toString();
                                database.getReference("Notifications").child(blogUserId).child(UUID.randomUUID().toString()).setValue(notificationMap);
                            }
                        });

                    }
                });
            }
        });
    }

    private void increaseCommentsCount() {
        database.getReference("Posts").child(blogPostId).child("commentsCount").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Log.d("pttt", task.getResult().toString());
                int currentCommentsCount = Integer.parseInt(task.getResult().getValue().toString());
                currentCommentsCount++;
//                                Log.d("pttt", "likes count is: "+currentLikesCount);
                database.getReference("Posts").child(blogPostId).child("commentsCount").setValue(currentCommentsCount);
            }
        });
    }

    private void findViews() {
        commentsToolbar = findViewById(R.id.tb_comments);
        setSupportActionBar(commentsToolbar);
        getSupportActionBar().setTitle("Comments");
        commentPostBtn = findViewById(R.id.imv_comments_post);
        commentField = findViewById(R.id.et_comments_current_comment);
        blogPostId = getIntent().getStringExtra("blogPostId");
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();
        commentsListView = findViewById(R.id.recv_comments_all_comments);
        commentsList = new ArrayList<>();
        commentRecyclerAdapter = new CommentRecyclerAdapter(commentsList);
        commentsListView.setHasFixedSize(true);
        commentsListView.setLayoutManager(new LinearLayoutManager(this));
        commentsListView.setAdapter(commentRecyclerAdapter);
    }
}