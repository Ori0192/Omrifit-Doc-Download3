package com.example.omrifit.adapters;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.omrifit.R;
import com.example.omrifit.Recyclerview_Interface;
import com.example.omrifit.classes.Exercise;
import com.example.omrifit.fragments.HomePageActivity;
import com.example.omrifit.sport.ExpandSetsActivity;
import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * RecyclerView Adapter for displaying a list of exercises with their sets.
 */
public class SetsExercisesAdapter extends RecyclerView.Adapter<SetsExercisesAdapter.MyViewHolder> {
    private final Context context;
    private final ArrayList<Exercise> exercises;
    private final Recyclerview_Interface recyclerviewInterface;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private long lastClickTime = 0;

    /**
     * Constructor for initializing the adapter with necessary data.
     *
     * @param context               The context of the calling activity.
     * @param exercises             The list of Exercise objects to be displayed.
     * @param recyclerviewInterface The interface for handling RecyclerView item clicks.
     */
    public SetsExercisesAdapter(Context context, ArrayList<Exercise> exercises, Recyclerview_Interface recyclerviewInterface) {
        this.context = context;
        this.exercises = exercises;
        this.recyclerviewInterface = recyclerviewInterface;
    }

    /**
     * Creates and inflates the view holder.
     *
     * @param parent   The parent view group.
     * @param viewType The view type.
     * @return A new MyViewHolder instance.
     */
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_setsexercises_row, parent, false);
        return new MyViewHolder(view, recyclerviewInterface);
    }

    /**
     * Binds the data to the view holder.
     *
     * @param holder   The MyViewHolder instance.
     * @param position The position of the current item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Exercise currentExercise = exercises.get(position);
        if (currentExercise.getSets() == 0) {
            holder.img_done.setImageResource(R.drawable.ic_check_mark);
        }
        holder.txt_sets.setText(currentExercise.getSets() + " sets left");
        holder.txt_name.setText(currentExercise.getName());
        holder.txt_category.setText(currentExercise.getTarget());

        int estimatedMinutes = 3 * currentExercise.getSets();
        int estimatedCaloriesBurn = 5 * currentExercise.getSets();
        holder.txt_estimated.setText(estimatedMinutes + "min, " + estimatedCaloriesBurn + "kcal");

        holder.itemView.setOnClickListener(v -> handleItemClick(v, position));
        holder.txt_askOmri.setOnClickListener(v -> handleAskOmriClick(currentExercise));
    }

    /**
     * Handles item click events with debounce logic to distinguish between single and double clicks.
     *
     * @param v        The view that was clicked.
     * @param position The position of the clicked item.
     */
    private void handleItemClick(View v, int position) {
        if (System.currentTimeMillis() - lastClickTime < 200) {
            handler.removeCallbacks(runnable); // Remove the scheduled runnable for single click
            // Handle double click if needed
        } else {
            runnable = () -> onClick(position); // Handle single click
            handler.postDelayed(runnable, 200); // Schedule the runnable after 200 milliseconds
        }
        lastClickTime = System.currentTimeMillis();
    }

    /**
     * Handles single click events on items.
     *
     * @param position The position of the clicked item.
     */
    private void onClick(int position) {
        Intent intent = new Intent(context, ExpandSetsActivity.class);
        Gson gson = new Gson();
        String json = gson.toJson(exercises.get(position));
        intent.putExtra("expand_sets", json);
        intent.putExtra("position", position);
        startActivity(context, intent, new Bundle());
    }

    /**
     * Handles click events on the "Ask Omri" text.
     *
     * @param exercise The Exercise object for which the user wants more information.
     */
    private void handleAskOmriClick(Exercise exercise) {
        Intent intent = new Intent(context, HomePageActivity.class);
        intent.putExtra("toOmriChat", true);
        intent.putExtra("exercise", "Hi can you explain to me how to perform " + exercise.getName() + " properly");
        startActivity(context, intent, null);
    }

    /**
     * Returns the size of the data list.
     *
     * @return The size of the data list.
     */
    @Override
    public int getItemCount() {
        return exercises.size();
    }

    /**
     * ViewHolder class for holding the view elements of each item.
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        final TextView txt_session, txt_name, txt_estimated, txt_sets, txt_category, txt_askOmri;
        final ImageView img_done;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView               The view of the item.
         * @param recyclerviewInterface The interface for handling RecyclerView item clicks.
         */
        public MyViewHolder(@NonNull View itemView, Recyclerview_Interface recyclerviewInterface) {
            super(itemView);
            txt_session = itemView.findViewById(R.id.txt_session);
            txt_name = itemView.findViewById(R.id.txt_name);
            txt_estimated = itemView.findViewById(R.id.txt_estimated);
            img_done = itemView.findViewById(R.id.img_done);
            txt_sets = itemView.findViewById(R.id.txt_sets_for_rv);
            txt_category = itemView.findViewById(R.id.txt_category);
            txt_askOmri = itemView.findViewById(R.id.txt_askOmri);
        }
    }
}
