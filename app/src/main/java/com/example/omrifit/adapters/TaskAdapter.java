package com.example.omrifit.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.example.omrifit.R;
import com.example.omrifit.classes.CustomPathView;
import com.example.omrifit.classes.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * RecyclerView Adapter for displaying a list of tasks.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private final ArrayList<Task> items;
    private final Context context;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser user = mAuth.getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference myRef = database.getReference("user_information").child(user.getUid());
    private final DatabaseReference myRef2 = database.getReference("tasks");

    /**
     * Constructor for initializing the adapter with necessary data.
     *
     * @param context The context of the calling activity.
     * @param items   The list of Task objects to be displayed.
     */
    public TaskAdapter(Context context, ArrayList<Task> items) {
        this.context = context;
        this.items = items;
    }

    /**
     * Creates and inflates the view holder.
     *
     * @param parent   The parent view group.
     * @param viewType The view type.
     * @return A new ViewHolder instance.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chaleng_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the data to the view holder.
     *
     * @param holder   The ViewHolder instance.
     * @param position The position of the current item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = items.get(position);
        holder.titleTextView.setText(task.getName());
        holder.descriptionTextView.setText(task.getDescription());
        holder.countTextView.setText(String.valueOf(position + 1));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (position == 0 || items.get(position - 1).isCompleted()) {
            holder.customPathView.setTasksCompleted(10);
        }

        if (position == 0 && !task.isCompleted() && isFirstTime()) {
            String endDate = addDaysToDate(dateFormat.format(Calendar.getInstance().getTime()), task.getDuration());
            myRef.child("taskTimesOver").setValue(endDate);
            myRef.child("taskFailed").setValue(false);
        }

        if (!task.isCompleted() && (position == 0 || items.get(position - 1).isCompleted())) {
            highlightCardView(holder.cardView);

            if (Task.NUTRITION_TASK.equals(task.getType())) {
                myRef.child("tryToNotEat").setValue(task.getTarget());
            }

            myRef.child("taskTimesOver").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String taskTimesOver = snapshot.getValue(String.class);
                        if (taskTimesOver != null && taskTimesOver.equals(dateFormat.format(Calendar.getInstance().getTime()))) {
                            checkAndHandleTaskCompletion(task, position, taskTimesOver);
                        }
                        holder.endDateTextView.setText("End Date: " + taskTimesOver);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Error fetching task end date.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Highlights the given CardView with a scale animation.
     *
     * @param cardView The CardView to be highlighted.
     */
    private void highlightCardView(CardView cardView) {
        cardView.setCardElevation(12f); // Change elevation to create shadows
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 1.05f, // Start and end scale for X axis
                1.0f, 1.05f, // Start and end scale for Y axis
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot X
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot Y
        scaleAnimation.setDuration(500); // Duration of animation
        scaleAnimation.setRepeatCount(Animation.INFINITE); // Infinite repeat
        scaleAnimation.setRepeatMode(Animation.REVERSE); // Reverse animation at each repeat
        cardView.startAnimation(scaleAnimation);
    }

    /**
     * Checks and handles task completion based on the given parameters.
     *
     * @param task           The task to be checked.
     * @param position       The position of the task in the list.
     * @param taskTimesOver  The task's end date.
     */
    private void checkAndHandleTaskCompletion(Task task, int position, String taskTimesOver) {
        myRef.child("taskFailed").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean taskFailed = snapshot.getValue(Boolean.class);
                    if (taskFailed) {
                        myRef.child("taskTimesOver").setValue(addDaysToDate(taskTimesOver, task.getDuration()));
                    } else {
                        if (position != getItemCount() - 1) {
                            task.setCompleted(true);
                            myRef2.child(String.valueOf(position)).child("isCompleted").child(user.getUid()).setValue(true);
                            notifyItemChanged(position);
                            myRef.child("taskTimesOver").setValue(addDaysToDate(taskTimesOver, items.get(position + 1).getDuration()));
                        }
                    }
                    myRef.child("taskFailed").setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("TaskAdapter", "onCancelled", error.toException());
            }
        });
    }

    /**
     * Checks if this is the first time the adapter is being used.
     *
     * @return True if it is the first time, false otherwise.
     */
    private boolean isFirstTime() {
        SharedPreferences preferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("RanBefore", false);
        if (!ranBefore) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("RanBefore", true);
            editor.apply();
        }
        return !ranBefore;
    }

    /**
     * Returns the size of the data list.
     *
     * @return The size of the data list.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder class for holding the view elements of each item.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView descriptionTextView;
        private final TextView countTextView;
        private final TextView endDateTextView;
        private final CustomPathView customPathView;
        private final CircleImageView circleImageView;
        private final CardView cardView;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The view of the item.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textView8);
            descriptionTextView = itemView.findViewById(R.id.textView13);
            countTextView = itemView.findViewById(R.id.textView14);
            endDateTextView = itemView.findViewById(R.id.tvEndDate);
            customPathView = itemView.findViewById(R.id.customPathView);
            circleImageView = itemView.findViewById(R.id.circleImageView);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    /**
     * Adds the specified number of days to the given date string.
     *
     * @param dateString The original date string.
     * @param days       The number of days to add.
     * @return The new date string with the added days.
     */
    public String addDaysToDate(String dateString, int days) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_MONTH, days);
            return sdf.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
