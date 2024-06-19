package com.example.omrifit.adapters;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.omrifit.R;
import com.example.omrifit.classes.Exercise;
import com.example.omrifit.classes.Muscle;
import com.example.omrifit.sport.DetailWorkout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying a list of Muscle objects.
 */
public class MuscleAdapter extends RecyclerView.Adapter<MuscleAdapter.MuscleViewHolder> {

    private final List<Muscle> muscleList;
    private final Context context;
    private final DatabaseReference myRef;
    private final String workout_type;
    private EditText etSearchExercise, edt_sets;
    private ListView lvSearchResults;
    private Exercise selectedExercise = new Exercise();

    /**
     * Constructor for MuscleAdapter.
     *
     * @param context     The context of the calling activity.
     * @param muscleList  The list of Muscle objects to be displayed.
     * @param workout_type The type of workout.
     */
    public MuscleAdapter(Context context, List<Muscle> muscleList, String workout_type) {
        this.context = context;
        this.muscleList = muscleList;
        this.workout_type = workout_type;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        this.myRef = FirebaseDatabase.getInstance().getReference("user_information")
                .child(user.getUid()).child("workouts").child(workout_type)
                .child("musclesforworkout");
    }

    /**
     * Creates and inflates the view holder.
     *
     * @param parent   The parent view group.
     * @param viewType The view type.
     * @return A new MuscleViewHolder instance.
     */
    @NonNull
    @Override
    public MuscleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_muscle, parent, false);
        return new MuscleViewHolder(view);
    }

    /**
     * Binds the data to the view holder.
     *
     * @param holder   The MuscleViewHolder instance.
     * @param position The position of the current item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull MuscleViewHolder holder, int position) {
        Muscle currentMuscle = muscleList.get(position);
        if (currentMuscle.getMusclename().equals("add new muscle")) {
            setupAddNewMuscleView(holder, position);
        } else {
            setupExistingMuscleView(holder, currentMuscle, position);
        }
    }

    /**
     * Sets up the view for adding a new muscle.
     *
     * @param holder   The MuscleViewHolder instance.
     * @param position The position of the current item in the list.
     */
    private void setupAddNewMuscleView(MuscleViewHolder holder, int position) {
        holder.tvMuscleName.setText("add new muscle");
        holder.tvMuscleName.setGravity(Gravity.CENTER);
        holder.btnDeleteMuscle.setVisibility(View.GONE);
        holder.itemView.setOnClickListener(v -> {
            holder.cardViewadd.setVisibility(View.VISIBLE);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.simple_spinner_dropdown_item, getMuscles());
            holder.spn_add.setAdapter(adapter);

            holder.btn_add_muscle.setOnClickListener(v1 -> addNewMuscle(holder, position));
        });
    }

    /**
     * Adds a new muscle to the workout.
     *
     * @param holder   The MuscleViewHolder instance.
     * @param position The position of the current item in the list.
     */
    private void addNewMuscle(MuscleViewHolder holder, int position) {
        boolean isDoesNotExist = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            isDoesNotExist = muscleList.stream().noneMatch(muscle -> muscle.getMusclename().equals(holder.spn_add.getSelectedItem().toString()));
        }

        if (isDoesNotExist) {
            DatabaseReference exercisesRef = FirebaseDatabase.getInstance().getReference("exercises");
            exercisesRef.child(holder.spn_add.getSelectedItem().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ArrayList<Exercise> exercisesForCategory = new ArrayList<>();
                    for (DataSnapshot targetSnapshot : dataSnapshot.getChildren()) {
                        Exercise exercise = targetSnapshot.getValue(Exercise.class);
                        if (exercise != null) {
                            exercisesForCategory.add(exercise);
                        }
                    }
                    muscleList.remove(getItemCount() - 1);
                    muscleList.add(new Muscle(holder.spn_add.getSelectedItem().toString(), exercisesForCategory));
                    updateMuscleListInDatabase();
                    muscleList.add(new Muscle("add new muscle"));
                    notifyItemChanged(position);
                    notifyItemInserted(position);
                    holder.cardViewadd.setVisibility(View.GONE);
                    restartDetailWorkoutActivity();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle the error
                }
            });
        } else {
            Toast.makeText(context, "This exercise is already registered in this workout", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sets up the view for an existing muscle.
     *
     * @param holder         The MuscleViewHolder instance.
     * @param currentMuscle  The current Muscle object.
     * @param position       The position of the current item in the list.
     */
    private void setupExistingMuscleView(MuscleViewHolder holder, Muscle currentMuscle, int position) {
        holder.tvMuscleName.setText(currentMuscle.getMusclename());

        LinearLayoutManager layoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        holder.rvExercises.setLayoutManager(layoutManager);

        ExerciseAdapter exerciseAdapter = new ExerciseAdapter(context, currentMuscle.getExercises(), workout_type, position);
        holder.rvExercises.setAdapter(exerciseAdapter);

        holder.tvMuscleName.setOnClickListener(v -> toggleExercisesVisibility(holder));
        holder.btnDeleteMuscle.setOnClickListener(v -> deleteMuscle(position));
        holder.btn_add_exercise.setOnClickListener(v -> showAddExerciseDialog(currentMuscle, position, exerciseAdapter));
    }

    /**
     * Toggles the visibility of the exercises in the muscle.
     *
     * @param holder The MuscleViewHolder instance.
     */
    private void toggleExercisesVisibility(MuscleViewHolder holder) {
        if (holder.rvExercises.getVisibility() == View.GONE) {
            holder.rvExercises.setVisibility(View.VISIBLE);
            holder.btn_add_exercise.setVisibility(View.VISIBLE);
        } else {
            holder.rvExercises.setVisibility(View.GONE);
            holder.btn_add_exercise.setVisibility(View.GONE);
        }
    }

    /**
     * Deletes a muscle from the workout.
     *
     * @param position The position of the muscle to be deleted.
     */
    private void deleteMuscle(int position) {
        if (getItemCount() == 1) {
            Toast.makeText(context, "You must work on at least one muscle group in a workout", Toast.LENGTH_SHORT).show();
        } else {
            muscleList.remove(position);
            notifyItemRemoved(position);
            if (position == getItemCount()) {
                notifyItemChanged(position - 1);
            }
            myRef.child(String.valueOf(position)).removeValue();
            restartDetailWorkoutActivity();
        }
    }

    /**
     * Shows the dialog for adding an exercise to the muscle.
     *
     * @param currentMuscle   The current Muscle object.
     * @param position        The position of the muscle.
     * @param exerciseAdapter The ExerciseAdapter for updating the exercises.
     */
    private void showAddExerciseDialog(Muscle currentMuscle, int position, ExerciseAdapter exerciseAdapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_exercise, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        edt_sets = dialogView.findViewById(R.id.edt_sets);
        etSearchExercise = dialogView.findViewById(R.id.editTextsearch);
        lvSearchResults = dialogView.findViewById(R.id.listviewaddexercise);
        Button btn_search = dialogView.findViewById(R.id.btn_search);
        Button btn_next = dialogView.findViewById(R.id.btn_next_addexercise);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, new ArrayList<>());
        lvSearchResults.setAdapter(adapter);

        btn_search.setOnClickListener(v -> searchExercises(currentMuscle.getMusclename()));
        lvSearchResults.setOnItemClickListener((parent, view, position1, id) -> selectExerciseFromSearchResults(parent, view, position1, currentMuscle));
        btn_next.setOnClickListener(v -> addSelectedExerciseToMuscle(currentMuscle, position, exerciseAdapter, dialog));
    }

    /**
     * Searches for exercises matching the search term and updates the search results list.
     *
     * @param muscleName The name of the muscle being searched.
     */
    private void searchExercises(String muscleName) {
        String search = etSearchExercise.getText().toString();
        if (search.isEmpty()) {
            etSearchExercise.setError("You must enter a product to search");
        } else {
            filterData(search, muscleName);
        }
    }

    /**
     * Selects an exercise from the search results.
     *
     * @param parent       The parent view group.
     * @param view         The view being clicked.
     * @param position     The position of the item being clicked.
     * @param currentMuscle The current Muscle object.
     */
    private void selectExerciseFromSearchResults(ViewGroup parent, View view, int position, Muscle currentMuscle) {
        String selectedItem = ((TextView) view).getText().toString();
        boolean isExistInWorkout = currentMuscle.getExercises().stream().anyMatch(exercise -> exercise.getName().equals(selectedItem));

        if (!isExistInWorkout) {
            etSearchExercise.setText(selectedItem);
            lvSearchResults.setVisibility(View.GONE);

            Toast.makeText(context, selectedItem + " selected successfully", Toast.LENGTH_SHORT).show();

            DatabaseReference exercisesRef = FirebaseDatabase.getInstance().getReference("exercises");
            exercisesRef.child(currentMuscle.getMusclename()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot targetSnapshot : dataSnapshot.getChildren()) {
                        Exercise exercise = targetSnapshot.getValue(Exercise.class);
                        if (exercise != null && exercise.getName().equals(selectedItem)) {
                            selectedExercise = exercise;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle the error
                }
            });
        } else {
            Toast.makeText(context, selectedItem + " already exists in the workout", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Adds the selected exercise to the muscle.
     *
     * @param currentMuscle   The current Muscle object.
     * @param position        The position of the muscle.
     * @param exerciseAdapter The ExerciseAdapter for updating the exercises.
     * @param dialog          The dialog for adding the exercise.
     */
    private void addSelectedExerciseToMuscle(Muscle currentMuscle, int position, ExerciseAdapter exerciseAdapter, AlertDialog dialog) {
        if (etSearchExercise.getText().toString().isEmpty() || selectedExercise == null) {
            Toast.makeText(context, "Please select an exercise", Toast.LENGTH_SHORT).show();
        } else {
            int setsNum = Integer.parseInt(edt_sets.getText().toString() + "0");
            if (setsNum != 0) {
                selectedExercise.setSets(setsNum);
                currentMuscle.getExercises().add(selectedExercise);
                myRef.child(String.valueOf(position)).child("exercises").setValue(currentMuscle.getExercises());
                exerciseAdapter.notifyDataSetChanged();
                Toast.makeText(context, "Exercise added successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Select a number of sets", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Filters the data based on the search term and updates the search results list.
     *
     * @param search The search term.
     * @param target The target muscle group.
     */
    private void filterData(String search, String target) {
        DatabaseReference exercisesRef = FirebaseDatabase.getInstance().getReference("exercises");
        exercisesRef.child(target).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Exercise> exercisesForCategory = new ArrayList<>();
                for (DataSnapshot targetSnapshot : dataSnapshot.getChildren()) {
                    Exercise exercise = targetSnapshot.getValue(Exercise.class);
                    if (exercise != null && exercise.getName().toLowerCase().contains(search.toLowerCase())) {
                        exercisesForCategory.add(exercise);
                    }
                }
                updateSearchResultsList(exercisesForCategory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
            }
        });
    }

    /**
     * Updates the search results list based on the filtered exercises.
     *
     * @param exercises The filtered list of exercises.
     */
    private void updateSearchResultsList(List<Exercise> exercises) {
        List<String> filteredData = new ArrayList<>();
        for (Exercise exercise : exercises) {
            filteredData.add(exercise.getName());
        }

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) lvSearchResults.getAdapter();
        adapter.clear();
        adapter.addAll(filteredData);
        adapter.notifyDataSetChanged();

        lvSearchResults.setVisibility(filteredData.isEmpty() ? View.GONE : View.VISIBLE);

        if (filteredData.isEmpty()) {
            etSearchExercise.setError("No product found matching the search");
        }
    }

    /**
     * Updates the muscle list in the database.
     */
    private void updateMuscleListInDatabase() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("user_information").child(user.getUid());
        myRef.child("workouts").child(workout_type).child("musclesforworkout").setValue(muscleList);
    }

    /**
     * Restarts the DetailWorkout activity.
     */
    private void restartDetailWorkoutActivity() {
        Intent intent = new Intent(context, DetailWorkout.class);
        intent.putExtra("type", workout_type);
        startActivity(context, intent, null);
        finishActivity();
    }

    /**
     * Finishes the current activity.
     */
    private void finishActivity() {
        if (context instanceof Activity) {
            ((Activity) context).finish();
        } else {
            throw new IllegalStateException("Context used in adapter is not an instance of Activity");
        }
    }

    /**
     * Returns a list of muscle groups.
     *
     * @return A list of muscle groups.
     */
    private List<String> getMuscles() {
        List<String> items = new ArrayList<>();
        items.add("row exercises");
        items.add("pull exercises");
        items.add("upper chest exercises");
        items.add("middle chest exercises");
        items.add("lower chest exercises");
        items.add("front shoulder exercises");
        items.add("middle shoulder exercises");
        items.add("back shoulder exercises");
        items.add("biceps exercises");
        items.add("triceps exercises");
        items.add("forearms exercises");
        items.add("abs exercises");
        items.add("hamstring exercises");
        items.add("quads exercises");
        items.add("calves exercises");
        return items;
    }

    /**
     * Returns the size of the muscle list.
     *
     * @return The size of the muscle list.
     */
    @Override
    public int getItemCount() {
        return muscleList != null ? muscleList.size() : 0;
    }

    /**
     * ViewHolder class for holding the view elements of each item.
     */
    public static class MuscleViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMuscleName;
        final RecyclerView rvExercises;
        final ImageButton btnDeleteMuscle;
        final Spinner spn_add;
        final CardView btn_add_exercise;
        final CardView cardViewadd;
        final Button btn_add_muscle;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The view of the item.
         */
        public MuscleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMuscleName = itemView.findViewById(R.id.tv_muscle_name);
            rvExercises = itemView.findViewById(R.id.rv_exercises);
            btnDeleteMuscle = itemView.findViewById(R.id.btnDeleteMuscle);
            cardViewadd = itemView.findViewById(R.id.cardviewadd);
            btn_add_exercise = itemView.findViewById(R.id.btn_add_exercise);
            spn_add = itemView.findViewById(R.id.spinneradd);
            btn_add_muscle = itemView.findViewById(R.id.buttonaddmuscle);
        }
    }
}
