package com.example.omrifit.classes;

import android.net.Uri;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Utility class providing various static methods for common tasks.
 */
public class Utility {

    /**
     * Sets the user's profile photo in the given ImageView using Firebase Storage.
     *
     * @param uid             The user ID.
     * @param imageUserPhoto  The CircleImageView where the photo will be set.
     */
    public static void setImageUserPhoto(String uid, CircleImageView imageUserPhoto) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(uid + "_img_profile");

        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(imageUserPhoto);
            }
        });
    }

    /**
     * Creates a chat reference string by concatenating two user IDs in alphabetical order.
     *
     * @param userId1 The first user ID.
     * @param userId2 The second user ID.
     * @return The chat reference string.
     */
    public static String createChatReference(String userId1, String userId2) {
        // Sort the user IDs alphabetically and concatenate them with an underscore
        return userId1.compareTo(userId2) < 0 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }
}
