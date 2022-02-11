package com.example.blog4u.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.blog4u.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AccountRecyclerAdapter extends RecyclerView.Adapter<AccountRecyclerAdapter.ViewHolder> {

    private List<String> accountList;
    private Context context;
    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;

    public AccountRecyclerAdapter(List<String> accountList){
        this.accountList = accountList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_list_item, parent, false);
        context = parent.getContext();
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = accountList.get(position);
        holder.setImage(imageUrl);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        //if you need three fix imageview in width
        int devicewidth = displaymetrics.widthPixels / 3;
        holder.image.getLayoutParams().width = devicewidth;
        holder.image.getLayoutParams().height = devicewidth;

    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private ImageView image;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            findViews();
        }

        public void setImage(String imageUrl){
            RequestOptions placeHolderOption = new RequestOptions();
            placeHolderOption.placeholder(R.drawable.default_image);
            Glide.with(context).applyDefaultRequestOptions(placeHolderOption).load(imageUrl).into(image);
        }

        private void findViews() {
            image = mView.findViewById(R.id.account_list_item_image);
        }
    }
}
