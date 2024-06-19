package com.example.omrifit.friends_profile;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.omrifit.R;
import com.example.omrifit.adapters.MediaAdapter;
import com.example.omrifit.classes.MediaItem;
import com.example.omrifit.classes.ProfileInfo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

/**
 * Fragment to display the media gallery for a user's profile.
 */
public class UserExpandGallery extends Fragment {

    private RecyclerView recyclerView;
    private MediaAdapter adapter;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private ArrayList<MediaItem> mediaList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_gallery, container, false);

        FloatingActionButton fab = view.findViewById(R.id.fabButton);
        fab.setVisibility(View.GONE);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        Bundle bundle = getArguments();
        String json = bundle.getString("profile", "");
        Type type = new TypeToken<ProfileInfo>() {}.getType();
        Gson gson = new Gson();
        ProfileInfo profileInfo = gson.fromJson(json, type);

        adapter = new MediaAdapter(requireContext(), mediaList, false, profileInfo.getId());
        recyclerView.setAdapter(adapter);

        getMedia(profileInfo);

        return view;
    }

    /**
     * Retrieve media for the given user profile and display it in the gallery.
     *
     * @param profileInfo The profile information of the user.
     */
    public void getMedia(ProfileInfo profileInfo) {
        mediaList = new ArrayList<>();
        addMediaFromFolder(profileInfo.getId() + "/public");
        FirebaseDatabase.getInstance().getReference("user_information")
                .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .child("friends")
                .child(profileInfo.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            addMediaFromFolder(profileInfo.getId() + "/only friends");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Collections.sort(mediaList, Comparator.comparingInt(MediaItem::getId));
            Collections.reverse(mediaList);
        }
        setUpRecyclerView(mediaList, profileInfo);
    }

    /**
     * Set up the RecyclerView to display media items.
     *
     * @param mediaItems  The list of media items to display.
     * @param profileInfo The profile information of the user.
     */
    public void setUpRecyclerView(ArrayList<MediaItem> mediaItems, ProfileInfo profileInfo) {
        adapter = new MediaAdapter(requireContext(), mediaItems, false, profileInfo.getId());
        recyclerView.setAdapter(adapter);
    }

    /**
     * Add media items from the specified folder to the media list.
     *
     * @param folderPath The path of the folder to retrieve media from.
     */
    private void addMediaFromFolder(String folderPath) {
        StorageReference folderRef = storageRef.child(folderPath);
        folderRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference fileRef : listResult.getItems()) {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String url = uri.toString();
                    MediaItem.MediaType mediaType = fileRef.getName().endsWith(".mp4") ? MediaItem.MediaType.VIDEO : MediaItem.MediaType.IMAGE;
                    if (mediaType == MediaItem.MediaType.VIDEO) {
                        Toast.makeText(requireContext(), "Video", Toast.LENGTH_SHORT).show();
                    }
                    mediaList.add(new MediaItem(url, mediaType));
                    adapter.notifyItemInserted(mediaList.size() - 1);
                }).addOnFailureListener(e -> {
                    // Handle failure
                });
            }
        }).addOnFailureListener(e -> {
            // Handle failure
        });
    }
}
