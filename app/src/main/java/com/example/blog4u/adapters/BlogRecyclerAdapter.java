package com.example.blog4u.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.blog4u.etc.BlogPost;
import com.example.blog4u.activities.CommentsActivity;
import com.example.blog4u.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    private List<BlogPost> blogList;
    private Context context;
    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;



    public BlogRecyclerAdapter(List<BlogPost> blogList){
        this.blogList = blogList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);
        context = parent.getContext();
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();
        String descData = blogList.get(position).getDescription();
        holder.setDescText(descData);
        String imageUrl = blogList.get(position).getImage_url();
        String thumbUrl = blogList.get(position).getThumbnail();
        holder.setBlogImage(imageUrl, thumbUrl);
        String timeStamp = blogList.get(position).getTimestamp();
        holder.setBlogTimeStamp(timeStamp);
        String blogUserId = blogList.get(position).getUserId();
        String blogId = blogList.get(position).getBlogId();


        String commentsCount = blogList.get(position).getCommentsCount();
        holder.setCommentsCount(commentsCount);

        String likesCount = blogList.get(position).getLikesCount();
        holder.setLikesCount(likesCount, false);
        //checking if user already liking this post and changing the heart icon color.
        if(!(Integer.parseInt(likesCount) == 0)){
            database.getReference("Likes").child(blogId).child(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.getResult().exists())
                        if(!task.getResult().getValue().toString().equals("null"))
                            holder.setLikesCount(likesCount, true);
                }
            });
        }

        //check if the current user is the post owner and than enabling the delete button.
        if(blogUserId.equals(currentUserId)){
//            blogList.get(position).
            holder.blogDeleteBtn.setEnabled(true);
            holder.blogDeleteBtn.setVisibility(View.VISIBLE);
            //init delete button on relevant posts
            initDeleteButton(holder, position, blogId);
        }

        //need to get user Data from the Id.
        setBlogUserData(holder, blogUserId);
        //init likes
        initLikesIcon(holder, position, currentUserId, blogUserId, blogId);
        //init comments
        initCommentsIcon(holder, position);
    }

    private void initDeleteButton(ViewHolder holder, int position, String blogId) {
        holder.blogDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.getReference("Posts").child(blogId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //TODO known bug- when creating 2 posts and deleting the top one, the delete button doesnt follow.
//                            blogList.remove(position);
                            holder.blogDeleteBtn.setVisibility(View.INVISIBLE);
                        }
                    }
                });
                database.getReference("Likes").child(blogId).removeValue();
                database.getReference("Comments").child(blogId).removeValue();
                //TODO need to refresh the recycle view.
//                notifyItemRemoved(position);
//                holder.notify();
//                notifyDataSetChanged();
            }
        });
    }

    private void setBlogUserData(ViewHolder holder, String blogUserId) {
        database.getReference("Users").child(blogUserId).get().addOnCompleteListener(task -> {
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

    private void initCommentsIcon(ViewHolder holder, int position) {
        holder.blogCommentsImageView.setOnClickListener(v -> {
            Intent commentsIntent = new Intent(context, CommentsActivity.class);
            commentsIntent.putExtra("blogPostId", blogList.get(position).getBlogId());
            context.startActivity(commentsIntent);
        });
    }

    private void initLikesIcon(ViewHolder holder, int position, String currentUserId, String blogUserId, String blogId) {
        //Likes Imageview
        holder.blogLikeImageView.setOnClickListener(v -> {
            Map<String, String> likesMap = new HashMap<>();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date date = new Date();
            likesMap.put("timestamp", formatter.format(date));
            database.getReference("Likes").child(blogList.get(position).getBlogId()).child(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(!task.getResult().exists()){
                        changeLikesCount(position, 1);
                        database.getReference("Likes").child(blogList.get(position).getBlogId()).child(currentUserId).setValue(likesMap);
                    }
                    else{
                        if(task.getResult().getValue().toString().equals("null")){
                            database.getReference("Likes").child(blogList.get(position).getBlogId()).child(currentUserId).setValue(likesMap);
                            changeLikesCount(position, 1);
                        } else {
                            //TODO maybe need to use remove value
                            database.getReference("Likes").child(blogList.get(position).getBlogId()).child(currentUserId).setValue("null");

                            changeLikesCount(position, -1);
                            holder.changeLikeIconColor(false);
                        }
                    }
                    Log.d("pttt", "task result "+ task.getResult().getValue());
                }
            });

            //TODO adding notification
            database.getReference("Users").child(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    String userActedName = task.getResult().child("name").getValue().toString();
                    String userActedImage = task.getResult().child("image").getValue().toString();
                    database.getReference("Posts").child(blogId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            Map<String, String> notificationMap = new HashMap<>();
                            String postName = task.getResult().child("description").getValue().toString();
                            if(postName.length()>=30){
                                postName = postName.substring(0,25);
                                notificationMap.put("description", userActedName+" liked your post "+postName+"...");
                            } else {
                                notificationMap.put("description", userActedName+" liked your post "+postName);
                            }
                            notificationMap.put("userActedImage", userActedImage);
                            //made date at specific format
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            Date date = new Date();
                            notificationMap.put("timestamp", String.valueOf(formatter.format(date)));

                            //TODO maybe need onCompleteListener.
                            database.getReference("Notifications").child(blogUserId).child(currentUserId).setValue(notificationMap);
                        }
                    });
                }
            });
        });
    }

    private void changeLikesCount(int position, int i) {
        database.getReference("Posts").child(blogList.get(position).getBlogId()).child("likesCount").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
//                                Log.d("pttt", "likes count is: "+task.getResult().getValue());
                int currentLikesCount = Integer.parseInt(task.getResult().getValue().toString());
                currentLikesCount+=i;
//                                Log.d("pttt", "likes count is: "+currentLikesCount);
                database.getReference("Posts").child(blogList.get(position).getBlogId()).child("likesCount").setValue(currentLikesCount);
            }
        });
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView descView;
        private TextView blogTimeStamp;
        private TextView blogUserName;
        private ImageView blogImageView ;
        private CircleImageView blogUserImage;
        private ImageView blogLikeImageView;
        private TextView blogLikeCount;
        private TextView blogCommentCount;
        private ImageView blogCommentsImageView;
//        private TextView blogCommentsCount;
        private Button blogDeleteBtn;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            findViews();
        }


        public void setDescText(String descText){
            descView.setText(descText);
        }

        public void setBlogImage(String downloadUri, String thumbUrl){
            RequestOptions placeHolderOption = new RequestOptions();
            placeHolderOption.placeholder(R.drawable.default_image);
            Glide.with(context).applyDefaultRequestOptions(placeHolderOption).load(downloadUri).thumbnail(Glide.with(context).load(thumbUrl)).into(blogImageView);
        }

        public void setBlogTimeStamp(String timeStamp){
            blogTimeStamp.setText(timeStamp);
        }

        public void setUserData(String name, String image){
            blogUserName.setText(name);
            RequestOptions placeHolderOption = new RequestOptions();
            placeHolderOption.placeholder(R.drawable.default_profile);
            Glide.with(context).applyDefaultRequestOptions(placeHolderOption).load(image).into(blogUserImage);
        }

        public void setLikesCount(String likesCount, Boolean alreadyLiked) {
            blogLikeCount.setText(likesCount+ " Likes");
            changeLikeIconColor(alreadyLiked);
        }

        public void setCommentsCount(String commentsCount) {
            blogCommentCount.setText(commentsCount+ " Comments");
        }

        public void changeLikeIconColor(Boolean color){
            if(color)
                blogLikeImageView.setImageResource(R.mipmap.action_like_accent);
            else
                blogLikeImageView.setImageResource(R.mipmap.action_like_gray);

        }

        private void findViews() {
            blogCommentsImageView = mView.findViewById(R.id.imv_blog_list_comments);
//            blogCommentsCount = mView.findViewById(R.id.tv_blog_list_comments_count);
            blogDeleteBtn = mView.findViewById(R.id.btn_blog_list_delete);
            descView = mView.findViewById(R.id.tv_blog_list_description);
            blogImageView = mView.findViewById(R.id.imv_blog_list_image);
            blogTimeStamp = mView.findViewById(R.id.tv_blog_list_date);
            blogUserName = mView.findViewById(R.id.tv_blog_list_user_name);
            blogUserImage = mView.findViewById(R.id.circle_imv_blog_list_user_image);
            blogLikeImageView = mView.findViewById(R.id.imv_blog_list_like);
            blogLikeCount = mView.findViewById(R.id.tv_blog_list_like_count);
            blogCommentCount = mView.findViewById(R.id.tv_blog_list_comments_count);
        }


    }

}
