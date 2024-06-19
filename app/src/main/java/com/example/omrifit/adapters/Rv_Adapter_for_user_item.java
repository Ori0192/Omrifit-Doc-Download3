package com.example.omrifit.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.omrifit.R;
import com.example.omrifit.classes.ProfileInfo;
import com.example.omrifit.classes.Utility;
import com.example.omrifit.friends_profile.UserExpand;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * RecyclerView Adapter for displaying a list of user profile items.
 */
public class Rv_Adapter_for_user_item extends RecyclerView.Adapter<Rv_Adapter_for_user_item.ViewHolder> {

    private final LayoutInflater mInflater;
    private final ArrayList<ProfileInfo> profileInfos;
    private final Context context;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser user = mAuth.getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final int view;
    private final DatabaseReference chatRef = database.getReference("chats");

    /**
     * Constructor for initializing the adapter with necessary data.
     *
     * @param view    The layout resource ID for each item.
     * @param context The context of the activity.
     * @param data    The list of profile information.
     */
    public Rv_Adapter_for_user_item(int view, Context context, ArrayList<ProfileInfo> data) {
        this.context = context;
        this.profileInfos = data;
        this.mInflater = LayoutInflater.from(context);
        this.view = view;
    }

    /**
     * Creates and inflates the view holder.
     *
     * @param parent   The parent view group.
     * @param viewType The view type.
     * @return A new ViewHolder instance.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(this.view, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the data to the view holder.
     *
     * @param holder   The ViewHolder instance.
     * @param position The position of the current item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProfileInfo profileInfo = profileInfos.get(position);
        holder.txt_name.setText(profileInfo.getName());
        Utility.setImageUserPhoto(profileInfo.getId(), holder.img_user_item);

        DatabaseReference userChatRef = chatRef.child(Utility.createChatReference(user.getUid(), profileInfo.getId()));
        userChatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unreadCount = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Boolean isUnread = dataSnapshot.child("unread").getValue(Boolean.class);
                    String sentById = dataSnapshot.child("sent_by").getValue(String.class);
                    if (Boolean.TRUE.equals(isUnread) && profileInfo.getId().equals(sentById)) {
                        unreadCount++;
                    }
                }
                holder.txt_unread_messages.setText(unreadCount + " unread messages");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log error or show error message
            }
        });

        holder.itemView.setOnClickListener(v -> onClick(v, position));
    }

    /**
     * Handles the click event for each item in the RecyclerView.
     *
     * @param v        The view that was clicked.
     * @param position The position of the clicked item.
     */
    private void onClick(View v, int position) {
        Intent intent = new Intent(context, UserExpand.class);
        Gson gson = new Gson();
        String json = gson.toJson(profileInfos.get(position));
        intent.putExtra("profile", json);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            context.startActivity(intent, new Bundle());
        }
    }

    /**
     * Returns the size of the data list.
     *
     * @return The size of the data list.
     */
    @Override
    public int getItemCount() {
        return profileInfos.size();
    }

    /**
     * ViewHolder class for caching views in the RecyclerView items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt_name, txt_unread_messages;
        CircleImageView img_user_item, img_user_rank;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The view of the item.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_name = itemView.findViewById(R.id.txt_name_user_item);
            img_user_item = itemView.findViewById(R.id.img_user_epand);
            img_user_rank = itemView.findViewById(R.id.img_user_epand_rank);
            txt_unread_messages = itemView.findViewById(R.id.textView22);
        }
    }
}
