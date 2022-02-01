package com.example.blog4u;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentRecyclerAdapter.ViewHolder> {

    public List<Comment> commentsList;
    public Context context;
    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;


    public CommentRecyclerAdapter(List<Comment> commentsList){
        this.commentsList = commentsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.comment_list_item, parent, false);
        return new CommentRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String commentMessage = commentsList.get(position).getMessage();
        holder.setCommentMessage(commentMessage);
        String userId = commentsList.get(position).getUserId();
        database.getReference("Users").child(userId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                String userName = task.getResult().child("name").getValue().toString();
                String userImage = task.getResult().child("image").getValue().toString();
                holder.setUserData(userName, userImage);
            } else {
                String error = task.getException().getMessage();
                Log.d("pttt", "Error: "+error);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(commentsList != null){
            return commentsList.size();
        } else {
            return 0;
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView commentMessage;
        private CircleImageView commentUserImage;
        private TextView commentUserName;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setCommentMessage(String message){
            commentMessage = mView.findViewById(R.id.tv_comment_list_message);
            commentMessage.setText(message);
        }

        public void setUserData(String name, String image){
            commentUserName = mView.findViewById(R.id.tv_comment_list_user_name);
            commentUserImage = mView.findViewById(R.id.circle_imv_comment_list_user_image);
            commentUserName.setText(name);
            RequestOptions placeHolderOption = new RequestOptions();
            placeHolderOption.placeholder(R.drawable.default_profile);
            Glide.with(context).applyDefaultRequestOptions(placeHolderOption).load(image).into(commentUserImage);
        }
    }

}
