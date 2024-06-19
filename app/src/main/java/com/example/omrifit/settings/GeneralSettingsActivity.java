package com.example.omrifit.settings;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.omrifit.R;
import com.example.omrifit.classes.UserGeneralInfo;
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
 * GeneralSettingsActivity handles the user's general settings including daily activity,
 * target goals, meal numbers, and security settings.
 */
public class GeneralSettingsActivity extends AppCompatActivity {

    // UI components
    private Spinner spnDailyActivity, spnTarget, spnMealNum, spnExperience, spnWeightTarget;
    private Button btnNext;
    private TextView txtSendToTimings;
    private Switch switchAppSec, switchMealTimings, switchDailyTip;
    private RelativeLayout relativeLayoutWeightTarget, relativeLayoutExperience;

    // Firebase components
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("user_information").child(user.getUid());
    private UserGeneralInfo userNutritionInfo;

    // Member variables
    private boolean isFirstEntrance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_nutrition);

        // Initialize views and components
        initializeViews();

        // Check if it's the user's first entrance
        isFirstEntrance = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getBoolean("first_entrance", false);

        // Setup spinners with options
        setupSpinners();

        // Load current user settings from the database
        loadCurrentDetails();

        // Setup button and switch listeners
        setupButtonListeners();
    }

    /**
     * Initializes the views and components of the activity.
     */
    private void initializeViews() {
        relativeLayoutExperience = findViewById(R.id.r);
        txtSendToTimings = findViewById(R.id.txtSendToTimings);
        spnDailyActivity = findViewById(R.id.spn_daily_activity);
        spnTarget = findViewById(R.id.spn_target);
        spnMealNum = findViewById(R.id.spn_meal_num);
        btnNext = findViewById(R.id.btn_next);
        spnExperience = findViewById(R.id.spn_userexperience);
        spnWeightTarget = findViewById(R.id.spn_weight_target);
        switchAppSec = findViewById(R.id.switchAppSec);
        switchDailyTip = findViewById(R.id.switchDailyTip);
        switchMealTimings = findViewById(R.id.switchMealTimes);
        relativeLayoutWeightTarget = findViewById(R.id.rlv_weight_target);
    }

    /**
     * Sets up the spinners with appropriate options and listeners.
     */
    private void setupSpinners() {
        if (isFirstEntrance) {
            relativeLayoutExperience.setVisibility(View.VISIBLE);
            setSpinnerAdapter(spnExperience, getExperienceOptions());
        }
        setSpinnerAdapter(spnDailyActivity, getDailyActivityOptions());
        setSpinnerAdapter(spnMealNum, getMealNumOptions());
        setSpinnerAdapter(spnTarget, getTargetOptions());

        spnTarget.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateWeightTargetOptions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Sets the adapter for a spinner with the given options.
     *
     * @param spinner The spinner to set the adapter for.
     * @param options The options to display in the spinner.
     */
    private void setSpinnerAdapter(Spinner spinner, ArrayList<String> options) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, options);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /**
     * Sets up listeners for buttons and switches.
     */
    private void setupButtonListeners() {
        btnNext.setOnClickListener(v -> processSettings());
        txtSendToTimings.setOnClickListener(v -> startActivity(new Intent(GeneralSettingsActivity.this, UserTimesActivity.class)));
        switchMealTimings.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                txtSendToTimings.setVisibility(View.VISIBLE);
            } else {
                txtSendToTimings.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Processes the user settings and updates the database.
     */
    private void processSettings() {
        if (isFirstEntrance) {
            setupWorkoutRecommendations();
        }
        getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit().putBoolean("first_entrance", false).apply();
        updateBeginWeightIfTargetChanged();
        saveUserSettings();
    }

    /**
     * Saves the user settings to the database.
     */
    private void saveUserSettings() {
        userNutritionInfo = new UserGeneralInfo(spnTarget.getSelectedItem().toString(),
                spnDailyActivity.getSelectedItem().toString(),
                spnMealNum.getSelectedItemPosition() + 3);
        myRef.child("getDailyTip").setValue(switchDailyTip.isChecked());
        myRef.child("getMealTimes").setValue(switchMealTimings.isChecked());
        handleSecuritySettings();
    }

    /**
     * Handles security settings, ensuring the device has a secure lock screen if required.
     */
    private void handleSecuritySettings() {
        if (switchAppSec.isChecked()) {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if (keyguardManager.isKeyguardSecure()) {
                    myRef.child("isThereScreenLock").setValue(true);
                    updateDatabaseWithUserInfo();
                } else {
                    switchAppSec.setChecked(false);
                    Toast.makeText(GeneralSettingsActivity.this, "Please enable a secure lock screen first.", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            myRef.child("isThereScreenLock").setValue(false);
            updateDatabaseWithUserInfo();
        }
    }

    /**
     * Updates the user information in the database and navigates to the home screen.
     */
    private void updateDatabaseWithUserInfo() {
        myRef.child("userNutritionInfo").setValue(userNutritionInfo);
        myRef.child("userNutritionInfo").child("target_weight").setValue(Integer.parseInt(spnWeightTarget.getSelectedItem().toString()));
        navigateToHome();
    }

    /**
     * Navigates to the home screen.
     */
    private void navigateToHome() {
        Intent intent = new Intent(GeneralSettingsActivity.this, HomePageActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Updates the beginning weight if the target has changed.
     */
    private void updateBeginWeightIfTargetChanged() {
        myRef.child("userNutritionInfo").child("target").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentTarget = snapshot.getValue(String.class);
                if (!spnTarget.getSelectedItem().toString().equals(currentTarget)) {
                    myRef.child("user_profile").child("weight").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot weightSnapshot) {
                            if (weightSnapshot.exists()) {
                                myRef.child("begin_weight").setValue(weightSnapshot.getValue());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(GeneralSettingsActivity.this, "Failed to update beginning weight.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GeneralSettingsActivity.this, "Failed to fetch current target.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sets up workout recommendations based on the user's experience level.
     */
    private void setupWorkoutRecommendations() {
        String experienceLevel = spnExperience.getSelectedItem().toString();
        String split;
        if ("Beginner".equals(experienceLevel))
            split = "A";
        else if ("Intermediate".equals(experienceLevel))
            split = "A,B";
        else
            split = "A,B,C";

        myRef.child("workouts").child("days").child("1").setValue("A");
        myRef.child("workouts").child("days").child("3").setValue("A");
        if (split.contains("A,B")) {
            myRef.child("workouts").child("days").child("2").setValue("B");
            myRef.child("workouts").child("days").child("4").setValue("B");
        }if (split.equals("A,B,C")) {
            myRef.child("workouts").child("days").child("3").setValue("C");
            myRef.child("workouts").child("days").child("6").setValue("C");
        }


        DatabaseReference workoutsRef = database.getReference("recommended_workouts").child(split);
        workoutsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot shot : snapshot.getChildren()) {
                    myRef.child("workouts").child(shot.getKey()).setValue(shot.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GeneralSettingsActivity.this, "Failed to load recommended workouts.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Returns a list of daily activity options.
     *
     * @return A list of daily activity options.
     */
    private ArrayList<String> getDailyActivityOptions() {
        ArrayList<String> options = new ArrayList<>();
        options.add("None");
        options.add("Light");
        options.add("Moderate");
        options.add("Heavy");
        options.add("Very Heavy");
        return options;
    }

    /**
     * Returns a list of meal number options.
     *
     * @return A list of meal number options.
     */
    private ArrayList<String> getMealNumOptions() {
        ArrayList<String> options = new ArrayList<>();
        for (int i = 3; i <= 6; i++) {
            options.add(i + " meals per day");
        }
        return options;
    }

    /**
     * Returns a list of target options.
     *
     * @return A list of target options.
     */
    private ArrayList<String> getTargetOptions() {
        ArrayList<String> options = new ArrayList<>();
        options.add("Mass Gain");
        options.add("Maintenance");
        options.add("Weight Loss");
        return options;
    }

    /**
     * Returns a list of experience level options.
     *
     * @return A list of experience level options.
     */
    private ArrayList<String> getExperienceOptions() {
        ArrayList<String> options = new ArrayList<>();
        options.add("Beginner");
        options.add("Intermediate");
        options.add("Advanced");
        return options;
    }

    /**
     * Loads the current user details from the database and updates the UI.
     */
    private void loadCurrentDetails() {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserGeneralInfo userGeneralInfo = dataSnapshot.child("userNutritionInfo").getValue(UserGeneralInfo.class);
                    if (userGeneralInfo != null) {
                        setSpinnerValue(spnDailyActivity, userGeneralInfo.getDailyActivity());
                        setSpinnerValue(spnTarget, userGeneralInfo.getTarget());
                        spnMealNum.setSelection(userGeneralInfo.getMealNum() - 3);
                        setSpinnerValue(spnExperience, userGeneralInfo.getExperience());

                        // Ensure weight target updates after target is set
                        updateWeightTargetOptions();
                        if (dataSnapshot.child("target_weight").exists()) {
                            setSpinnerValue(spnWeightTarget, dataSnapshot.child("target_weight").getValue().toString());
                        }
                        switchAppSec.setChecked(Boolean.TRUE.equals(dataSnapshot.child("isThereScreenLock").getValue(Boolean.class)));
                        switchDailyTip.setChecked(Boolean.TRUE.equals(dataSnapshot.child("getDailyTip").getValue(Boolean.class)));
                        switchMealTimings.setChecked(Boolean.TRUE.equals(dataSnapshot.child("getMealTimes").getValue(Boolean.class)));
                        txtSendToTimings.setVisibility(switchMealTimings.isChecked() ? View.VISIBLE : View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GeneralSettingsActivity.this, "Failed to load user settings.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the weight target options based on the selected target.
     */
    private void updateWeightTargetOptions() {
        String target = spnTarget.getSelectedItem().toString();
        ArrayList<String> options = new ArrayList<>();

        myRef.child("user_profile").child("weight").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int currentWeight = snapshot.getValue(Integer.class);
                    int change = target.equals("Weight Loss") ? -1 : 1;
                    for (int i = 0; i <= 15; i++) {
                        options.add(String.valueOf(currentWeight + change * i));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(GeneralSettingsActivity.this, R.layout.spinner_item, options);
                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                    spnWeightTarget.setAdapter(adapter);

                    relativeLayoutWeightTarget.setVisibility(target.equals("Maintenance") ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**
     * Sets the value of a spinner to the given value.
     *
     * @param spinner The spinner to set the value for.
     * @param value   The value to set.
     */
    private void setSpinnerValue(Spinner spinner, String value) {
        if (value != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }
}
