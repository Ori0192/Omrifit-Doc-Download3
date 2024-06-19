//package com.example.omrifit.measures;
//
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.omrifit.R;
//import com.example.omrifit.classes.MyModelPredictor;
//
//import java.io.IOException;
//
//public class MyModelPredictorActivity extends AppCompatActivity {
//
//    private EditText ageInput, weightInput, heightInput, neckInput, chestInput, abdomenInput, ankleInput, bicepsInput, wristInput;
//    private Button predictButton;
//    private MyModelPredictor predictor;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_my_model_predictor);
//
//        // Initialize EditTexts
//        ageInput = findViewById(R.id.ageInput);
//        weightInput = findViewById(R.id.weightInput);
//        heightInput = findViewById(R.id.heightInput);
//        neckInput = findViewById(R.id.neckInput);
//        chestInput = findViewById(R.id.chestInput);
//        abdomenInput = findViewById(R.id.abdomenInput);
//        ankleInput = findViewById(R.id.ankleInput);
//        bicepsInput = findViewById(R.id.bicepsInput);
//        wristInput = findViewById(R.id.wristInput);
//
//        // Initialize Button
//        predictButton = findViewById(R.id.predictButton);
//
//        try {
//            predictor = new MyModelPredictor(MyModelPredictorActivity.this);
//        } catch (IOException e) {
//            throw new RuntimeException("Error initializing TensorFlow Lite model", e);
//        }
//
//        // Set onClickListener for the button
//        predictButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                performPrediction();
//            }
//        });
//    }
//
//    private void performPrediction() {
//        try {
//            float age = Float.parseFloat(ageInput.getText().toString());
//            float weight = Float.parseFloat(weightInput.getText().toString());
//            float height = Float.parseFloat(heightInput.getText().toString());
//            float neck = Float.parseFloat(neckInput.getText().toString());
//            float chest = Float.parseFloat(chestInput.getText().toString());
//            float abdomen = Float.parseFloat(abdomenInput.getText().toString());
//            float ankle = Float.parseFloat(ankleInput.getText().toString());
//            float biceps = Float.parseFloat(bicepsInput.getText().toString());
//            float wrist = Float.parseFloat(wristInput.getText().toString());
//
//            // Prepare input array
//            float[] input = new float[]{age, weight, height, neck, chest, abdomen, ankle, biceps, wrist};
//
//            // Perform prediction
//            float[] prediction = predictor.predict(input);
//
//            // Display the result
//            Toast.makeText(this, "Predicted Bodyfat: " + prediction[0], Toast.LENGTH_SHORT).show();
//
//        } catch (NumberFormatException e) {
//            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
//        }
//    }
//}
