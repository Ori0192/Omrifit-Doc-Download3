package com.example.omrifit.sport;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.omrifit.R;
import com.example.omrifit.Recyclerview_Interface;
import com.example.omrifit.adapters.SetsExercisesAdapter;
import com.example.omrifit.classes.Exercise;
import com.example.omrifit.measures.PulseMonitorActivity;
import com.example.omrifit.timer.TimerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * WorkoutFragment is responsible for displaying the workout plan of the day.
 * It interacts with Firebase to fetch and display exercises.
 */
public class WorkoutFragment extends Fragment implements Recyclerview_Interface {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private ArrayList<Exercise> exercises;
    private CircleImageView btnTimer, btnHeartMonitor;
    private RecyclerView recyclerView;
    public static ArrayList<Exercise> todaysWorkout = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout_fragemant, container, false);

        initializeFirebase();
        initializeUI(view);
        setupListeners();

        fetchTodaysWorkout(view);

        return view;
    }

    /**
     * Initialize Firebase instances.
     */
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("user_information").child(user.getUid());
    }

    /**
     * Initialize UI components.
     */
    private void initializeUI(View view) {
        btnTimer = view.findViewById(R.id.btn_timer2);
        btnHeartMonitor = view.findViewById(R.id.btn_heart_monitor2);
        recyclerView = view.findViewById(R.id.recyclerviewforworkoutfragment);
    }

    /**
     * Set up click listeners for UI components.
     */
    private void setupListeners() {
        btnTimer.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TimerActivity.class);
            startActivity(intent);
        });

        btnHeartMonitor.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PulseMonitorActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Fetch today's workout data from Firebase.
     */
    private void fetchTodaysWorkout(View view) {
        myRef.child("today'sWorkout").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                handleWorkoutDataChange(snapshot, view);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    /**
     * Handle changes in workout data from Firebase.
     */
    private void handleWorkoutDataChange(DataSnapshot snapshot, View view) {
        TextView txt = view.findViewById(R.id.todaysWorkout);
        ImageView imageView = view.findViewById(R.id.imageView2);

        if (snapshot.exists()) {
            todaysWorkout = new ArrayList<>();
            for (DataSnapshot shot : snapshot.getChildren()) {
                todaysWorkout.add(shot.getValue(Exercise.class));
            }
            txt.setText("Today's workout: ");
            setupRecyclerView(todaysWorkout);
        } else {
            try {
                Glide.with(requireContext()).asGif().load(R.drawable.workoutloading).into(imageView);
            } catch (Exception ignored) {
            }
            txt.setText("Today's workout: Rest");
            imageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set up RecyclerView with exercises.
     */
    private void setupRecyclerView(ArrayList<Exercise> exercises) {
        SetsExercisesAdapter adapter = new SetsExercisesAdapter(getContext(), exercises, WorkoutFragment.this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    /**
     * Get the current day in number.
     */
    public static int getCurrentDayInNumber() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    @Override
    public void ClickOnce(int position) {
        Gson gson = new Gson();
        ArrayList<Exercise> exercisesList = new ArrayList<>();
        for (int i = 0; i < exercises.get(position).getSets(); i++) {
            exercisesList.add(exercises.get(position));
        }
        String json = gson.toJson(exercisesList);
        Intent intent = new Intent(requireContext(), ExpandSetsActivity.class);
        intent.putExtra("expand_sets", json);
        startActivity(intent);
    }



    @Override
    public void onclick(int position) {
        // No implementation needed
    }
}
