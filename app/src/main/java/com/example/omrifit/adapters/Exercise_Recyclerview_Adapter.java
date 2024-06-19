package com.example.omrifit.adapters;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * RecyclerView Adapter for displaying a list of Exercise items.
 */
public class Exercise_Recyclerview_Adapter extends RecyclerView.Adapter<Exercise_Recyclerview_Adapter.MyViewHolder> {
    private final Context context;
    private final ArrayList<Exercise> exercises;
    private final Recyclerview_Interface recyclerviewInterface;

    /**
     * Constructor for Exercise_Recyclerview_Adapter.
     *
     * @param context              The context of the calling activity.
     * @param exercises            The list of Exercise objects to be displayed.
     * @param recyclerviewInterface The interface for handling item clicks.
     */
    public Exercise_Recyclerview_Adapter(Context context, ArrayList<Exercise> exercises, Recyclerview_Interface recyclerviewInterface) {
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
        View view = inflater.inflate(R.layout.rectcler_view_row, parent, false);
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
        holder.txt_name.setText(currentExercise.getName());
        holder.txt_repetitions.setText(currentExercise.getRepetitions());
        holder.txt_target.setText(currentExercise.getTarget().replace("exercises", "movement"));
        Picasso.get().load(currentExercise.getImageurl()).error(R.drawable.background_gradient_task).into(holder.imageView);

        holder.txt_askOmri.setPaintFlags(holder.txt_askOmri.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        holder.txt_askOmri.setOnClickListener(v -> {
            Intent intent = new Intent(context, HomePageActivity.class);
            intent.putExtra("toOmriChat", true);
            intent.putExtra("exercise", "Hi can you explain to me how to perform " + currentExercise.getName() + " properly");
            startActivity(context, intent, null);
        });
    }

    /**
     * Removes an item from the list and notifies the adapter.
     *
     * @param position The position of the item to be removed.
     */
    public void removeItem(int position) {
        exercises.remove(position);
        notifyItemRemoved(position);
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
        final ImageView imageView;
        final TextView txt_name, txt_target, txt_repetitions, txt_askOmri;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView                The view of the item.
         * @param recyclerviewInterface The interface for handling item clicks.
         */
        public MyViewHolder(@NonNull View itemView, Recyclerview_Interface recyclerviewInterface) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView3);
            txt_askOmri = itemView.findViewById(R.id.txt_askOmri);
            txt_name = itemView.findViewById(R.id.txt_name);
            txt_repetitions = itemView.findViewById(R.id.txt_repetitions);
            txt_target = itemView.findViewById(R.id.txt_category);

            itemView.setOnClickListener(view -> {
                if (recyclerviewInterface != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        recyclerviewInterface.ClickOnce(pos);
                    }
                }
            });
        }
    }
}
