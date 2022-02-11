package com.example.blog4u.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.blog4u.etc.Notification;
import com.example.blog4u.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationRecyclerAdapter extends RecyclerView.Adapter<NotificationRecyclerAdapter.ViewHolder> {

    private List<Notification> notificationList;
    private Context context;
    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;

    public NotificationRecyclerAdapter(List<Notification> notificationList) {
        this.notificationList=notificationList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_list_item, parent, false);
        context = parent.getContext();
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String description = notificationList.get(position).getDescription();
        holder.setDescription(description);
//        String timestamp = notificationList.get(position).getTimestamp();
        String userActedImage = notificationList.get(position).getUserActedImage();
        holder.setUserImage(userActedImage);

    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView notificationDescription;
        private CircleImageView notificationUserImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            findViews();
        }

        public void setDescription(String description) {
            notificationDescription.setText(description);
        }

        public void setUserImage(String userActedImage) {
            RequestOptions placeHolderOption = new RequestOptions();
            placeHolderOption.placeholder(R.drawable.default_profile);
            Glide.with(context).applyDefaultRequestOptions(placeHolderOption).load(userActedImage).into(notificationUserImage);
        }

        private void findViews() {
            notificationDescription = mView.findViewById(R.id.tv_notification_list_description);
            notificationUserImage = mView.findViewById(R.id.circle_imv_notification_list_user_image);
        }
    }
}
