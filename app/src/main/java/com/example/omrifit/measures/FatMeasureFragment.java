package com.example.omrifit.measures;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;


import com.example.omrifit.R;
import com.example.omrifit.classes.MyModelPredictor;

import java.io.IOException;

/**
 * Fragment for measuring body fat using various input parameters and a machine learning model.
 */
public class FatMeasureFragment extends Fragment {

    private EditText ageInput, weightInput, heightInput, neckInput, chestInput, abdomenInput, ankleInput, bicepsInput, wristInput;
    private Button predictButton;
    private MyModelPredictor predictor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_my_model_predictor, container, false);

        initializeUIElements(view);

        try {
            predictor = new MyModelPredictor(requireActivity());
        } catch (IOException e) {
            throw new RuntimeException("Error initializing TensorFlow Lite model", e);
        }

        predictButton.setOnClickListener(v -> performPrediction());

        return view;
    }

    /**
     * Initializes UI elements.
     * @param view The root view of the fragment.
     */
    private void initializeUIElements(View view) {
        ageInput = view.findViewById(R.id.ageInput);
        weightInput = view.findViewById(R.id.weightInput);
        heightInput = view.findViewById(R.id.heightInput);
        neckInput = view.findViewById(R.id.neckInput);
        chestInput = view.findViewById(R.id.chestInput);
        abdomenInput = view.findViewById(R.id.abdomenInput);
        ankleInput = view.findViewById(R.id.ankleInput);
        bicepsInput = view.findViewById(R.id.bicepsInput);
        wristInput = view.findViewById(R.id.wristInput);
        predictButton = view.findViewById(R.id.predictButton);
    }

    /**
     * Performs the prediction of body fat percentage using the input values.
     */
    private void performPrediction() {
        try {
            float age = Float.parseFloat(ageInput.getText().toString());
            float weight = Float.parseFloat(weightInput.getText().toString());
            float height = Float.parseFloat(heightInput.getText().toString());
            float neck = Float.parseFloat(neckInput.getText().toString());
            float chest = Float.parseFloat(chestInput.getText().toString());
            float abdomen = Float.parseFloat(abdomenInput.getText().toString());
            float ankle = Float.parseFloat(ankleInput.getText().toString());
            float biceps = Float.parseFloat(bicepsInput.getText().toString());
            float wrist = Float.parseFloat(wristInput.getText().toString());

            if (validateInputValues(age, weight, height)) {
                float[] input = createInputArray(age, weight, height, neck, chest, abdomen, ankle, biceps, wrist);
                float[] prediction = predictor.predict(input);
                displayPrediction(prediction);
            }

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validates the input values.
     * @param age The age input.
     * @param weight The weight input.
     * @param height The height input.
     * @return true if all values are within the acceptable range, false otherwise.
     */
    private boolean validateInputValues(float age, float weight, float height) {
        if (height < 100 || height > 200) {
            Toast.makeText(getContext(), "Height must be between 100cm and 200cm", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (weight < 50 || weight > 120) {
            Toast.makeText(getContext(), "Weight must be between 50kg and 120kg", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (age < 18 || age > 80) {
            Toast.makeText(getContext(), "Age must be between 18 and 80", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Creates the input array for the prediction model.
     * @param age The age input.
     * @param weight The weight input.
     * @param height The height input.
     * @param neck The neck measurement.
     * @param chest The chest measurement.
     * @param abdomen The abdomen measurement.
     * @param ankle The ankle measurement.
     * @param biceps The biceps measurement.
     * @param wrist The wrist measurement.
     * @return The input array.
     */
    private float[] createInputArray(float age, float weight, float height, float neck, float chest, float abdomen, float ankle, float biceps, float wrist) {
        return new float[]{age, (float) (weight * 2.2), height - 100, neck, chest, abdomen, 101, 60, 41, ankle, biceps, 30, wrist};
    }

    /**
     * Displays the prediction result.
     * @param prediction The prediction result from the model.
     */
    private void displayPrediction(float[] prediction) {
        Toast.makeText(getContext(), "Predicted Bodyfat: " + (prediction[0] - 35), Toast.LENGTH_SHORT).show();
    }
}
