package com.example.omrifit.adapters;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.omrifit.ChatViewModel;
import com.example.omrifit.R;
import com.example.omrifit.classes.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * RecyclerView Adapter for displaying a list of Message objects.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    private final List<Message> messageList;
    private final Context context;
    private final FirebaseUser user;
    private final ChatViewModel viewModel;
    private final boolean isGroup;
    private int lastUnreadMessagePosition = -1;
    private FirebaseDatabase database;

    /**
     * Constructor for MessageAdapter.
     *
     * @param context      The context of the calling activity.
     * @param messageList  The list of Message objects to be displayed.
     * @param isGroup      Boolean indicating if the chat is a group chat.
     */
    public MessageAdapter(Context context, List<Message> messageList, boolean isGroup) {
        this.messageList = messageList;
        this.context = context;
        this.isGroup = isGroup;
        this.user = FirebaseAuth.getInstance().getCurrentUser();
        this.viewModel = new ChatViewModel();
    }

    /**
     * Creates and inflates the view holder.
     *
     * @param parent   The parent view group.
     * @param viewType The view type.
     * @return A new MyViewHolder instance.
     */
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View chatView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
        return new MyViewHolder(chatView);
    }

    /**
     * Binds the data to the view holder.
     *
     * @param holder   The MyViewHolder instance.
     * @param position The position of the current item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Message message = messageList.get(position);
        String sender = message.getSent_by();
        boolean isCurrentUser = sender.equals("me") || sender.equals(user.getUid());

        if ((!isGroup && message.isUnread() && !user.getUid().equals(message.getSent_by())) || position == getItemCount() - 1) {
            lastUnreadMessagePosition = position;
        }

        holder.configureView(message, isCurrentUser, position);
    }

    /**
     * Returns the size of the data list.
     *
     * @return The size of the data list.
     */
    @Override
    public int getItemCount() {
        return messageList.size();
    }

    /**
     * Returns the position of the last unread message.
     *
     * @return The position of the last unread message.
     */
    public int getLastUnreadMessagePosition() {
        return lastUnreadMessagePosition;
    }

    /**
     * ViewHolder class for holding the view elements of each item.
     */
    class MyViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout leftChatView, rightChatView;
        final TextView leftChatTextView, rightChatTextView, sentByRight, sentByLeft, sentTimeRight, sentTimeLeft;
        final ImageView leftImage, rightImage;
        final CircleImageView leftImageProfile, rightImageProfile;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The view of the item.
         */
        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            leftChatView = itemView.findViewById(R.id.left_chat_view);
            rightChatView = itemView.findViewById(R.id.right_chat_view);
            leftChatTextView = itemView.findViewById(R.id.left_chat_text_view);
            rightChatTextView = itemView.findViewById(R.id.right_chat_text_view);
            sentByRight = itemView.findViewById(R.id.txt_sent_by_right);
            sentByLeft = itemView.findViewById(R.id.txt_sent_by_left);
            sentTimeRight = itemView.findViewById(R.id.txt_sent_time_right);
            sentTimeLeft = itemView.findViewById(R.id.txt_sent_time_left);
            leftImage = itemView.findViewById(R.id.imageView6);
            rightImage = itemView.findViewById(R.id.imageView7);
            leftImageProfile = itemView.findViewById(R.id.btnimageleft);
            rightImageProfile = itemView.findViewById(R.id.btnimageright);
        }

        /**
         * Configures the view based on the message and whether the sender is the current user.
         *
         * @param message      The Message object containing the message data.
         * @param isCurrentUser Boolean indicating if the sender is the current user.
         * @param position     The position of the current item in the list.
         */
        void configureView(Message message, boolean isCurrentUser, int position) {
            if (isCurrentUser) {
                configureRightView(message, position);
            } else {
                configureLeftView(message, position);
            }
        }

        /**
         * Configures the view for messages sent by the current user.
         *
         * @param message  The Message object containing the message data.
         * @param position The position of the current item in the list.
         */
        void configureRightView(Message message, int position) {
            rightChatView.setVisibility(View.VISIBLE);
            leftChatView.setVisibility(View.GONE);
            rightChatTextView.setText(message.getMessage());
            sentByRight.setText("you");
            sentTimeRight.setText(formatTimestamp(message.getTimestamp()));

            if (message.getBase64() != null) {
                rightImage.setImageBitmap(viewModel.base64ToBitmap(message.getBase64()));
                rightImage.setVisibility(View.VISIBLE);
            } else {
                rightImage.setVisibility(View.GONE);
            }

            loadProfileImage(rightImageProfile, user.getUid(), position, message.getSent_by());
        }

        /**
         * Configures the view for messages sent by other users.
         *
         * @param message  The Message object containing the message data.
         * @param position The position of the current item in the list.
         */
        void configureLeftView(Message message, int position) {
            leftChatView.setVisibility(View.VISIBLE);
            rightChatView.setVisibility(View.GONE);
            leftChatTextView.setText(message.getMessage());
            sentTimeLeft.setText(formatTimestamp(message.getTimestamp()));

            if (message.getBase64() != null) {
                leftImage.setImageBitmap(viewModel.base64ToBitmap(message.getBase64()));
                leftImage.setVisibility(View.VISIBLE);
            } else {
                leftImage.setVisibility(View.GONE);
            }

            if ("Omri".equals(message.getSent_by())) {
                sentByLeft.setText("Omri");
                if (position == 0 || !"Omri".equals(messageList.get(position - 1).getSent_by())) {
                    leftImageProfile.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        leftImageProfile.setImageDrawable(getDrawable(context, R.drawable.omri2));
                    }
                }
            } else {
                setUserDetails(message.getSent_by(), sentByLeft);
                loadProfileImage(leftImageProfile, message.getSent_by(), position, message.getSent_by());
            }
        }

        /**
         * Loads the profile image for the user.
         *
         * @param imageView        The ImageView to load the profile image into.
         * @param userId           The ID of the user.
         * @param position         The position of the current item in the list.
         * @param currentSenderId  The ID of the current sender.
         */
        void loadProfileImage(ImageView imageView, String userId, int position, String currentSenderId) {
            if (position == 0 || !currentSenderId.equals(messageList.get(position - 1).getSent_by())) {
                imageView.setVisibility(View.VISIBLE);
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference imageRef = storageRef.child(userId + "_img_profile");
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).noFade().into(imageView));
            }
        }
    }

    /**
     * Sets the user details in the chat view.
     *
     * @param userId           The ID of the user.
     * @param sentByTextView   The TextView to display the sender's name.
     */
    void setUserDetails(String userId, TextView sentByTextView) {
        database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("user_information").child(userId).child("user_profile").child("name");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.getValue(String.class);
                    sentByTextView.setText(name); // Set the fetched name to TextView
                } else {
                    sentByTextView.setText("Unknown User"); // Fallback text
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                sentByTextView.setText("Error"); // Error handling
            }
        });
    }

    /**
     * Formats the timestamp for display.
     *
     * @param timestamp The timestamp to be formatted.
     * @return The formatted timestamp string.
     */
    private String formatTimestamp(long timestamp) {
        // Similar timestamp formatting logic
        // Use SimpleDateFormat for a real application
        return String.format("%tF %<tT", timestamp);
    }
}
