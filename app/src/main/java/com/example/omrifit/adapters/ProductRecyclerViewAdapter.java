package com.example.omrifit.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.omrifit.R;
import com.example.omrifit.classes.Product;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.util.List;

/**
 * RecyclerView Adapter for displaying a list of Product objects.
 */
public class ProductRecyclerViewAdapter extends RecyclerView.Adapter<ProductRecyclerViewAdapter.MyViewHolder> {

    private final Context context;
    private final List<Product> products;

    /**
     * Constructor for ProductRecyclerViewAdapter.
     *
     * @param context  The context of the calling activity.
     * @param products The list of Product objects to be displayed.
     */
    public ProductRecyclerViewAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
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
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_row_product, parent, false);
        return new MyViewHolder(view);
    }

    /**
     * Binds the data to the view holder.
     *
     * @param holder   The MyViewHolder instance.
     * @param position The position of the current item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Product product = products.get(position);
        holder.txt_name.setText(product.getName());
        holder.txt_protein.setText("Protein: " + product.getProtein() + "g");
        holder.txt_fat.setText("Fat: " + product.getFat() + "g");
        holder.txt_calories.setText("Carbohydrates: " + product.getCarbs() + "g");
        holder.setupPieChart(product);
    }

    /**
     * Returns the size of the data list.
     *
     * @return The size of the data list.
     */
    @Override
    public int getItemCount() {
        return products.size();
    }

    /**
     * ViewHolder class for holding the view elements of each item.
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        final TextView txt_name, txt_protein, txt_fat, txt_calories;
        final PieChart pieChart;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The view of the item.
         */
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_name = itemView.findViewById(R.id.txt_product_name);
            txt_protein = itemView.findViewById(R.id.txt_protein);
            txt_fat = itemView.findViewById(R.id.txt_fat);
            txt_calories = itemView.findViewById(R.id.txt_calories);
            pieChart = itemView.findViewById(R.id.piechart);
        }

        /**
         * Sets up the PieChart for displaying the nutritional information of the product.
         *
         * @param product The Product object containing the nutritional information.
         */
        public void setupPieChart(Product product) {
            pieChart.clearChart();
            pieChart.addPieSlice(new PieModel("Protein", product.getProtein(), Color.parseColor("#FF2196F3")));
            pieChart.addPieSlice(new PieModel("Fat", product.getFat(), Color.parseColor("#FF4CAF50")));
            pieChart.addPieSlice(new PieModel("Carbohydrates", product.getCarbs(), Color.parseColor("#FFF44336")));
            pieChart.startAnimation();
        }
    }
}
