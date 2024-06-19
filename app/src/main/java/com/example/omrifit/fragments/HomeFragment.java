package com.example.omrifit.fragments;

import static com.example.omrifit.sport.WorkoutFragment.getCurrentDayInNumber;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.omrifit.Chat;
import com.example.omrifit.ChatResponseCallback;
import com.example.omrifit.ChatViewModel;
import com.example.omrifit.R;
import com.example.omrifit.TasksActivity;
import com.example.omrifit.timer.TimerActivity;
import com.example.omrifit.TrainYourBrainActivity;
import com.example.omrifit.classes.Exercise;
import com.example.omrifit.classes.Utility;
import com.example.omrifit.editors.EditorsActivity;
import com.example.omrifit.measures.BodyMeasureActivity;
import com.example.omrifit.measures.PulseMonitorActivity;
import com.example.omrifit.media.GalleryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * The HomeFragment class represents the main screen of the app, providing access to various features.
 */
public class HomeFragment extends Fragment {

    private CircleImageView btn_manager, btn_gallery, btn_tyb, btn_exercises_editor, btn_products_editor, image__user_photo, imageButton_user_rank, btn_heart_monitor, btn_timer;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private TextView txt_progress, textViewDaysInApp,txt_welcome;
    CardView cardViewProggress;
    private ProgressBar progressBar;
    private FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("user_information").child(user.getUid());
    public static ArrayList<Exercise> exercises = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_fragemant, container, false);
        setHasOptionsMenu(true);

        // Initialize UI components
        initializeUIComponents(view);

        // Set user photo
        Utility.setImageUserPhoto(user.getUid(), image__user_photo);

        // Set visibility of progress card based on user target
        setProgressCardVisibility();

        // Set button click listeners
        setButtonClickListeners();

        // Display experience in app
        displayExperience(myRef, textViewDaysInApp, true);

        // Load welcome message and user progress
        loadUserData();

        //
        getProgress();

        return view;
    }

    /**
     * Initializes the UI components.
     */
    private void initializeUIComponents(View view) {
        btn_manager = view.findViewById(R.id.manage_chalnges);
        btn_exercises_editor = view.findViewById(R.id.btn_exercises_editor);
        btn_heart_monitor = view.findViewById(R.id.btn_heart_monitor);
        btn_products_editor = view.findViewById(R.id.btn_products_editor);
        btn_timer = view.findViewById(R.id.btn_timer);
        btn_gallery = view.findViewById(R.id.btn_gallery);
        image__user_photo = view.findViewById(R.id.imageButton_user_photo);
        imageButton_user_rank = view.findViewById(R.id.imageButton_user_rank);
        btn_tyb = view.findViewById(R.id.btn_tyb);
        txt_progress = view.findViewById(R.id.txt_progress);
        progressBar = view.findViewById(R.id.progressBar2);
        textViewDaysInApp = view.findViewById(R.id.textViewDaysInApp);
        cardViewProggress = view.findViewById(R.id.card_view_progress);
        txt_welcome = view.findViewById(R.id.textView3);

    }

    /**
     * Sets the visibility of the progress card based on the user's target.
     */
    private void setProgressCardVisibility() {

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String target = snapshot.child("userNutritionInfo").child("target").getValue(String.class);
                if (target != null) {
                    if (target.equals("Maintenance")) {
                        cardViewProggress.setVisibility(View.GONE);
                    } else {
                        cardViewProggress.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle cancellation or error
            }
        });
    }

    /**
     * Sets click listeners for buttons.
     */
    private void setButtonClickListeners() {
        btn_manager.setOnClickListener(v -> startActivity(new Intent(requireContext(), TasksActivity.class)));
        btn_gallery.setOnClickListener(v -> startActivity(new Intent(requireContext(), GalleryActivity.class)));
        btn_tyb.setOnClickListener(v -> startActivity(new Intent(requireContext(), TrainYourBrainActivity.class)));
        btn_timer.setOnClickListener(v -> startActivity(new Intent(requireContext(), TimerActivity.class)));
        btn_heart_monitor.setOnClickListener(v -> startActivity(new Intent(requireContext(), PulseMonitorActivity.class)));
        btn_exercises_editor.setOnClickListener(v -> startActivity(new Intent(requireContext(), EditorsActivity.class)));
        btn_products_editor.setOnClickListener(v -> startActivity(new Intent(requireContext(), BodyMeasureActivity.class)));
    }

    /**
     * Loads user data including welcome message and progress.
     */
    private void loadUserData() {

        myRef.child("user_profile").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                txt_welcome.setText("Welcome back " + name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle cancellation or error
            }
        });

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String lastUpdateDate = snapshot.child("last_update_date").getValue(String.class);
                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                if (lastUpdateDate == null || !lastUpdateDate.equals(currentDate)) {
                    setTodayWorkout();
                    updateExperienceInApp(myRef);
                    if (Boolean.TRUE.equals(snapshot.child("getDailyTip").getValue(Boolean.class))) {
                        giveDailyTip();
                    }
                    myRef.child("last_update_date").setValue(currentDate);
                }
            }

            private void giveDailyTip() {
                ChatViewModel viewModel = new ChatViewModel();
                viewModel.getResponse("Can you give me an interesting fact about nutrition, healthy lifestyle, gym, or sports?", new ChatResponseCallback() {
                    @Override
                    public void onSuccess(Chat chat) {
                        myRef.child("Omri").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                myRef.child("chat").child(String.valueOf(snapshot.child("chat").getChildrenCount())).setValue(chat.getPrompt());
                                if (snapshot.child("newMessages").exists()) {
                                    myRef.child("Omri").child("newMessages").child("newMessages").setValue(snapshot.getValue(Integer.class) + 1);
                                } else {
                                    myRef.child("Omri").child("newMessages").child("newMessages").setValue(1);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Handle cancellation or error
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        // Handle error
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle cancellation or error
            }
        });
    }

    /**
     * Displays the user's experience in the app.
     */
    public static void displayExperience(DatabaseReference myRef, TextView textView, boolean showDaysInApp) {
        myRef.child("rank").child("experience_in_app").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer experience = snapshot.getValue(Integer.class);
                if (experience == null) {
                    experience = 0; // Start from zero if value does not exist
                }
                if (showDaysInApp) {
                    textView.setText(experience + "\n days \n in app");
                } else {
                    textView.setText(experience + " days in app");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle cancellation or error
            }
        });
    }

    /**
     * Gets the user's progress towards their target.
     */
    public void getProgress() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int targetWeight = 1, weight = 1, beginWeight = 1;
                String target = "";

                if (snapshot.child("user_profile").child("weight").exists()) {
                    weight = snapshot.child("user_profile").child("weight").getValue(Integer.class);
                }
                if (snapshot.child("userNutritionInfo").child("target_weight").exists()) {
                    targetWeight = snapshot.child("userNutritionInfo").child("target_weight").getValue(Integer.class);
                }
                if (snapshot.child("begin_weight").exists()) {
                    beginWeight = snapshot.child("begin_weight").getValue(Integer.class);
                }
                if (snapshot.child("userNutritionInfo").child("target").exists()) {
                    target = snapshot.child("userNutritionInfo").child("target").getValue(String.class);
                }

                if ((weight - targetWeight > 0 && target.equals("Weight Loss"))) {
                    txt_progress.setText(Math.abs(weight - targetWeight) + "KG left to reach the goal!");
                    progressBar.setMax(Math.abs(beginWeight - targetWeight));
                    progressBar.setProgress(Math.abs(beginWeight - weight));
                } else {
                    progressBar.setMax(100);
                    progressBar.setProgress(100);
                    txt_progress.setText("Congratulations! You've reached the goal!");
                }

                if ((weight - targetWeight < 0 && target.equals("Mass Gain"))) {
                    txt_progress.setText(Math.abs(weight - targetWeight) + "KG left to reach the goal!");
                    progressBar.setMax(Math.abs(beginWeight - targetWeight));
                    progressBar.setProgress(Math.abs(beginWeight - weight));
                } else {
                    txt_progress.setText("Congratulations! You've reached the goal!");
                    progressBar.setMax(100);
                    progressBar.setProgress(100);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle cancellation or error
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate your menu resource or modify the menu
        inflater.inflate(R.menu.main_menu, menu);
    }

    /**
     * Sets today's workout for the user.
     */
    public static void setTodayWorkout() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("user_information").child(user.getUid());

        myRef.child("workouts").child("days").child(getCurrentDayInNumber()+"").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String workoutType= snapshot.getValue(String.class);
                myRef.child("workouts").child(workoutType).child("musclesforworkout").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        exercises = new ArrayList<>();
                        for (DataSnapshot shot:snapshot.getChildren()){
                            for (DataSnapshot exerciseSnapshot:shot.child("exercises").getChildren()) {
                                if (exerciseSnapshot.exists()) {
                                    exercises.add(exerciseSnapshot.getValue(Exercise.class));
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle cancellation or error
            }
        });
    }

    /**
     * Updates the user's experience in the app.
     */
    private void updateExperienceInApp(DatabaseReference myRef) {
        myRef.child("rank").child("experience_in_app").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer experience = snapshot.getValue(Integer.class);
                if (experience == null) {
                    experience = 0; // Start from zero if value does not exist
                }
                textViewDaysInApp.setText(experience + "\n days \n in app");
                myRef.child("rank").child("experience_in_app").setValue(experience + 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle cancellation or error
            }
        });
    }
}
