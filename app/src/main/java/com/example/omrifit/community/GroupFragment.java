package com.example.omrifit.community;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.omrifit.ChatViewModel;
import com.example.omrifit.R;
import com.example.omrifit.adapters.MessageAdapter;
import com.example.omrifit.adapters.Rv_Adapter_for_user_item;
import com.example.omrifit.classes.Message;
import com.example.omrifit.classes.ProfileInfo;
import com.example.omrifit.fragments.HomePageActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.nl.smartreply.SmartReply;
import com.google.mlkit.nl.smartreply.SmartReplyGenerator;
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult;
import com.google.mlkit.nl.smartreply.TextMessage;

import java.util.ArrayList;

/**
 * Fragment that handles displaying and managing group chats.
 */
public class GroupFragment extends Fragment {
    private static final int REQUEST_PERMISSIONS_CODE = 103;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_GALLERY_PHOTO = 3;

    private RecyclerView recyclerView, rv_group_members;
    private EditText message_edt_text;
    private ArrayList<Message> messageList;
    private MessageAdapter messageAdapter;
    private ArrayList<ProfileInfo> groupMembers = new ArrayList<>();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference groupRef = database.getReference("groups");
    private DatabaseReference myRef = database.getReference("user_information").child(user.getUid()).child("belongToGroup");
    private Button suggestion1, suggestion2, suggestion3, leaveGroup;
    private CardView cd_group;
    private Bitmap bitmapToSend;
    private ImageButton addImageButton;
    private TextView groupName;
    private ConstraintLayout ctl;
    private ImageView imageView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_fragemant, container, false);
        setHasOptionsMenu(true);

        initializeUI(view);
        loadGroup();
        messageList = new ArrayList<>();

        message_edt_text.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                if (message_edt_text.getText().toString().length() > 0) {
                    addToChat(message_edt_text.getText().toString());
                } else {
                    Toast.makeText(requireContext(), "Please enter your query..", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
        return view;
    }

    /**
     * Initializes the UI components of the fragment.
     *
     * @param view The root view of the fragment.
     */
    private void initializeUI(View view) {
        imageView = view.findViewById(R.id.imageViewfirstview);
        rv_group_members = view.findViewById(R.id.rv_group_members);
        ctl = view.findViewById(R.id.ctl_group_members);
        groupName = view.findViewById(R.id.group_name);
        recyclerView = view.findViewById(R.id.recycler_view_search);
        message_edt_text = view.findViewById(R.id.edit_text);
        suggestion1 = view.findViewById(R.id.suggestion1);
        suggestion2 = view.findViewById(R.id.suggestion2);
        suggestion3 = view.findViewById(R.id.suggestion3);
        cd_group = view.findViewById(R.id.cd_group);
        cd_group.setVisibility(View.VISIBLE);
        addImageButton = view.findViewById(R.id.addImageButton);
        leaveGroup = view.findViewById(R.id.leave_group);

        addImageButton.setOnClickListener(v -> setupAddImageButton());
        leaveGroup.setOnClickListener(v -> handleLeaveGroup());
    }

    /**
     * Loads the group information and sets up listeners for group data.
     */
    private void loadGroup() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    groupName.setText(snapshot.getValue(String.class));
                    setupGroupChatListener();
                    setupGroupMembersListener();
                } else {
                    groupName.setText("choose group");
                    setupChooseGroupListener();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading group data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sets up the RecyclerView for displaying group members.
     *
     * @param profileInfos The list of group members' profile information.
     */
    private void setUpRvForGroupMembers(ArrayList<ProfileInfo> profileInfos) {
        Rv_Adapter_for_user_item rvAdapterForUserItem = new Rv_Adapter_for_user_item(R.layout.user_item2, getContext(), profileInfos);
        rv_group_members.setAdapter(rvAdapterForUserItem);
        rv_group_members.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    /**
     * Sets up a listener for changes in the group chat.
     */
    private void setupGroupChatListener() {
        groupRef.child(groupName.getText().toString()).child("chat").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot shot : snapshot.getChildren()) {
                    Message message = new Message(shot.child("message").getValue(String.class),
                            shot.child("sent_by").getValue(String.class),
                            Boolean.TRUE.equals(shot.child("unread").getValue(boolean.class)),
                            shot.child("timestamp").getValue(long.class));
                    if (shot.child("base64").exists()) {
                        message.setBase64(shot.child("base64").getValue(String.class));
                    }
                    messageList.add(message);
                }
                if (!messageList.isEmpty()) {
                    try {
                        suggest(messageList);
                        setupRecyclerView(messageList);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error loading messages", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading chat messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sets up a listener for changes in the group members.
     */
    private void setupGroupMembersListener() {
        cd_group.setOnClickListener(v -> {
            if (ctl.getVisibility() == View.VISIBLE) {
                ctl.setVisibility(View.GONE);
            } else {
                ctl.setVisibility(View.VISIBLE);
                if (!groupName.getText().equals("choose group")) {
                    groupRef.child(groupName.getText().toString()).child("members").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            groupMembers.clear();
                            for (DataSnapshot shot : snapshot.getChildren()) {
                                String profileInfoString = shot.getValue(String.class);
                                loadProfileInfo(profileInfoString);
                            }
                            setUpRvForGroupMembers(groupMembers);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Failed to load group members: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * Sets up a listener for choosing a group.
     */
    private void setupChooseGroupListener() {
        cd_group.setOnClickListener(v -> {
            ArrayList<String> Groups = new ArrayList<>();
            groupRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Groups.clear();
                    for (DataSnapshot shot : snapshot.getChildren()) {
                        String Group = shot.getKey();
                        Groups.add(Group);
                    }
                    if (groupName.getText().toString().equals("choose group")) {
                        showSearchDialog(Groups);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error loading groups", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /**
     * Shows a dialog for searching and selecting a group.
     *
     * @param groups The list of available groups.
     */
    public void showSearchDialog(ArrayList<String> groups) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.search_dialog_layout, null);
        builder.setView(dialogView);

        EditText editTextSearch = dialogView.findViewById(R.id.editTextSearch);
        ListView listView = dialogView.findViewById(R.id.listViewSearch);
        Button createNewGroup = dialogView.findViewById(R.id.create_new_group);
        AlertDialog dialog = builder.create();

        createNewGroup.setOnClickListener(v -> handleCreateNewGroup(editTextSearch, listView, createNewGroup, dialog));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, groups);
        listView.setAdapter(adapter);

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        listView.setOnItemClickListener((adapterView, view, position, id) -> handleGroupSelection(adapter, position, dialog));
        dialog.show();
    }

    /**
     * Handles the creation of a new group.
     *
     * @param editTextSearch The EditText for group name input.
     * @param listView       The ListView for displaying groups.
     * @param createNewGroup The Button for creating a new group.
     * @param dialog         The AlertDialog.
     */
    private void handleCreateNewGroup(EditText editTextSearch, ListView listView, Button createNewGroup, AlertDialog dialog) {
        if (!createNewGroup.getText().toString().equals("create")) {
            listView.setVisibility(View.GONE);
            createNewGroup.setText("create");
            editTextSearch.setHint("enter the group name");
        } else {
            if (!editTextSearch.getText().toString().isEmpty()) {
                groupRef.child(editTextSearch.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            myRef.setValue(editTextSearch.getText().toString());
                            groupRef.child(editTextSearch.getText().toString()).child("members").push().setValue(user.getUid());
                            dialog.dismiss();
                        } else {
                            Toast.makeText(requireContext(), "The group name is already used", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error creating group", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    /**
     * Handles the selection of a group from the list.
     *
     * @param adapter The ArrayAdapter for the ListView.
     * @param position The position of the selected item.
     * @param dialog The AlertDialog.
     */
    private void handleGroupSelection(ArrayAdapter<String> adapter, int position, AlertDialog dialog) {
        String selectedItem = adapter.getItem(position);
        try {
            myRef.setValue(selectedItem);
            groupRef.child(selectedItem).child("members").push().setValue(user.getUid());
        } catch (Exception ignored) {
        }
        dialog.dismiss();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    /**
     * Adds a message to the chat.
     *
     * @param response The message text.
     */
    public void addToChat(String response) {
        ChatViewModel viewModel = new ChatViewModel();
        Message message = new Message(response, user.getUid());
        if (bitmapToSend != null) {
            message.setBase64(viewModel.bitmapToBase64(bitmapToSend));
            bitmapToSend = null;
            imageView.setImageBitmap(null);
        }
        groupRef.child(groupName.getText().toString()).child("chat").child(String.valueOf(messageList.size())).setValue(message);
        clearMessageInput();
    }

    /**
     * Loads the profile information of a user.
     *
     * @param userId The ID of the user.
     */
    private void loadProfileInfo(String userId) {
        DatabaseReference userProfileRef = database.getReference("user_information").child(userId).child("user_profile");
        userProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ProfileInfo profileInfo = snapshot.getValue(ProfileInfo.class);
                if (profileInfo != null) {
                    groupMembers.add(profileInfo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sets up the RecyclerView for displaying messages.
     *
     * @param messageList The list of messages.
     */
    public void setupRecyclerView(ArrayList<Message> messageList) {
        messageAdapter = new MessageAdapter(requireContext(), messageList, true);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(new HomePageActivity());
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);
        suggest(messageList);
        scrollToLastUnreadMessage();
    }

    /**
     * Scrolls to the last unread message in the chat.
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
     * Generates and displays smart reply suggestions based on the conversation.
     *
     * @param messageList The list of messages in the conversation.
     */
    private void suggest(ArrayList<Message> messageList) {
        ArrayList<TextMessage> conversation = new ArrayList<>();
        for (Message message : messageList) {
            conversation.add(message.getTextMessage());
        }
        SmartReplyGenerator smartReply = SmartReply.getClient();
        smartReply.suggestReplies(conversation)
                .addOnSuccessListener(result -> {
                    if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                        suggestion1.setText(result.getSuggestions().get(0).getText());
                        suggestion2.setText(result.getSuggestions().get(1).getText());
                        suggestion3.setText(result.getSuggestions().get(2).getText());
                    }
                });
    }

    /**
     * Sets up the button for adding images to the chat.
     */
    private void setupAddImageButton() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Image From");
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
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO);
    }

    /**
     * Clears the message input field.
     */
    public void clearMessageInput() {
        message_edt_text.setText("");
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

    /**
     * Handles the action of leaving the group.
     */
    private void handleLeaveGroup() {
        myRef.setValue(null);
        groupRef.child(groupName.getText().toString()).child("members").orderByValue().equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to leave group", Toast.LENGTH_SHORT).show();
            }
        });
        ctl.setVisibility(View.GONE);
        messageAdapter = new MessageAdapter(requireContext(), new ArrayList<>(), true);
        recyclerView.setAdapter(messageAdapter);
    }
}
