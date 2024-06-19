package com.example.omrifit.measures;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.omrifit.R;
import com.example.omrifit.adapters.Ad_BodyMeasure;
import com.example.omrifit.classes.BodyMeasure;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for managing and displaying body weight measurements.
 */
public class WeightMeasureFragment extends Fragment {

    private RecyclerView recyclerView;
    private Ad_BodyMeasure adapter;
    private List<BodyMeasure> data;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("user_information").child(user.getUid());

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_body_measure, container, false);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_bodtmeasure);
        EditText edt_bodymeasure = view.findViewById(R.id.edt_bodymeasure);
        recyclerView = view.findViewById(R.id.RecyclerView);

        data = new ArrayList<>();
        myRef.child("body_measures").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                data.clear();
                for (DataSnapshot shot : snapshot.getChildren()) {
                    BodyMeasure bodyMeasure = shot.getValue(BodyMeasure.class);
                    if (bodyMeasure != null) {
                        data.add(bodyMeasure);
                    }
                }
                updateRecyclerView(data);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        fab.setOnClickListener(v -> addBodyMeasure(edt_bodymeasure));

        return view;
    }

    /**
     * Adds a new body measure if the input is valid and not already added for the current date.
     *
     * @param edt_bodymeasure EditText containing the user's weight input.
     */
    private void addBodyMeasure(EditText edt_bodymeasure) {
        String currentDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        try {
            int weight = Integer.parseInt(edt_bodymeasure.getText().toString());
            if (data.isEmpty() || !data.get(data.size() - 1).getDate().equals(currentDate)) {
                data.add(new BodyMeasure(currentDate, weight));
                myRef.child("body_measures").setValue(data);
                myRef.child("user_profile").child("weight").setValue(weight);
                edt_bodymeasure.setText("");
                updateRecyclerView(data);
            } else {
                Toast.makeText(getContext(), "You've already updated the weight for today", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter a valid weight", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Updates the RecyclerView with the latest body measures.
     *
     * @param data List of BodyMeasure objects to display.
     */
    private void updateRecyclerView(List<BodyMeasure> data) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new Ad_BodyMeasure(getContext(), data);
        recyclerView.setAdapter(adapter);
    }
}
