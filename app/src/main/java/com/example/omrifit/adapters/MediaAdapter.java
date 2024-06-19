package com.example.omrifit.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.omrifit.R;
import com.example.omrifit.classes.MediaItem;
import com.example.omrifit.media.MediaFullScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * RecyclerView Adapter for displaying a list of MediaItem objects (images and videos).
 */
public class MediaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_VIDEO = 0;
    private static final int TYPE_IMAGE = 1;
    private final LayoutInflater mInflater;
    private final List<MediaItem> mediaList;
    private final Context mContext;
    private final boolean isme;
    private final String anotherId;
    private final FirebaseUser user;

    /**
     * Constructor for MediaAdapter.
     *
     * @param context  The context of the calling activity.
     * @param mediaList The list of MediaItem objects to be displayed.
     * @param isme     Boolean indicating if the current user is the owner of the media.
     * @param anotherId The ID of another user.
     */
    public MediaAdapter(Context context, List<MediaItem> mediaList, boolean isme, String anotherId) {
        this.mContext = context;
        this.mediaList = mediaList;
        this.mInflater = LayoutInflater.from(context);
        this.isme = isme;
        this.anotherId = anotherId;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        this.user = mAuth.getCurrentUser();
    }

    /**
     * Determines the type of view for the item at the given position.
     *
     * @param position The position of the item in the list.
     * @return The type of view for the item.
     */
    @Override
    public int getItemViewType(int position) {
        MediaItem item = mediaList.get(position);
        return item.getType() == MediaItem.MediaType.VIDEO ? TYPE_VIDEO : TYPE_IMAGE;
    }

    /**
     * Creates and inflates the view holder.
     *
     * @param parent   The parent view group.
     * @param viewType The view type.
     * @return A new RecyclerView.ViewHolder instance.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_VIDEO) {
            View view = mInflater.inflate(R.layout.item_video, parent, false);
            return new VideoViewHolder(view);
        } else {
            View view = mInflater.inflate(R.layout.item_photo, parent, false);
            return new ImageViewHolder(view);
        }
    }

    /**
     * Binds the data to the view holder.
     *
     * @param holder   The RecyclerView.ViewHolder instance.
     * @param position The position of the current item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MediaItem item = mediaList.get(position);
        if (getItemViewType(position) == TYPE_VIDEO) {
            VideoViewHolder videoHolder = (VideoViewHolder) holder;
            setupVideoViewHolder(videoHolder, item, position);
        } else {
            ImageViewHolder imageHolder = (ImageViewHolder) holder;
            setupImageViewHolder(imageHolder, item, position);
        }

        holder.itemView.setOnClickListener(v -> {
            Gson gson = new Gson();
            String json = gson.toJson(mediaList);
            Intent intent = new Intent(mContext, MediaFullScreen.class);
            intent.putExtra("position", position);
            intent.putExtra("anotherId", anotherId);
            intent.putExtra("mediaList", json);
            mContext.startActivity(intent);
        });
    }

    /**
     * Configures the VideoViewHolder to display a video.
     *
     * @param holder   The VideoViewHolder instance.
     * @param item     The MediaItem object containing the video data.
     * @param position The position of the current item in the list.
     */
    private void setupVideoViewHolder(VideoViewHolder holder, MediaItem item, int position) {
        holder.videoView.setVideoURI(Uri.parse(item.getUrl()));
        if (!isme) {
            updateLikedStatus(holder.btn_status, position);
            holder.btn_status.setOnClickListener(v -> toggleLikeStatus(holder.btn_status, position));
        }
    }

    /**
     * Configures the ImageViewHolder to display an image.
     *
     * @param holder   The ImageViewHolder instance.
     * @param item     The MediaItem object containing the image data.
     * @param position The position of the current item in the list.
     */
    private void setupImageViewHolder(ImageViewHolder holder, MediaItem item, int position) {
        Glide.with(mContext).load(item.getUrl()).into(holder.imageView);
        if (!isme) {
            updateLikedStatus(holder.btn_status, position);
            holder.btn_status.setOnClickListener(v -> toggleLikeStatus(holder.btn_status, position));
        }
    }

    /**
     * Updates the liked status of a media item.
     *
     * @param btnStatus The CircleImageView for displaying the like status.
     * @param position  The position of the current item in the list.
     */
    private void updateLikedStatus(CircleImageView btnStatus, int position) {
        isLiked(position, isLiked -> btnStatus.setImageResource(isLiked ? R.drawable.like : R.drawable.dislike));
    }

    /**
     * Toggles the like status of a media item.
     *
     * @param btnStatus The CircleImageView for displaying the like status.
     * @param position  The position of the current item in the list.
     */
    private void toggleLikeStatus(CircleImageView btnStatus, int position) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                .getReference("user_information").child(user.getUid()).child("likedimages").child(anotherId).child("img" + position);

        isLiked(position, isLiked -> {
            if (isLiked) {
                databaseRef.removeValue(); // Unlike the image
                btnStatus.setImageResource(R.drawable.dislike);
            } else {
                databaseRef.setValue(true); // Like the image
                btnStatus.setImageResource(R.drawable.like);
            }
        });
    }

    /**
     * Returns the size of the data list.
     *
     * @return The size of the data list.
     */
    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    /**
     * ViewHolder class for holding the view elements of each item for images.
     */
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final CircleImageView btn_status;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The view of the item.
         */
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.videoView);
            btn_status = itemView.findViewById(R.id.btn_like);

            DisplayMetrics displayMetrics = itemView.getContext().getResources().getDisplayMetrics();
            int screenWidth = displayMetrics.widthPixels / 3;
            itemView.setMinimumWidth(screenWidth);
        }
    }

    /**
     * ViewHolder class for holding the view elements of each item for videos.
     */
    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        final VideoView videoView;
        final CircleImageView btn_status;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The view of the item.
         */
        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.videoView);
            btn_status = itemView.findViewById(R.id.btn_like);
        }
    }

    /**
     * Interface for checking liked status of a media item.
     */
    public interface LikedStatusListener {
        void onStatusChecked(boolean isLiked);
    }

    /**
     * Checks if a media item is liked by the current user.
     *
     * @param position The position of the media item.
     * @param listener The listener for liked status.
     */
    public void isLiked(int position, LikedStatusListener listener) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                .getReference("user_information").child(user.getUid()).child("likedimages").child(anotherId).child("img" + position);

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listener.onStatusChecked(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onStatusChecked(false);
            }
        });
    }
}
