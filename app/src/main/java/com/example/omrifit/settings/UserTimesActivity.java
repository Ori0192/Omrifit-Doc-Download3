package com.example.omrifit.settings;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.omrifit.R;
import com.example.omrifit.adapters.Time_Recycler_View_Adapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * UserTimesActivity manages the user's meal times, displaying them in a RecyclerView.
 */
public class UserTimesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_times);

        // Initialize Firebase Authentication and Database references
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("user_information")
                .child(user.getUid())
                .child("userNutritionInfo")
                .child("meal_num");

        // Retrieve the number of meals from the database and setup the RecyclerView
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int mealNum = snapshot.getValue(Integer.class);
                    ArrayList<String> mealTimes = getItemsToSetTime(mealNum);
                    setupRecyclerView(mealTimes);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserTimesActivity.this, "Failed to load meal times.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Generates a list of meal times based on the number of meals.
     *
     * @param mealNum The number of meals.
     * @return A list of meal times.
     */
    private ArrayList<String> getItemsToSetTime(int mealNum) {
        ArrayList<String> mealTimes = new ArrayList<>();
        for (int i = 0; i < mealNum; i++) {
            mealTimes.add("Meal number: " + (i + 1));
        }
        return mealTimes;
    }

    /**
     * Sets up the RecyclerView with the given list of meal times.
     *
     * @param mealTimes The list of meal times.
     */
    private void setupRecyclerView(ArrayList<String> mealTimes) {
        RecyclerView recyclerView = findViewById(R.id.recyclerviewforworkoutfragment);
        Time_Recycler_View_Adapter adapter = new Time_Recycler_View_Adapter(this, mealTimes);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}

