package com.example.blog4u;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
        String descData = blogList.get(position).getDescription();
        holder.setDescText(descData);
        String imageUrl = blogList.get(position).getImage_url();
        String thumbUrl = blogList.get(position).getThumbnail();
        holder.setBlogImage(imageUrl, thumbUrl);
        String timeStamp = blogList.get(position).getTimestamp();
        holder.setBlogTimeStamp(timeStamp);
        Log.d("pttt", "blog: "+blogList.get(position));

        String likesCount = blogList.get(position).getLikesCount();
        holder.setLikesCount(likesCount, false);
        //checking if user already liking this post and changing the heart icon color.
        if(!(Integer.parseInt(likesCount) == 0)){
            database.getReference("Likes").child(blogList.get(position).getBlogId()).child(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.getResult().exists())
                        if(!task.getResult().getValue().toString().equals("null"))
                            holder.setLikesCount(likesCount, true);
                }
            });
        }

        String userId = blogList.get(position).getUserId();
        //need to get user Data from the Id.
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

        //Likes Imageview
        holder.blogLikeImageView.setOnClickListener(v -> {
            Map<String, String> likesMap = new HashMap<>();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date date = new Date();
            likesMap.put("timestamp", String.valueOf(formatter.format(date)));
            database.getReference("Likes").child(blogList.get(position).getBlogId()).child(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(!task.getResult().exists()){
                        changeLikesCount(position, 1);
                        database.getReference("Likes").child(blogList.get(position).getBlogId()).child(firebaseAuth.getCurrentUser().getUid()).setValue(likesMap);
                    }
                    else{
                        if(task.getResult().getValue().toString().equals("null")){
                            database.getReference("Likes").child(blogList.get(position).getBlogId()).child(firebaseAuth.getCurrentUser().getUid()).setValue(likesMap);
                            changeLikesCount(position, 1);
                        } else {
                            database.getReference("Likes").child(blogList.get(position).getBlogId()).child(firebaseAuth.getCurrentUser().getUid()).setValue("null");

                            changeLikesCount(position, -1);
                            holder.changeLikeIconColor(false);
                        }
                    }
                    Log.d("pttt", "task result "+ task.getResult().getValue());

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


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDescText(String descText){
            descView = mView.findViewById(R.id.tv_blog_list_description);
            descView.setText(descText);
        }

        public void setBlogImage(String downloadUri, String thumbUrl){
            blogImageView = mView.findViewById(R.id.imv_blog_list_image);
            RequestOptions placeHolderOption = new RequestOptions();
            placeHolderOption.placeholder(R.drawable.default_image);
            Glide.with(context).applyDefaultRequestOptions(placeHolderOption).load(downloadUri).thumbnail(Glide.with(context).load(thumbUrl)).into(blogImageView);
        }

        public void setBlogTimeStamp(String timeStamp){
            blogTimeStamp = mView.findViewById(R.id.tv_blog_list_date);
            blogTimeStamp.setText(timeStamp);
        }

        public void setUserData(String name, String image){
            blogUserName = mView.findViewById(R.id.tv_blog_list_user_name);
            blogUserImage = mView.findViewById(R.id.circle_imv_blog_list_user_image);
            blogUserName.setText(name);
            RequestOptions placeHolderOption = new RequestOptions();
            placeHolderOption.placeholder(R.drawable.default_profile);
            Glide.with(context).applyDefaultRequestOptions(placeHolderOption).load(image).into(blogUserImage);
        }

        public void setLikesCount(String likesCount, Boolean alreadyLiked) {
            blogLikeImageView = mView.findViewById(R.id.imv_blog_list_like);
            blogLikeCount = mView.findViewById(R.id.tv_blog_list_like_count);
            blogLikeCount.setText(likesCount+ " Likes");
            changeLikeIconColor(alreadyLiked);
        }

        public void changeLikeIconColor(Boolean color){
            if(color)
                blogLikeImageView.setImageResource(R.mipmap.action_like_accent);
            else
                blogLikeImageView.setImageResource(R.mipmap.action_like_gray);

        }
    }

}
