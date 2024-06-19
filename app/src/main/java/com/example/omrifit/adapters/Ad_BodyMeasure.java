package com.example.omrifit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.omrifit.R;
import com.example.omrifit.classes.BodyMeasure;

import java.util.List;

/**
 * RecyclerView Adapter for displaying a list of BodyMeasure items.
 */
public class Ad_BodyMeasure extends RecyclerView.Adapter<Ad_BodyMeasure.ViewHolder> {

    private final LayoutInflater mInflater;
    private final List<BodyMeasure> data;
    private final Context mContext;

    /**
     * Constructor for Ad_BodyMeasure adapter.
     *
     * @param context The context of the calling activity.
     * @param data    The list of BodyMeasure objects to be displayed.
     */
    public Ad_BodyMeasure(Context context, List<BodyMeasure> data) {
        this.mContext = context;
        this.data = data;
        this.mInflater = LayoutInflater.from(context);
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
        View view = mInflater.inflate(R.layout.item_bodymeasure, parent, false);
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
        BodyMeasure currentMeasure = data.get(position);
        holder.dateTextView.setText(currentMeasure.getDate());
        holder.weightTextView.setText(String.format("%s KG", currentMeasure.getWeight()));
    }

    /**
     * Returns the size of the data list.
     *
     * @return The size of the data list.
     */
    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * ViewHolder class for holding the view elements of each item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView dateTextView;
        final TextView weightTextView;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The view of the item.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            weightTextView = itemView.findViewById(R.id.weightTextView);
        }
    }
}
