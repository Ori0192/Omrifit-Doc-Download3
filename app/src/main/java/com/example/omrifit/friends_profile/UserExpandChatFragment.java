package com.example.omrifit.friends_profile;

import static android.app.Activity.RESULT_OK;
import static com.example.omrifit.classes.Utility.createChatReference;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.omrifit.ChatViewModel;
import com.example.omrifit.R;
import com.example.omrifit.adapters.MessageAdapter;
import com.example.omrifit.classes.Message;
import com.example.omrifit.classes.ProfileInfo;
import com.example.omrifit.fragments.HomePageActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.mlkit.nl.smartreply.SmartReply;
import com.google.mlkit.nl.smartreply.SmartReplyGenerator;
import com.google.mlkit.nl.smartreply.SmartReplySuggestion;
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult;
import com.google.mlkit.nl.smartreply.TextMessage;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to handle chat functionality in the user profile expand screen.
 */
public class UserExpandChatFragment extends Fragment {
    private RecyclerView recyclerView;
    private EditText messageEditText;
    private ArrayList<Message> messageList;
    private MessageAdapter messageAdapter;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_GALLERY_PHOTO = 2;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference chatRef = database.getReference("chats");
    private DatabaseReference myRef = database.getReference("user_information").child(user.getUid());
    private CardView cardViewForFriendshipRequest;
    private TextInputLayout cardViewChat;
    private boolean isFriend;
    private ImageButton addImageButton;
    private Button suggestion1, suggestion2, suggestion3;
    private ImageView imageView;
    private Bitmap bitmapToSend;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.user_expand_activity, container, false);
        initializeUI(view);

        Bundle bundle = getArguments();
        String json = bundle.getString("profile", "");
        Type type = new TypeToken<ProfileInfo>() {}.getType();
        Gson gson = new Gson();
        ProfileInfo profileInfo = gson.fromJson(json, type);

        setupMessageEditText();
        setupAddImageButton();

        DatabaseReference friendRef = database.getReference("user_information").child(profileInfo.getId());

        isFriend = false;
        myRef.child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot shot : dataSnapshot.getChildren()) {
                        if (profileInfo.getId().equals(shot.getValue(String.class))) {
                            isFriend = true;
                            break;
                        }
                    }
                }
                if (isFriend) {
                    openChat(profileInfo);
                } else {
                    showFriendshipRequestOption(profileInfo, friendRef, view);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
        return view;
    }

    /**
     * Initialize UI elements.
     *
     * @param view The parent view containing the UI elements.
     */
    private void initializeUI(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_search);
        messageEditText = view.findViewById(R.id.edit_text);
        suggestion1 = view.findViewById(R.id.suggestion1);
        imageView = view.findViewById(R.id.imageViewfirstview);
        suggestion2 = view.findViewById(R.id.suggestion2);
        suggestion3 = view.findViewById(R.id.suggestion3);
        addImageButton = view.findViewById(R.id.addImageButton);
        cardViewForFriendshipRequest = view.findViewById(R.id.cardViewfriendshiprequest);
        cardViewChat = view.findViewById(R.id.idTILQuery);
    }

    /**
     * Set up the message edit text to send messages when the send action is triggered.
     */
    private void setupMessageEditText() {
        messageEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                if (messageEditText.getText().toString().length() > 0) {
                    addToChat(messageEditText.getText().toString());
                } else {
                    Toast.makeText(requireContext(), "Please enter your query..", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
    }

    /**
     * Open the chat for the specified user profile.
     *
     * @param profileInfo The profile information of the user to chat with.
     */
    private void openChat(ProfileInfo profileInfo) {
        chatRef = chatRef.child(createChatReference(user.getUid(), profileInfo.getId()));
        cardViewForFriendshipRequest.setVisibility(View.GONE);
        cardViewChat.setVisibility(View.VISIBLE);

        messageList = new ArrayList<>();
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot shot : snapshot.getChildren()) {
                    Message message = new Message(shot.child("message").getValue(String.class),
                            shot.child("sent_by").getValue(String.class), Boolean.TRUE.equals(shot.child("unread").getValue(boolean.class)),
                            shot.child("timestamp").getValue(long.class));
                    if (shot.child("base64").exists()) {
                        message.setBase64(shot.child("base64").getValue(String.class));
                    }
                    messageList.add(message);
                }
                setupRecyclerView(messageList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (Boolean.TRUE.equals(dataSnapshot.child("unread").getValue(Boolean.class))
                            && dataSnapshot.child("sent_by").getValue(String.class).equals(profileInfo.getId())) {
                        dataSnapshot.child("unread").getRef().setValue(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    /**
     * Show the friendship request option if the users are not friends.
     *
     * @param profileInfo The profile information of the user to send a request to.
     * @param friendRef The database reference of the friend.
     * @param view The parent view containing the UI elements.
     */
    private void showFriendshipRequestOption(ProfileInfo profileInfo, DatabaseReference friendRef, View view) {
        cardViewForFriendshipRequest.setVisibility(View.VISIBLE);
        cardViewChat.setVisibility(View.GONE);

        Button btnSendRequest = view.findViewById(R.id.btn_send_request);
        TextView txtRequestFriend = view.findViewById(R.id.txt_friendshuprequest);

        btnSendRequest.setOnClickListener(v -> {
            friendRef.child("requests").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<String> requests = new ArrayList<>();
                    if (snapshot.exists()) {
                        for (DataSnapshot shot : snapshot.getChildren()) {
                            String request = shot.getValue(String.class);
                            requests.add(request);
                        }
                    }

                    if (!requests.contains(user.getUid())) {
                        requests.add(user.getUid());
                        friendRef.child("requests").setValue(requests);
                        btnSendRequest.setVisibility(View.GONE);
                        txtRequestFriend.setText("Request sent");
                    } else {
                        Toast.makeText(requireActivity(), "Request already sent", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        });
    }

    /**
     * Add a message to the chat.
     *
     * @param response The message to add.
     */
    public void addToChat(String response) {
        ChatViewModel viewModel = new ChatViewModel();
        Message message = new Message(response, user.getUid());
        if (bitmapToSend != null) {
            message.setBase64(viewModel.bitmapToBase64(bitmapToSend));
            bitmapToSend = null;
            imageView.setImageBitmap(null);
        }
        chatRef.child(messageList.size() + "").setValue(message);
        clearMessageInput();
    }

    /**
     * Set up the RecyclerView to display chat messages.
     *
     * @param messageList The list of messages to display.
     */
    public void setupRecyclerView(ArrayList<Message> messageList) {
        messageAdapter = new MessageAdapter(requireContext(), messageList, false);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(new HomePageActivity());
        layoutManager.setStackFromEnd(false);
        recyclerView.setLayoutManager(layoutManager);
        suggestReplies(messageList);
        scrollToLastUnreadMessage();
    }

    /**
     * Scroll to the last unread message in the chat.
     */
    private void scrollToLastUnreadMessage() {
        recyclerView.post(() -> {
            int lastUnreadPosition = messageAdapter.getLastUnreadMessagePosition();
            if (lastUnreadPosition != -1) {
                recyclerView.scrollToPosition(lastUnreadPosition);
            }
        });
    }

    /**
     * Suggest replies based on the chat conversation.
     *
     * @param messageList The list of messages in the conversation.
     */
    private void suggestReplies(ArrayList<Message> messageList) {
        if (messageList.size() > 0) {
            ArrayList<TextMessage> conversation = new ArrayList<>();
            for (Message message : messageList) {
                conversation.add(message.getTextMessage());
            }
            SmartReplyGenerator smartReply = SmartReply.getClient();
            smartReply.suggestReplies(conversation)
                    .addOnSuccessListener(new OnSuccessListener<SmartReplySuggestionResult>() {
                        @Override
                        public void onSuccess(SmartReplySuggestionResult result) {
                            if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                                List<SmartReplySuggestion> suggestions = result.getSuggestions();
                                if (!suggestions.isEmpty()) {
                                    setupSuggestionButtons(suggestions);
                                } else {
                                    hideSuggestionButtons();
                                }
                            }
                        }
                    });
        }
    }

    /**
     * Set up the suggestion buttons with the suggested replies.
     *
     * @param suggestions The list of suggested replies.
     */
    private void setupSuggestionButtons(List<SmartReplySuggestion> suggestions) {
        suggestion1.setVisibility(suggestions.size() > 0 ? View.VISIBLE : View.GONE);
        suggestion2.setVisibility(suggestions.size() > 1 ? View.VISIBLE : View.GONE);
        suggestion3.setVisibility(suggestions.size() > 2 ? View.VISIBLE : View.GONE);

        if (suggestions.size() > 0)
            suggestion1.setText(suggestions.get(0).getText());
        if (suggestions.size() > 1)
            suggestion2.setText(suggestions.get(1).getText());
        if (suggestions.size() > 2)
            suggestion3.setText(suggestions.get(2).getText());

        suggestion1.setOnClickListener(v -> messageEditText.setText(suggestion1.getText().toString()));
        suggestion2.setOnClickListener(v -> messageEditText.setText(suggestion2.getText().toString()));
        suggestion3.setOnClickListener(v -> messageEditText.setText(suggestion3.getText().toString()));
    }

    /**
     * Hide the suggestion buttons when there are no suggestions.
     */
    private void hideSuggestionButtons() {
        suggestion1.setVisibility(View.GONE);
        suggestion2.setVisibility(View.GONE);
        suggestion3.setVisibility(View.GONE);
    }

    /**
     * Set up the Add Image button to open the camera or gallery.
     */
    private void setupAddImageButton() {
        addImageButton.setOnClickListener(v -> {
            String[] options = {"Take a photo", "Choose from gallery"};
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Select Image From");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        openCamera();
                    } else if (which == 1) {
                        openGallery();
                    }
                }
            });
            builder.show();
        });
    }

    /**
     * Open the camera to take a photo.
     */
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    /**
     * Open the gallery to choose a photo.
     */
    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO);
    }

    /**
     * Clear the message input field.
     */
    public void clearMessageInput() {
        messageEditText.setText("");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            bitmapToSend = imageBitmap;
            imageView.setImageBitmap(imageBitmap);
        } else if (requestCode == REQUEST_GALLERY_PHOTO && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            imageView.setImageURI(selectedImage);
        }
    }
}
