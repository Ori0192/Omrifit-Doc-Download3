package com.example.omrifit.media;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.omrifit.R;
import com.example.omrifit.adapters.MediaAdapter;
import com.example.omrifit.classes.MediaItem;
import com.example.omrifit.photo_editor.EditImageActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Activity to manage the user's media gallery, including image and video uploads.
 */
public class GalleryActivity extends AppCompatActivity {
    private static final int PICK_MEDIA_REQUEST = 1;
    private RecyclerView recyclerView;
    private MediaAdapter adapter;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private ArrayList<MediaItem> mediaItems = new ArrayList<>();
    private String mediaPrivacy = "public"; // Default privacy
    private ProgressBar uploadProgressBar;
    private CardView cardViewUpload;
    private Intent intent;

    /**
     * Called when the activity is starting.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this Bundle contains the most recent data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        initializeUIElements();
        setupFloatingActionButton();
        setupRecyclerView();
        getMedia();
    }

    /**
     * Initializes UI elements.
     */
    private void initializeUIElements() {
        cardViewUpload = findViewById(R.id.cardViewUpload);
        uploadProgressBar = findViewById(R.id.uploadProgressBar);
        FloatingActionButton fab = findViewById(R.id.fabButton);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
    }

    /**
     * Sets up the FloatingActionButton for media selection.
     */
    private void setupFloatingActionButton() {
        FloatingActionButton fab = findViewById(R.id.fabButton);
        fab.setOnClickListener(v -> openMediaSelector());
    }

    /**
     * Sets up the RecyclerView.
     */
    private void setupRecyclerView() {
        adapter = new MediaAdapter(GalleryActivity.this, mediaItems, true, user.getUid());
        recyclerView.setAdapter(adapter);
    }

    /**
     * Fetches media items and populates the RecyclerView.
     */
    public void getMedia() {
        mediaItems = new ArrayList<>();
        addMediaFromFolder(user.getUid() + "/public");
        addMediaFromFolder(user.getUid() + "/private");
        addMediaFromFolder(user.getUid() + "/only friends");
        setUpRv(mediaItems);
    }

    /**
     * Sets up the RecyclerView with media items.
     * @param mediaItems List of media items.
     */
    public void setUpRv(ArrayList<MediaItem> mediaItems) {
        adapter = new MediaAdapter(GalleryActivity.this, mediaItems, true, user.getUid());
        recyclerView.setAdapter(adapter);
    }

    /**
     * Adds media items from a specific folder.
     * @param folderPath Path of the folder to add media from.
     */
    private void addMediaFromFolder(String folderPath) {
        StorageReference folderRef = storageRef.child(folderPath);
        folderRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference fileRef : listResult.getItems()) {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String url = uri.toString();
                    MediaItem.MediaType mediaType = fileRef.getName().endsWith(".mp4") ? MediaItem.MediaType.VIDEO : MediaItem.MediaType.IMAGE;
                    mediaItems.add(new MediaItem(url, mediaType));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Collections.sort(mediaItems, Comparator.comparingInt(MediaItem::getId));
                        Collections.reverse(mediaItems);
                    }
                    adapter.notifyDataSetChanged();
                    adapter.notifyItemInserted(mediaItems.size() - 1);
                }).addOnFailureListener(e -> {
                    // Handle failure
                });
            }
        }).addOnFailureListener(e -> {
            // Handle failure
        });
    }

    /**
     * Opens the media selector dialog.
     */
    private void openMediaSelector() {
        new AlertDialog.Builder(this)
                .setTitle("Select Privacy")
                .setItems(new String[]{"Public", "only friends", "Private"}, (dialog, which) -> {
                    if (which == 2) {
                        mediaPrivacy = "private";
                    } else if (which == 1) {
                        mediaPrivacy = "only friends";
                    } else {
                        mediaPrivacy = "public";
                    }
                    selectMediaType();
                }).show();
    }

    /**
     * Selects the media type (image or video).
     */
    private void selectMediaType() {
        String[] options = {"Image", "Video"};

        new AlertDialog.Builder(this)
                .setTitle("Upload Media")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        intent = new Intent(GalleryActivity.this, EditImageActivity.class);
                        intent.putExtra("directory", mediaPrivacy);
                        intent.putExtra("id", adapter.getItemCount());
                        startActivity(intent);
                    } else {
                        selectMediaFromGallery("video");
                    }
                })
                .show();
    }

    /**
     * Selects media from the gallery.
     * @param mediaType Type of media to select (image or video).
     */
    private void selectMediaFromGallery(String mediaType) {
        Intent intent = new Intent();
        intent.setType(mediaType.equals("image") ? "image/*" : "video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_MEDIA_REQUEST);
    }

    /**
     * Handles the result of the media selection activity.
     * @param requestCode The request code originally supplied to startActivityForResult().
     * @param resultCode The result code returned by the child activity.
     * @param data An Intent that carries the result data.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_MEDIA_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedMediaUri = data.getData();
            String mediaType = getContentResolver().getType(selectedMediaUri).startsWith("image/") ? "image" : "video";
            enterVideoDescription(selectedMediaUri, mediaType);
        }
    }

    /**
     * Prompts the user to enter a description for the video.
     * @param selectedMediaUri URI of the selected media.
     * @param mediaType Type of the selected media (image or video).
     */
    private void enterVideoDescription(Uri selectedMediaUri, String mediaType) {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Enter Video Description")
                .setView(input)
                .setPositiveButton("OK", (dialog, whichButton) -> {
                    String videoDescription = input.getText().toString();
                    uploadMedia(selectedMediaUri, mediaType, videoDescription);
                })
                .setNegativeButton("No description", (dialog, whichButton) -> {
                    uploadMedia(selectedMediaUri, mediaType, "");
                })
                .show();
    }

    /**
     * Uploads the selected media to Firebase Storage.
     * @param uri URI of the media to upload.
     * @param mediaType Type of the media (image or video).
     * @param description Description of the media.
     */
    private void uploadMedia(Uri uri, String mediaType, String description) {
        String directory = "/" + mediaPrivacy;
        String fileType = mediaType.equals("image") ? ".jpg" : ".mp4";
        StorageReference mediaRef = storageRef.child(user.getUid() + directory + "/img" + adapter.getItemCount() + fileType);

        cardViewUpload.setVisibility(View.VISIBLE);

        UploadTask uploadTask = mediaRef.putFile(uri);
        DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                .getReference("user_information").child(user.getUid()).child("descriptions")
                .child("img" + adapter.getItemCount());
        databaseRef.setValue(description);

        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            uploadProgressBar.setProgress((int) progress);
        }).addOnSuccessListener(taskSnapshot -> {
            cardViewUpload.setVisibility(View.GONE);
            getMedia();
        }).addOnFailureListener(e -> {
            uploadProgressBar.setVisibility(View.GONE);
            // Handle failure
        });
    }
}
