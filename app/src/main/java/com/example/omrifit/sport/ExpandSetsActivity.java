package com.example.omrifit.sport;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.omrifit.R;
import com.example.omrifit.Recyclerview_Interface;
import com.example.omrifit.adapters.Exercise_Recyclerview_Adapter;
import com.example.omrifit.classes.Exercise;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * ExpandSetsActivity handles the display and interaction with the expanded sets of a workout exercise.
 */
public class ExpandSetsActivity extends AppCompatActivity implements Recyclerview_Interface {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("user_information").child(user.getUid());

    private int positionOfParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_workout_fragemant);

        String jsonArray = getIntent().getStringExtra("expand_sets");
        positionOfParent = getIntent().getIntExtra("position", 0);
        Type listType = new TypeToken<Exercise>() {}.getType();
        Gson gson = new Gson();
        Exercise exerciseList = gson.fromJson(jsonArray, listType);

        convertToSet(exerciseList);
    }

    /**
     * Converts the exercise to a list of sets and initializes the RecyclerView.
     *
     * @param exercise The exercise to be converted.
     */
    public void convertToSet(Exercise exercise) {
        ArrayList<Exercise> exercises = new ArrayList<>();
        for (int i = 0; i < exercise.getSets(); i++) {
            exercises.add(exercise);
        }
        Toast.makeText(ExpandSetsActivity.this, "" + exercise.getSets(), Toast.LENGTH_SHORT).show();
        setupRecyclerView(exercises);
    }

    /**
     * Sets up the RecyclerView with the list of exercises and configures swipe to delete functionality.
     *
     * @param exercises The list of exercises to be displayed.
     */
    private void setupRecyclerView(ArrayList<Exercise> exercises) {
        RecyclerView recyclerView = findViewById(R.id.recyclerviewforworkoutfragment);
        Exercise_Recyclerview_Adapter adapter = new Exercise_Recyclerview_Adapter(ExpandSetsActivity.this, exercises, ExpandSetsActivity.this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ExpandSetsActivity.this));

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // We don't want to handle move in this example.
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition(); // Get the swiped item position.
                adapter.removeItem(position); // Remove the item from the adapter's data set.
                myRef.child("today'sWorkout").child(String.valueOf(positionOfParent))
                        .child("sets").setValue(adapter.getItemCount());
            }
        };

        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(recyclerView);
    }

    @Override
    public void ClickOnce(int position) {
        // Handle item click event
    }

    @Override
    public void onclick(int position) {
        // Handle click event
    }
}
