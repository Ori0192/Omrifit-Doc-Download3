package com.example.omrifit.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.omrifit.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * RecyclerView Adapter for handling friend requests.
 */
public class RvForRequests extends RecyclerView.Adapter<RvForRequests.ViewHolder> {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser user = mAuth.getCurrentUser();
    private final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("user_information");

    private final ArrayList<String> requestsUserUids;
    private final Context mContext;
    private final LayoutInflater mInflater;

    /**
     * Constructor for initializing the adapter with necessary data.
     *
     * @param context  The context of the calling activity.
     * @param requests The list of user UIDs requesting to be friends.
     */
    public RvForRequests(Context context, ArrayList<String> requests) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.requestsUserUids = requests;
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
        View view = mInflater.inflate(R.layout.request_item, parent, false);
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
        String currentUserUid = requestsUserUids.get(position);
        myRef.child(currentUserUid).child("user_profile").child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.getValue(String.class);
                    holder.txtUserAsking.setText("Would you like to add " + name + " (ID: " + currentUserUid + ") to your friends?");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, "Error fetching user data.", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnAccept.setOnClickListener(v -> handleAccept(currentUserUid, holder));
        holder.btnRefuse.setOnClickListener(v -> handleRefuse(position));
    }

    /**
     * Handles the accept button click event.
     *
     * @param currentUserUid The UID of the user to accept.
     * @param holder         The ViewHolder instance.
     */
    private void handleAccept(String currentUserUid, ViewHolder holder) {
        DatabaseReference currentUserRef = myRef.child(user.getUid()).child("friends");
        DatabaseReference friendUserRef = myRef.child(currentUserUid).child("friends");

        ValueEventListener friendUpdateListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> friends = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String friendUid = child.getValue(String.class);
                    if (friendUid != null) {
                        friends.add(friendUid);
                    }
                }

                // Update the friend lists
                updateFriendList(currentUserUid, currentUserRef, friends, user.getUid());
                updateFriendList(user.getUid(), friendUserRef, friends, currentUserUid);

                // Remove the request
                removeRequest(holder.getAdapterPosition());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, "Failed to update friends list.", Toast.LENGTH_SHORT).show();
            }
        };

        currentUserRef.addListenerForSingleValueEvent(friendUpdateListener);
    }

    /**
     * Updates the friend list for the user.
     *
     * @param targetUserId The target user ID.
     * @param reference    The DatabaseReference for updating the friend list.
     * @param friends      The list of friends.
     * @param userIdToAdd  The user ID to add to the friend list.
     */
    private void updateFriendList(String targetUserId, DatabaseReference reference, ArrayList<String> friends, String userIdToAdd) {
        if (!friends.contains(userIdToAdd)) {
            friends.add(userIdToAdd);
            reference.setValue(friends);
        }
    }

    /**
     * Removes the request from the list and the database.
     *
     * @param position The position of the request to remove.
     */
    private void removeRequest(int position) {
        requestsUserUids.remove(position);
        notifyItemRemoved(position);
        myRef.child(user.getUid()).child("requests").child(String.valueOf(position)).removeValue();
    }

    /**
     * Handles the refuse button click event.
     *
     * @param position The position of the request to refuse.
     */
    private void handleRefuse(int position) {
        removeRequest(position);
    }

    /**
     * Returns the size of the data list.
     *
     * @return The size of the data list.
     */
    @Override
    public int getItemCount() {
        return requestsUserUids.size();
    }

    /**
     * ViewHolder class for caching views in the RecyclerView items.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        Button btnAccept, btnRefuse;
        TextView txtUserAsking;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The view of the item.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            btnAccept = itemView.findViewById(R.id.buttonaccept);
            btnRefuse = itemView.findViewById(R.id.buttonrefuse);
            txtUserAsking = itemView.findViewById(R.id.textViewrequestitem);
        }
    }
}
