package com.example.omrifit.adapters;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.omrifit.R;
import com.example.omrifit.classes.Exercise;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * RecyclerView Adapter for displaying a list of Exercise items.
 */
public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private final ArrayList<Exercise> exerciseList;
    private final Context context;
    private final int muscleposition;
    private final String workout_type;
    private final DatabaseReference myRef;

    /**
     * Constructor for ExerciseAdapter.
     *
     * @param context        The context of the calling activity.
     * @param exerciseList   The list of Exercise objects to be displayed.
     * @param workout_type   The type of workout.
     * @param muscleposition The position of the muscle group in the workout.
     */
    public ExerciseAdapter(Context context, ArrayList<Exercise> exerciseList, String workout_type, int muscleposition) {
        this.context = context;
        this.exerciseList = exerciseList;
        this.workout_type = workout_type;
        this.muscleposition = muscleposition;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        this.myRef = FirebaseDatabase.getInstance().getReference("user_information")
                .child(user.getUid()).child("workouts").child(workout_type)
                .child("musclesforworkout").child(String.valueOf(muscleposition)).child("exercises");
    }

    /**
     * Creates and inflates the view holder.
     *
     * @param parent   The parent view group.
     * @param viewType The view type.
     * @return A new ExerciseViewHolder instance.
     */
    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    /**
     * Binds the data to the view holder.
     *
     * @param holder   The ExerciseViewHolder instance.
     * @param position The position of the current item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise currentExercise = exerciseList.get(position);
        holder.tvExerciseName.setText(currentExercise.getName());
        holder.tvSets.setText(String.valueOf(currentExercise.getSets()));

        setupSetEditText(holder, position);
        setupDeleteButton(holder, position);
    }

    /**
     * Configures the EditText for sets to handle user input and updates the database accordingly.
     *
     * @param holder   The ExerciseViewHolder instance.
     * @param position The position of the current item in the list.
     */
    private void setupSetEditText(ExerciseViewHolder holder, int position) {
        holder.tvSets.setOnClickListener(v -> {
            holder.tvSets.setFocusableInTouchMode(true);
            holder.tvSets.setCursorVisible(true);
            showKeyboard(holder.tvSets);
        });

        holder.tvSets.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                try {
                    int sets = Integer.parseInt(holder.tvSets.getText().toString());
                    if (sets >= 2 && sets <= 6 && !holder.tvSets.getText().toString().isEmpty()) {
                        myRef.child(String.valueOf(position)).child("sets").setValue(sets);
                        holder.tvSets.clearFocus();
                        hideKeyboard(holder.tvSets);
                        return true;
                    } else {
                        Toast.makeText(context, "Enter value between 2 and 6", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Invalid number format", Toast.LENGTH_SHORT).show();
                }
            }
            return false;
        });
    }

    /**
     * Configures the delete button to handle item removal and updates the database accordingly.
     *
     * @param holder   The ExerciseViewHolder instance.
     * @param position The position of the current item in the list.
     */
    private void setupDeleteButton(ExerciseViewHolder holder, int position) {
        holder.itemView.setOnLongClickListener(v -> {
            if (position < getItemCount() - 1) {
                if (getItemCount() > 2) {
                    exerciseList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, exerciseList.size());
                    myRef.setValue(exerciseList);
                } else {
                    Toast.makeText(context, "At least two exercises required", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        });
    }

    /**
     * Shows the keyboard.
     *
     * @param view The view to attach the keyboard to.
     */
    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Hides the keyboard.
     *
     * @param view The view to detach the keyboard from.
     */
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Returns the size of the data list.
     *
     * @return The size of the data list.
     */
    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    /**
     * ViewHolder class for holding the view elements of each item.
     */
    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        final TextView tvExerciseName;
        final EditText tvSets;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The view of the item.
         */
        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tv_exercise_name);
            tvSets = itemView.findViewById(R.id.etv_sets);
        }
    }
}

