package com.example.omrifit.community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.omrifit.R;
import com.example.omrifit.adapters.RvForRequests;
import com.example.omrifit.fragments.HomePageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * The RequestsFragment class handles displaying friend requests in a RecyclerView.
 */
public class RequestsFragment extends Fragment {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("user_information").child(user.getUid()).child("requests");
    private ArrayList<String> requests = new ArrayList<>();
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        recyclerView = view.findViewById(R.id.rv_for_requests);
        setupFirebaseListener();

        return view;
    }

    /**
     * Sets up a Firebase listener to listen for changes in the friend requests.
     */
    private void setupFirebaseListener() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requests.clear();
                for (DataSnapshot shot : snapshot.getChildren()) {
                    String request = shot.getValue(String.class);
                    if (request != null) {
                        requests.add(request);
                    }
                }
                setupRecyclerView(requests);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load requests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sets up the RecyclerView with the list of friend requests.
     *
     * @param requests The list of friend requests.
     */
    private void setupRecyclerView(ArrayList<String> requests) {
        RvForRequests rvForRequests = new RvForRequests(requireContext(), requests);
        recyclerView.setAdapter(rvForRequests);
        LinearLayoutManager llm = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(llm);
    }
}
