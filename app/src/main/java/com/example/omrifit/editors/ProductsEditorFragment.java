package com.example.omrifit.editors;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.omrifit.R;
import com.example.omrifit.adapters.ProductRecyclerViewAdapter;
import com.example.omrifit.classes.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Fragment for editing and displaying products.
 */
public class ProductsEditorFragment extends Fragment {
    private EditText edtProteingrams, edtProductName, edtCalories, edtCarbsgrams, edtFatgrams;
    private Spinner spinMeasurementForm;
    private Button btnAdd;
    private CardView cardView;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private String category = "proteins";
    private DatabaseReference productRef;
    private ArrayList<Product> products = new ArrayList<>();
    private Spinner spinChoiceToShow;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_editot_exercise, container, false);

        initializeUI(view);
        setUpSpinners();
        setEventListeners();

        return view;
    }

    private void initializeUI(View view) {
        edtFatgrams = view.findViewById(R.id.edt_fatgrams);
        edtProductName = view.findViewById(R.id.edt_txt_name2);
        edtProteingrams = view.findViewById(R.id.edt_proteingrams);
        edtCalories = view.findViewById(R.id.edt_calories);
        edtCarbsgrams = view.findViewById(R.id.edt_carbsgrams);
        spinMeasurementForm = view.findViewById(R.id.spin_measurementform);
        btnAdd = view.findViewById(R.id.btn_add2);
        recyclerView = view.findViewById(R.id.recyclerviewforworkoutfragment);
        cardView = view.findViewById(R.id.cardView2);
        fab = view.findViewById(R.id.fabButton);
        spinChoiceToShow = view.findViewById(R.id.spin_choisetoshow);

        productRef = FirebaseDatabase.getInstance().getReference("products");
    }

    private void setUpSpinners() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, Arrays.asList("carbs", "fats", "proteins"));
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinChoiceToShow.setAdapter(categoryAdapter);

        ArrayAdapter<String> measurementAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, Arrays.asList("per 100g", "per unit"));
        measurementAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinMeasurementForm.setAdapter(measurementAdapter);

        showDetails("carbs");
    }

    private void setEventListeners() {
        fab.setOnClickListener(v -> cardView.setVisibility(View.VISIBLE));
        btnAdd.setOnClickListener(v -> addProduct());
        spinChoiceToShow.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showDetails(spinChoiceToShow.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });
    }

    private void addProduct() {
        String name = edtProductName.getText().toString().trim();
        String measurementForm = spinMeasurementForm.getSelectedItem().toString();
        int calories = Integer.parseInt(edtCalories.getText().toString().trim());
        int proteinGrams = Integer.parseInt(edtProteingrams.getText().toString().trim());
        int carbsGrams = Integer.parseInt(edtCarbsgrams.getText().toString().trim());
        int fatGrams = Integer.parseInt(edtFatgrams.getText().toString().trim());

        if (!name.isEmpty() && calories >= 0 && proteinGrams >= 0 && carbsGrams >= 0 && fatGrams >= 0) {
            Product product = new Product(name, measurementForm, calories, proteinGrams, carbsGrams, fatGrams);
            determineCategory(fatGrams, carbsGrams, proteinGrams);
            checkAndAddProduct(product, name);
        } else {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void determineCategory(int fatGrams, int carbsGrams, int proteinGrams) {
        if (fatGrams > Math.max(carbsGrams, proteinGrams)) {
            category = "fats";
        } else if (carbsGrams > proteinGrams) {
            category = "carbs";
        } else {
            category = "proteins";
        }
    }

    private void checkAndAddProduct(Product product, String name) {
        productRef.child(category).child(name.toLowerCase()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(requireContext(), "The product already exists", Toast.LENGTH_SHORT).show();
                } else {
                    productRef.child(category).child(name.toLowerCase()).setValue(product);
                    Toast.makeText(requireContext(), "The product added to the " + category + " section", Toast.LENGTH_SHORT).show();
                    clearFields();
                    showDetails(spinChoiceToShow.getSelectedItem().toString());
                    cardView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }

    private void showDetails(String category) {
        productRef.child(category).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                products.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        products.add(product);
                    }
                }
                updateRecyclerView(products);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Failed to read value.", error.toException());
            }
        });
    }

    private void updateRecyclerView(ArrayList<Product> products) {
        ProductRecyclerViewAdapter adapter = new ProductRecyclerViewAdapter(requireContext(), products);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void clearFields() {
        edtProductName.setText("");
        edtCarbsgrams.setText("");
        edtProteingrams.setText("");
        edtCalories.setText("");
        edtFatgrams.setText("");
    }
}
