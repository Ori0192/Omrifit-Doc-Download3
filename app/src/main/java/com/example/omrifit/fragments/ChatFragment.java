package com.example.omrifit.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.omrifit.Chat;
import com.example.omrifit.ChatResponseCallback;
import com.example.omrifit.ChatViewModel;
import com.example.omrifit.R;
import com.example.omrifit.adapters.MessageAdapter;
import com.example.omrifit.classes.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for chat functionality, allowing users to send and receive messages.
 */
public class ChatFragment extends Fragment {

    private static final int REQUEST_PERMISSIONS_CODE = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_GALLERY_PHOTO = 2;

    private RecyclerView recyclerView;
    private EditText messageEditText;
    private ImageView imageView;
    private ImageButton addImageButton;

    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter messageAdapter;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("user_information").child(user.getUid());

    private Bitmap bitmapToSend = null;
    private ChatViewModel viewModel = new ChatViewModel();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_fragemant, container, false);

        initializeUI(view);
        setupEventListeners();
        checkPermissions();
        loadChat();

        if (requireActivity().getIntent().getBooleanExtra("toOmriChat", false)) {
            prepareForOmriChat();
        }

        return view;
    }

    /**
     * Initializes the UI components.
     */
    private void initializeUI(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_search);
        messageEditText = view.findViewById(R.id.edit_text);
        addImageButton = view.findViewById(R.id.addImageButton);
        imageView = view.findViewById(R.id.imageViewfirstview);

        messageEditText.setText("");
        myRef.child("Omri").child("newMessages").setValue(0);
    }

    /**
     * Sets up event listeners for UI components.
     */
    private void setupEventListeners() {
        addImageButton.setOnClickListener(v -> setupAddImageButton());

        messageEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                handleSendMessage();
                return true;
            }
            return false;
        });
    }

    /**
     * Checks for necessary permissions and requests them if not granted.
     */
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_CODE);
        }
    }

    /**
     * Prepares the chat for interaction with Omri.
     */
    private void prepareForOmriChat() {
        messageEditText.setText(requireActivity().getIntent().getStringExtra("exercise"));
        messageEditText.requestFocus();
        showKeyboard(messageEditText);
    }

    /**
     * Shows the keyboard.
     */
    private void showKeyboard(EditText editText) {
        if (editText.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    /**
     * Handles sending a message.
     */
    private void handleSendMessage() {
        String messageText = messageEditText.getText().toString();
        if (!messageText.isEmpty()) {
            sendMessageToChat(messageText, Message.SENT_BY_ME, bitmapToSend);

            if (bitmapToSend == null) {
                viewModel.getResponse(messageText, new ChatResponseCallback() {
                    @Override
                    public void onSuccess(Chat chat) {
                        sendMessageToChat(chat.getPrompt(), Message.SENT_BY_ME, bitmapToSend);
                    }

                    @Override
                    public void onError(String error) {
                        // Handle error
                    }
                });
            } else {
                imageView.setImageBitmap(null);
                viewModel.getResponseWithImage(messageText, bitmapToSend, new ChatResponseCallback() {
                    @Override
                    public void onSuccess(Chat chat) {
                        bitmapToSend = null;
                        sendMessageToChat(chat.getPrompt(), Message.SENT_BY_OMRI, bitmapToSend);
                    }

                    @Override
                    public void onError(String error) {
                        // Handle error
                    }
                });
            }
            messageEditText.setText("");
        } else {
            Toast.makeText(requireContext(), "Please enter your query..", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sends a message to the chat and updates the UI.
     */
    private void sendMessageToChat(String prompt, String sentBy, Bitmap bitmap) {
        getActivity().runOnUiThread(() -> {
            Message message = new Message(prompt, sentBy);
            if (!prompt.equals("typing...")) {
                if (bitmap != null) {
                    message.setBase64(viewModel.bitmapToBase64(bitmap));
                }
                myRef.child("Omri").child("chat").child(messageList.size() + "").setValue(message);
            }
            messageList.add(message);
            messageAdapter.notifyItemInserted(messageList.size());
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        });
    }

    /**
     * Sets up the Add Image button.
     */
    private void setupAddImageButton() {
        String[] options = {"Take Photo", "Choose from Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Image Source");

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else if (which == 1) {
                openGallery();
            }
        });

        builder.show();
    }

    /**
     * Opens the camera to take a photo.
     */
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    /**
     * Opens the gallery to choose a photo.
     */
    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO);
    }

    /**
     * Loads the chat messages from Firebase.
     */
    private void loadChat() {
        myRef.child("Omri").child("chat").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    messageList.clear();
                    for (DataSnapshot shot : snapshot.getChildren()) {
                        Message message = new Message(
                                shot.child("message").getValue(String.class),
                                shot.child("sent_by").getValue(String.class),
                                Boolean.TRUE.equals(shot.child("unread").getValue(Boolean.class)),
                                shot.child("timestamp").getValue(Long.class)
                        );
                        if (shot.child("base64").exists()) {
                            message.setBase64(shot.child("base64").getValue(String.class));
                        }
                        messageList.add(message);
                    }
                }
                setupRecyclerView(messageList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    /**
     * Sets up the RecyclerView with the chat messages.
     */
    private void setupRecyclerView(List<Message> messageList) {
        messageAdapter = new MessageAdapter(requireContext(), messageList, false);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(requireContext());
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);
        scrollToLastUnreadMessage();
    }

    /**
     * Scrolls the RecyclerView to the last unread message.
     */
    private void scrollToLastUnreadMessage() {
        recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
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
            bitmapToSend = getBitmapFromUri(requireContext(), selectedImage);
            imageView.setImageURI(selectedImage);
        }
    }

    /**
     * Converts a URI to a Bitmap.
     *
     * @param context The context.
     * @param uri The URI of the image.
     * @return The Bitmap representation of the image.
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        InputStream inputStream = null;
        Bitmap bitmap = null;

        try {
            inputStream = contentResolver.openInputStream(uri);
            if (inputStream != null) {
                bitmap = BitmapFactory.decodeStream(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return bitmap;
    }
}
