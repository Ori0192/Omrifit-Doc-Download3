package com.example.omrifit.community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.omrifit.R;
import com.example.omrifit.adapters.Rv_Adapter_for_user_item;
import com.example.omrifit.classes.ProfileInfo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Fragment that handles displaying and searching for friends.
 */
public class FriendsFragment extends Fragment {
    private RecyclerView recyclerViewFriends, recyclerViewSearch;
    private EditText searchEditText;
    private Rv_Adapter_for_user_item adapterFriends, adapterSearch;
    private DatabaseReference myRef;
    private ArrayList<ProfileInfo> friendsList = new ArrayList<>();
    private ArrayList<ProfileInfo> searchList = new ArrayList<>();
    private View searchPage;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        initializeUI(view);
        loadFriends();
        return view;
    }

    /**
     * Initializes the UI components of the fragment.
     *
     * @param view The root view of the fragment.
     */
    private void initializeUI(View view) {
        recyclerViewFriends = view.findViewById(R.id.recycler_view);
        recyclerViewSearch = view.findViewById(R.id.recycler_view_search);
        searchEditText = view.findViewById(R.id.search_edit_text);
        searchPage = view.findViewById(R.id.search_page);

        FloatingActionButton fab = view.findViewById(R.id.floatingActionButton2);
        fab.setOnClickListener(v -> searchPage.setVisibility(View.VISIBLE));

        adapterFriends = new Rv_Adapter_for_user_item(R.layout.user_item, getContext(), friendsList);
        recyclerViewFriends.setAdapter(adapterFriends);
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(getContext()));

        adapterSearch = new Rv_Adapter_for_user_item(R.layout.user_item, getContext(), searchList);
        recyclerViewSearch.setAdapter(adapterSearch);
        recyclerViewSearch.setLayoutManager(new LinearLayoutManager(getContext()));

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchEditText.getText().toString());
                return true;
            }
            return false;
        });
    }

    /**
     * Loads the list of friends from Firebase and populates the friends list.
     */
    private void loadFriends() {
        if (user != null) {
            myRef = FirebaseDatabase.getInstance().getReference("user_information");
            DatabaseReference friendsRef = myRef.child(user.getUid()).child("friends");
            friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    friendsList.clear();
                    for (DataSnapshot shot : snapshot.getChildren()) {
                        String friendId = shot.getValue(String.class);
                        if (friendId != null) {
                            loadProfileInfo(friendId, adapterFriends, friendsList);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error loading friends", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Performs a search for users based on the query string.
     *
     * @param query The search query string.
     */
    private void performSearch(String query) {
        if (!query.isEmpty()) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("user_information");
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    searchList.clear();
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        ProfileInfo profileInfo = userSnapshot.child("user_profile").getValue(ProfileInfo.class);
                        if (profileInfo != null && profileInfo.getName() != null && profileInfo.getId().toLowerCase().contains(query.toLowerCase()) && !profileInfo.getId().equals(user.getUid())) {
                            searchList.add(profileInfo);
                        }
                    }
                    if (searchList.isEmpty()) {
                        Toast.makeText(getContext(), "No users found with \"" + query + "\"", Toast.LENGTH_SHORT).show();
                    } else {
                        adapterSearch.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error in search", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Please enter a search query", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Loads profile information for a user based on their user ID and adds it to the specified list.
     *
     * @param userId  The ID of the user to load.
     * @param adapter The adapter to notify of data changes.
     * @param list    The list to add the user's profile information to.
     */
    private void loadProfileInfo(String userId, Rv_Adapter_for_user_item adapter, ArrayList<ProfileInfo> list) {
        DatabaseReference userProfileRef = myRef.child(userId).child("user_profile");
        userProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ProfileInfo profileInfo = snapshot.getValue(ProfileInfo.class);
                if (profileInfo != null) {
                    list.add(profileInfo);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
