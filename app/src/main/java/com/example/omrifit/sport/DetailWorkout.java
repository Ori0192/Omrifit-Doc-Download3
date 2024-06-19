package com.example.omrifit.sport;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.omrifit.R;
import com.example.omrifit.adapters.MuscleAdapter;
import com.example.omrifit.classes.Muscle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * DetailWorkout Activity displays and manages the details of a specific workout,
 * including the days of the week it is scheduled for and the muscles involved.
 */
public class DetailWorkout extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("user_information").child(user.getUid()).child("workouts");

    private ArrayList<Muscle> muscles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.f);

        TextView txtWorkoutName = findViewById(R.id.txtWorkoutName);
        String type = getIntent().getStringExtra("type");


        myRef.child(type).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txtWorkoutName.setText(snapshot.child("name").getValue().toString());
                 txtWorkoutName.setText(snapshot.getKey());
                for (DataSnapshot shot : snapshot.child("musclesforworkout").getChildren()) {
                    Muscle muscle = shot.getValue(Muscle.class);
                    muscles.add(muscle);
                }
                setUpRv(muscles, type);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    /**
     * Sets up the RecyclerView with the list of muscles for the workout.
     *
     * @param muscles The list of muscles.
     * @param type    The type of workout.
     */
    public void setUpRv(ArrayList<Muscle> muscles, String type) {
        RecyclerView recyclerView = findViewById(R.id.recyclerviewforworkoutfragment);
        if (muscles.size() < 16) {
            muscles.add(new Muscle("add new muscle"));
        }
        MuscleAdapter adapter = new MuscleAdapter(DetailWorkout.this, muscles, type);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
