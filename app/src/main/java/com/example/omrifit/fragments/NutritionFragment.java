package com.example.omrifit.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.omrifit.R;
import com.example.omrifit.classes.Product;
import com.example.omrifit.classes.Time;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.HashMap;
import java.util.Map;

public class NutritionFragment extends Fragment {

    private int weight, height, age;
    private String gender, target, dailyActivity;
    private ListView listView, listViewForProductsEaten;
    private ArrayList<Product> productsForIndex = new ArrayList<>();
    private ArrayList<String> listForProductsEaten = new ArrayList<>();
    private EditText edtSearch, edtGrams;
    private boolean flagForMealTime = false;
    TextView txtMeasurement;
    private Time time = new Time();
    private Product productSelected = null;
    private double duplicateIn = 0;
    private TextView[] textViews;
    private double sumCalories = 0, sumCarbs = 0, sumProtein = 0, sumFat = 0;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("user_information").child(mAuth.getUid());
    private ProgressBar progressBar, progressBarFats, progressBarProtein, progressBarCarbs;
    private CardView cardView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nutrition_fragemant, container, false);
        setHasOptionsMenu(true);

        initializeUIElements(view);
        configureFloatingActionButton(view);
        configureSearchButton(view);
        setupTouchListener(view);

        return view;
    }

    /**
     * Initialize UI elements.
     * @param view The root view of the fragment.
     */
    private void initializeUIElements(View view) {
        FloatingActionButton fab = view.findViewById(R.id.floatingActionButton);
        cardView = view.findViewById(R.id.cardviewnutrition);
        listView = view.findViewById(R.id.listviewnutrition);
        edtGrams = view.findViewById(R.id.edt_grams);
        edtSearch = view.findViewById(R.id.editTextsearch);
        progressBar = view.findViewById(R.id.progressBar);
        progressBarCarbs = view.findViewById(R.id.progressBarCarbs);
        progressBarProtein = view.findViewById(R.id.progressBarProtein);
        progressBarFats = view.findViewById(R.id.progressBarFats);
        listViewForProductsEaten = view.findViewById(R.id.lsv_nutrition);
        textViews = new TextView[]{
                view.findViewById(R.id.txt_calories),
                view.findViewById(R.id.txt_protein),
                view.findViewById(R.id.txt_carbs),
                view.findViewById(R.id.txt_fat)
        };
        txtMeasurement = view.findViewById(R.id.txt_gramsorunits);
        updateProductNutrition(0, 0, 0, 0, "", "", ""); // Update on fragment start
    }

    /**
     * Configure the Floating Action Button.
     * @param view The root view of the fragment.
     */
    private void configureFloatingActionButton(View view) {
        FloatingActionButton fab = view.findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(v -> checkMealTimeAndShowCard());
    }

    /**
     * Configure the search button.
     * @param view The root view of the fragment.
     */
    private void configureSearchButton(View view) {
        ImageButton btnSearch = view.findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(v -> {
            listView.setVisibility(View.VISIBLE);
            String search = edtSearch.getText().toString();
            if (search.isEmpty()) {
                edtSearch.setError("Please enter a product to search");
            } else {
                filterData(search);
                setupListViewOnClickListener();
            }
            configureNextButton(view);
        });
    }

    /**
     * Configure the next button.
     * @param view The root view of the fragment.
     */
    private void configureNextButton(View view) {
        Button btnNext = view.findViewById(R.id.btn_next_nutrition);
        btnNext.setOnClickListener(v -> {
            String searchText = edtSearch.getText().toString();
            if (!isProductSelected(searchText)) {
                Toast.makeText(requireContext(), "Product not found, please select again", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isInputInvalid()) {
                Toast.makeText(requireContext(), "Please enter quantity in grams or units", Toast.LENGTH_SHORT).show();
            } else {
                addProductIfAllowed();
            }
        });
    }

    /**
     * Check if the product is selected.
     * @param searchText The search text.
     * @return True if the product is selected, otherwise false.
     */
    private boolean isProductSelected(String searchText) {
        for (Product product : productsForIndex) {
            if (product.getName().equalsIgnoreCase(searchText)) {
                productSelected = product;
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the input is invalid.
     * @return True if the input is invalid, otherwise false.
     */
    private boolean isInputInvalid() {
        return edtGrams.getText().toString().isEmpty() || productSelected == null;
    }

    /**
     * Add the product if allowed.
     */
    private void addProductIfAllowed() {
        checkProductAgainstRestriction(productSelected.getName(), isAllowed -> {
            if (isAllowed) {
                double gramsOrUnits = Integer.parseInt(edtGrams.getText().toString()) * duplicateIn;
                updateProductNutrition(
                        productSelected.getCalories() * gramsOrUnits,
                        productSelected.getProtein() * gramsOrUnits,
                        productSelected.getCarbs() * gramsOrUnits,
                        productSelected.getFat() * gramsOrUnits,
                        productSelected.getName(),
                        edtGrams.getText().toString(),
                        getMeasurementText(productSelected.getMeasurementform())
                );
                Toast.makeText(requireContext(), "Product added successfully", Toast.LENGTH_SHORT).show();
                cardView.setVisibility(View.GONE);
                cleanFields();
            } else {
                Toast.makeText(getContext(), "This product is not recommended for you", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Set up touch listener for the card view.
     * @param view The root view of the fragment.
     */
    private void setupTouchListener(View view) {
        ConstraintLayout cl = view.findViewById(R.id.re);
        cl.setOnTouchListener((v, event) -> {
            if (cardView.isShown()) {
                cardView.setVisibility(View.GONE);
                return true; // Event handled
            }
            return false;
        });
    }

    /**
     * Check meal time and show the card view if appropriate.
     */
    private void checkMealTimeAndShowCard() {
        FirebaseUser user = mAuth.getCurrentUser();
        DatabaseReference timesRef = FirebaseDatabase.getInstance().getReference("user_information").child(user.getUid());
        timesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                handleMealTime(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    /**
     * Handle meal time logic.
     * @param snapshot The data snapshot.
     */
    private void handleMealTime(DataSnapshot snapshot) {
        flagForMealTime = false;
        for (DataSnapshot shot : snapshot.child("usertimes").getChildren()) {
            time = shot.getValue(Time.class);
            if (isWithinHalfHourOfCurrentTime(time.getHours(), time.getMinutes())) {
                flagForMealTime = true;
                break;
            }
        }
        if (snapshot.child("getMealTimes").getValue(Boolean.class)) {
            if (flagForMealTime && progressBar.getProgress() < progressBar.getMax()) {
                cardView.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getContext(), "Wait for your next meal", Toast.LENGTH_SHORT).show();
            }
        } else {
            cardView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set up ListView item click listener.
     */
    private void setupListViewOnClickListener() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = parent.getItemAtPosition(position).toString();
            edtSearch.setText(selectedItem);
            productSelected = productsForIndex.get(position);
            if (productSelected != null) {
                updateMeasurementText(productSelected.getMeasurementform());
            }
            listView.setVisibility(View.GONE);
        });
    }
    /**
     * Update the measurement text based on the selected measurement form.
     * @param selectedMeasurement The selected measurement form.
     */
    private void updateMeasurementText(String selectedMeasurement) {
        if (selectedMeasurement != null) {
            if (selectedMeasurement.equals("per 100g")) {
                txtMeasurement.setText("grams");
                duplicateIn = 0.01;
            } else {
                txtMeasurement.setText("units");
                duplicateIn = 1;
            }
        }
    }


    /**
     * Get the measurement text based on the selected measurement form.
     * @param selectedMeasurement The selected measurement form.
     * @return The measurement text.
     */
    private String getMeasurementText(String selectedMeasurement) {
        return selectedMeasurement.equals("per 100g") ? "grams" : "units";
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    /**
     * Filter data based on the search term.
     * @param search The search term.
     */
    private void filterData(String search) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("products");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Product> products = new ArrayList<>();
                for (DataSnapshot snapshot2 : snapshot.getChildren()) {
                    for (DataSnapshot shot : snapshot2.getChildren()) {
                        Product product = shot.getValue(Product.class);
                        if (product != null && product.getName().toLowerCase().contains(search.toLowerCase())) {
                            products.add(product);
                        }
                    }
                }
                updateList(products);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    /**
     * Update the ListView with the filtered products.
     * @param products The filtered products.
     */
    private void updateList(ArrayList<Product> products) {
        ArrayList<String> filteredData = new ArrayList<>();
        productsForIndex = new ArrayList<>();
        for (Product product : products) {
            filteredData.add(product.getName());
            productsForIndex.add(product);
        }
        if (filteredData.isEmpty()) {
            edtSearch.setError("Product not found");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), com.google.android.material.R.layout.support_simple_spinner_dropdown_item, filteredData);
        listView.setAdapter(adapter);
    }

    /**
     * Clear the input fields.
     */
    private void cleanFields() {
        edtSearch.setText("");
        edtGrams.setText("");
    }

    /**
     * Check if the product is restricted.
     * @param productName The product name.
     * @param callback The callback to handle the result.
     */
    private void checkProductAgainstRestriction(String productName, ProductCheckCallback callback) {
        myRef.child("tryToNotEat").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String restrictedProduct = dataSnapshot.getValue(String.class);
                if (productName.equalsIgnoreCase(restrictedProduct)) {
                    showChallengeDialog(productName, callback);
                } else {
                    callback.onCheckResult(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("NutritionFragment", "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    /**
     * Show a dialog to challenge the user about restricted products.
     * @param productName The product name.
     * @param callback The callback to handle the result.
     */
    private void showChallengeDialog(String productName, ProductCheckCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Eating Challenge");
        builder.setMessage("The product " + productName + " is in the list of products you tried to avoid. Do you want to break the challenge?");

        builder.setPositiveButton("Yes", (dialog, id) -> {
            callback.onCheckResult(true);
            myRef.child("taskFailed").setValue(true);
        });
        builder.setNegativeButton("No", (dialog, id) -> callback.onCheckResult(false));

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Get the current date as a string.
     * @return The current date.
     */
    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(calendar.getTime());
    }

    /**
     * Update the user's nutrition information.
     * @param v The calories to add.
     * @param v1 The protein to add.
     * @param v2 The carbs to add.
     * @param v3 The fat to add.
     * @param name The product name.
     * @param grams The amount in grams or units.
     * @param measurement The measurement type.
     */
    public void updateProductNutrition(double v, double v1, double v2, double v3, String name, String grams, String measurement) {
        String currentDate = getCurrentDate();
        DatabaseReference nutritionRef = myRef.child("daily_nutrition").child(currentDate);
        nutritionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                updateNutritionValues(snapshot, v, v1, v2, v3, name, grams, measurement);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle cancelled event
            }
        });
    }

    /**
     * Update the nutrition values.
     * @param snapshot The data snapshot.
     * @param v The calories to add.
     * @param v1 The protein to add.
     * @param v2 The carbs to add.
     * @param v3 The fat to add.
     * @param name The product name.
     * @param grams The amount in grams or units.
     * @param measurement The measurement type.
     */
    private void updateNutritionValues(DataSnapshot snapshot, double v, double v1, double v2, double v3, String name, String grams, String measurement) {
        listForProductsEaten.clear();
        if (snapshot.exists()) {
            sumCalories = snapshot.child("calories").getValue(Double.class);
            sumCarbs = snapshot.child("carbs").getValue(Double.class);
            sumProtein = snapshot.child("protein").getValue(Double.class);
            sumFat = snapshot.child("fat").getValue(Double.class);
            for (DataSnapshot shot : snapshot.child("listForProductsEaten").getChildren()) {
                listForProductsEaten.add(shot.getValue(String.class));
            }
        } else {
            resetNutritionValues();
        }
        int newCalories = (int) (sumCalories + v);
        int newProtein = (int) (sumProtein + v1);
        int newCarbs = (int) (sumCarbs + v2);
        int newFat = (int) (sumFat + v3);
        if (!name.isEmpty()) {
            listForProductsEaten.add(name + " " + grams + " " + measurement);
        }

        Map<String, Object> nutritionUpdates = new HashMap<>();
        nutritionUpdates.put("calories", newCalories);
        nutritionUpdates.put("carbs", newCarbs);
        nutritionUpdates.put("protein", newProtein);
        nutritionUpdates.put("fat", newFat);

        DatabaseReference nutritionRef = myRef.child("daily_nutrition").child(getCurrentDate());
        nutritionRef.updateChildren(nutritionUpdates);
        nutritionRef.child("listForProductsEaten").setValue(listForProductsEaten);

        updateUI(newCalories, newCarbs, newProtein, newFat, listForProductsEaten);
    }

    /**
     * Reset the nutrition values.
     */
    private void resetNutritionValues() {
        sumCalories = 0.0;
        sumCarbs = 0.0;
        sumProtein = 0.0;
        sumFat = 0.0;
    }

    /**
     * Update the UI with the new nutrition values.
     * @param calories The new calories.
     * @param carbs The new carbs.
     * @param protein The new protein.
     * @param fat The new fat.
     * @param listForProductsEaten The list of products eaten.
     */
    private void updateUI(int calories, int carbs, int protein, int fat, ArrayList<String> listForProductsEaten) {
        textViews[0].setText(String.valueOf(calories));
        textViews[1].setText(String.valueOf(protein));
        textViews[2].setText(String.valueOf(carbs));
        textViews[3].setText(String.valueOf(fat));
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateUserProfile(dataSnapshot, calories, protein, carbs, fat);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle errors
            }
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.simple_spinner_dropdown_item, listForProductsEaten);
        listViewForProductsEaten.setAdapter(adapter);
    }

    /**
     * Update the user's profile with the new nutrition values.
     * @param dataSnapshot The data snapshot.
     * @param calories The new calories.
     * @param protein The new protein.
     * @param carbs The new carbs.
     * @param fat The new fat.
     */
    private void updateUserProfile(DataSnapshot dataSnapshot, int calories, int protein, int carbs, int fat) {
        weight = dataSnapshot.child("user_profile").child("weight").getValue(Integer.class);
        age = dataSnapshot.child("user_profile").child("age").getValue(Integer.class);
        height = dataSnapshot.child("user_profile").child("height").getValue(Integer.class);
        gender = dataSnapshot.child("user_profile").child("gender").getValue(String.class);
        target = dataSnapshot.child("userNutritionInfo").child("target").getValue(String.class);
        dailyActivity = dataSnapshot.child("userNutritionInfo").child("daily_activity").getValue(String.class);

        textViews[0].setText("calories: \n" + calories + "/\n" + getRequiredCalories(dailyActivity, gender, target, weight, height, age));
        textViews[1].setText("protein: \n" + protein + "/\n" + getRequiredProtein(weight));
        textViews[3].setText("fats: \n" + fat + "/\n" + getRequiredFat(weight, target));
        progressBar.setMax(getRequiredCalories(dailyActivity, gender, target, weight, height, age));
        progressBarProtein.setMax(getRequiredProtein(weight));
        progressBarFats.setMax(getRequiredFat(weight, target));
        progressBarCarbs.setMax(Math.round((float) (progressBar.getMax() - progressBarProtein.getMax() * 4 - progressBarFats.getMax() * 4) / 4));
        textViews[2].setText("carbs: \n" + carbs + "/\n" + progressBarCarbs.getMax());
        progressBar.setProgress(calories);
        progressBarProtein.setProgress(protein);
        progressBarCarbs.setProgress(carbs);
        progressBarFats.setProgress(fat);
    }

    /**
     * Check if the target time is within half an hour of the current time.
     * @param targetHour The target hour.
     * @param targetMinute The target minute.
     * @return True if the target time is within half an hour of the current time, otherwise false.
     */
    public static boolean isWithinHalfHourOfCurrentTime(int targetHour, int targetMinute) {
        Calendar currentTime = Calendar.getInstance();
        Calendar halfHourBefore = (Calendar) currentTime.clone();
        halfHourBefore.add(Calendar.MINUTE, -30);

        Calendar halfHourAfter = (Calendar) currentTime.clone();
        halfHourAfter.add(Calendar.MINUTE, 30);

        Calendar targetTime = Calendar.getInstance();
        targetTime.set(Calendar.HOUR_OF_DAY, targetHour);
        targetTime.set(Calendar.MINUTE, targetMinute);
        targetTime.set(Calendar.SECOND, 0);
        targetTime.set(Calendar.MILLISECOND, 0);

        return !targetTime.before(halfHourBefore) && !targetTime.after(halfHourAfter);
    }

    /**
     * Get the list of daily activities.
     * @return The list of daily activities.
     */
    private ArrayList<String> getDailyActivity() {
        ArrayList<String> options = new ArrayList<>();
        options.add("None");
        options.add("Light");
        options.add("Moderate");
        options.add("Heavy");
        options.add("Very Heavy");
        return options;
    }

    /**
     * Get the required calories for the user.
     * @param dailyActivity The daily activity level.
     * @param gender The gender of the user.
     * @param target The target goal (e.g., weight loss, mass gain).
     * @param weight The weight of the user.
     * @param height The height of the user.
     * @param age The age of the user.
     * @return The required calories.
     */
    public int getRequiredCalories(String dailyActivity, String gender, String target, int weight, int height, int age) {
        double dailyActivityNum = getActivityMultiplier(dailyActivity);
        int addition = getTargetAddition(target);
        double requiredCalories = calculateRequiredCalories(gender, weight, height, age, dailyActivityNum, addition);
        return (int) Math.round(requiredCalories);
    }

    /**
     * Get the activity multiplier based on the daily activity level.
     * @param dailyActivity The daily activity level.
     * @return The activity multiplier.
     */
    private double getActivityMultiplier(String dailyActivity) {
        switch (getDailyActivity().indexOf(dailyActivity)) {
            case 0:
                return 1.2;
            case 1:
                return 1.375;
            case 2:
                return 1.55;
            case 3:
                return 1.725;
            case 4:
                return 1.9;
            default:
                return 1.0;
        }
    }

    /**
     * Get the target addition based on the target goal.
     * @param target The target goal (e.g., weight loss, mass gain).
     * @return The target addition.
     */
    private int getTargetAddition(String target) {
        if ("Mass Gain".equals(target)) return 400;
        if ("Weight Loss".equals(target)) return -400;
        return 0;
    }

    /**
     * Calculate the required calories for the user.
     * @param gender The gender of the user.
     * @param weight The weight of the user.
     * @param height The height of the user.
     * @param age The age of the user.
     * @param dailyActivityNum The activity multiplier.
     * @param addition The target addition.
     * @return The required calories.
     */
    private double calculateRequiredCalories(String gender, int weight, int height, int age, double dailyActivityNum, int addition) {
        if ("Male".equals(gender)) {
            return (10 * weight + 6.25 * height - 5 * age + 5) * dailyActivityNum + addition;
        } else {
            return (10 * weight + 6.25 * height - 5 * age - 161) * dailyActivityNum + addition;
        }
    }

    /**
     * Get the required protein for the user.
     * @param weight The weight of the user.
     * @return The required protein.
     */
    public int getRequiredProtein(int weight) {
        return (int) Math.round(2.2 * weight);
    }

    /**
     * Get the required fat for the user.
     * @param weight The weight of the user.
     * @param target The target goal (e.g., weight loss, mass gain).
     * @return The required fat.
     */
    public int getRequiredFat(int weight, String target) {
        double fat = 0.8 * weight;
        if ("Mass Gain".equals(target)) fat *= 1.25;
        if ("Weight Loss".equals(target)) fat *= 0.75;
        return (int) Math.round(fat);
    }

    /**
     * Interface for product check callback.
     */
    private interface ProductCheckCallback {
        void onCheckResult(boolean isAllowed);
    }
}
