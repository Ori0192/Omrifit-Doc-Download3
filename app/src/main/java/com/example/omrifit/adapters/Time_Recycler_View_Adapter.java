package com.example.omrifit.adapters;

import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.omrifit.R;
import com.example.omrifit.classes.Time;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

/**
 * RecyclerView Adapter for displaying a list of items with the ability to set times.
 */
public class Time_Recycler_View_Adapter extends RecyclerView.Adapter<Time_Recycler_View_Adapter.TimeViewHolder> {
    private final ArrayList<String> items_to_set_time;
    private final Context context;
    private Time time = new Time(0, 0);

    /**
     * Constructor for initializing the adapter with necessary data.
     *
     * @param context           The context of the calling activity.
     * @param items_to_set_time The list of items for which times can be set.
     */
    public Time_Recycler_View_Adapter(Context context, ArrayList<String> items_to_set_time) {
        this.items_to_set_time = items_to_set_time;
        this.context = context;
    }

    /**
     * Creates and inflates the view holder.
     *
     * @param parent   The parent view group.
     * @param viewType The view type.
     * @return A new TimeViewHolder instance.
     */
    @NonNull
    @Override
    public TimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_time_row, parent, false);
        return new TimeViewHolder(view);
    }

    /**
     * Binds the data to the view holder.
     *
     * @param holder   The TimeViewHolder instance.
     * @param position The position of the current item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull TimeViewHolder holder, int position) {
        String current_item = items_to_set_time.get(position);
        holder.txt_item_to_set_time.setText(current_item);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        DatabaseReference timesRef = FirebaseDatabase.getInstance().getReference("user_information").child(user.getUid()).child("usertimes").child(current_item);

        // Check for existing time
        timesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Time existingTime = snapshot.getValue(Time.class);
                    if (existingTime != null) {
                        holder.btn_set_time.setText(String.format(Locale.getDefault(), "%02d:%02d", existingTime.getHours(), existingTime.getMinutes()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });

        holder.btn_set_time.setOnClickListener(v -> time_picker(holder, context, current_item));
    }

    /**
     * Opens a time picker dialog to set the time for the given item.
     *
     * @param holder      The TimeViewHolder instance.
     * @param context     The context of the calling activity.
     * @param currentitem The current item for which the time is being set.
     * @return The selected time.
     */
    public Time time_picker(@NonNull TimeViewHolder holder, Context context, String currentitem) {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view, hourOfDay, minute) -> {
            time = new Time(hourOfDay, minute);
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            DatabaseReference timesRef = FirebaseDatabase.getInstance().getReference("user_information").child(user.getUid()).child("usertimes").child(currentitem);
            timesRef.setValue(time);
            holder.btn_set_time.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(context, onTimeSetListener, time.getHours(), time.getMinutes(), true);
        timePickerDialog.setTitle("Choose time");
        timePickerDialog.show();
        return time;
    }

    /**
     * Returns the size of the data list.
     *
     * @return The size of the data list.
     */
    @Override
    public int getItemCount() {
        return items_to_set_time.size();
    }

    /**
     * ViewHolder class for holding the view elements of each item.
     */
    public static class TimeViewHolder extends RecyclerView.ViewHolder {
        final TextView txt_item_to_set_time;
        final Button btn_set_time;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The view of the item.
         */
        public TimeViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_item_to_set_time = itemView.findViewById(R.id.txt_item_to_set_time);
            btn_set_time = itemView.findViewById(R.id.btn_set_time);
        }
    }
}
