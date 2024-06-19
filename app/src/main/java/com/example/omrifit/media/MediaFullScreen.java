package com.example.omrifit.media;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;

import com.example.omrifit.R;
import com.example.omrifit.classes.MediaItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Activity to display media in fullscreen mode, supporting both images and videos.
 */
public class MediaFullScreen extends AppCompatActivity {
    private VideoView videoView;
    private GestureDetector gestureDetector;
    private List<MediaItem> mediaList;
    private int position;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String anotherId;
    private FirebaseUser user = mAuth.getCurrentUser();
    private DatabaseReference databaseRef;
    private TextView txtLikesAmount, txtDescription;
    private View draggableCardView;

    /**
     * Called when the activity is starting.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this Bundle contains the most recent data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve data from intent
        String json = getIntent().getStringExtra("mediaList");
        position = getIntent().getIntExtra("position", 0);
        mediaList = new Gson().fromJson(json, new TypeToken<List<MediaItem>>() {}.getType());
        anotherId = getIntent().getStringExtra("anotherId");
        databaseRef = FirebaseDatabase.getInstance()
                .getReference("user_information").child(anotherId).child("likedImages").child("img" + position);

        // Setup gesture detector
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(e1.getX() - e2.getX()) > Math.abs(e1.getY() - e2.getY())) {
                    if (e2.getX() < e1.getX()) {
                        // Swipe left
                        navigateLeft();
                    } else {
                        // Swipe right
                        navigateRight();
                    }
                    return true;
                }
                return false;
            }
        });

        // Update the view with the current media item
        updateMediaView();
        setupTouchListener(); // Call this after setting the content view
    }

    /**
     * Sets up the touch listener for draggable card view.
     */
    private void setupTouchListener() {
        final View draggableCardView = findViewById(R.id.draggableCardView);
        draggableCardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        if (v.getTranslationY() < 200) {
                            v.animate().translationY(600).setDuration(200).start();
                        } else {
                            v.animate().translationY(0).setDuration(300).start();
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (v.getTranslationY() < 200) {
                            v.animate().translationY(600).setDuration(200).start();
                        } else {
                            v.animate().translationY(0).setDuration(300).start();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * Navigates to the previous media item.
     */
    private void navigateRight() {
        if (position > 0) {
            position--;
            updateMediaView();
            setupTouchListener();
        } else {
            Toast.makeText(this, "No more items to the right", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navigates to the next media item.
     */
    private void navigateLeft() {
        if (position < mediaList.size() - 1) {
            position++;
            updateMediaView();
            setupTouchListener();
        } else {
            Toast.makeText(this, "No more items to the left", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Updates the view with the current media item.
     */
    private void updateMediaView() {
        MediaItem currentMedia = mediaList.get(position);
        if (currentMedia.getType() == MediaItem.MediaType.IMAGE) {
            setContentView(R.layout.photofullpage);
            ImageView imageView = findViewById(R.id.imageView);
            Glide.with(this).load(currentMedia.getUrl()).into(imageView);
            CircleImageView btnStatus = findViewById(R.id.btn_like);
            txtLikesAmount = findViewById(R.id.txtLikesAmount);
            txtDescription = findViewById(R.id.txtDescription);
            draggableCardView = findViewById(R.id.draggableCardView);

            if (!user.getUid().equals(anotherId)) {
                updateLikedStatus(btnStatus);
                btnStatus.setOnClickListener(v -> toggleLikeStatus(btnStatus));
            }

        } else if (currentMedia.getType() == MediaItem.MediaType.VIDEO) {
            setContentView(R.layout.videofullpage);
            videoView = findViewById(R.id.videoView);
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(Uri.parse(currentMedia.getUrl()));
            videoView.start();
            CircleImageView btnStatus = findViewById(R.id.btn_like);
            txtLikesAmount = findViewById(R.id.txtLikesAmount);
            txtDescription = findViewById(R.id.txtDescription);
            draggableCardView = findViewById(R.id.draggableCardView);

            if (!user.getUid().equals(anotherId)) {
                updateLikedStatus(btnStatus);
                btnStatus.setOnClickListener(v -> toggleLikeStatus(btnStatus));
            }
        }

        DatabaseReference descriptionRef = FirebaseDatabase.getInstance().getReference("user_information").child(anotherId).child("descriptions").child("img" + position);
        descriptionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txtDescription.setText(snapshot.exists() ? snapshot.getValue(String.class) : "none");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txtLikesAmount.setText(snapshot.exists() ? String.valueOf(snapshot.getChildrenCount()) : "0");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    /**
     * Handles touch events.
     * @param event The motion event.
     * @return True if the event was handled, false otherwise.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    /**
     * Updates the like button status based on whether the media is liked or not.
     * @param btnStatus The like button.
     */
    private void updateLikedStatus(CircleImageView btnStatus) {
        isLiked(isLiked -> btnStatus.setImageResource(isLiked ? R.drawable.like : R.drawable.dislike));
    }

    /**
     * Toggles the like status of the media.
     * @param btnStatus The like button.
     */
    private void toggleLikeStatus(CircleImageView btnStatus) {
        isLiked(isLiked -> {
            if (isLiked) {
                databaseRef.child(user.getUid()).removeValue(); // Unlike the image
                btnStatus.setImageResource(R.drawable.dislike);
            } else {
                databaseRef.child(user.getUid()).setValue(1); // Like the image
                btnStatus.setImageResource(R.drawable.like);
            }
        });
    }

    /**
     * Checks if the media is liked by the current user.
     * @param listener The listener to handle the result.
     */
    public void isLiked(LikedStatusListener listener) {
        databaseRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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

    /**
     * Listener interface for checking the like status.
     */
    public interface LikedStatusListener {
        void onStatusChecked(boolean isLiked);
    }
}
