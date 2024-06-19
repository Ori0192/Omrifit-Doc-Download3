package com.example.omrifit.classes;

import android.app.Activity;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Class for performing predictions using a TensorFlow Lite model.
 */
public class MyModelPredictor {
    private final Interpreter tflite;
    private final double[] means = new double[]{
            45.5721393, 178.07736318, 70.04228856, 37.96119403, 100.64527363,
            92.62587065, 99.79054726, 59.22835821, 38.56268657, 22.99701493,
            32.07761194, 28.68159204, 18.21890547
    };
    private final double[] stds = new double[]{
            12.69018581, 27.93347459, 3.92055151, 2.34176584, 8.48270644,
            10.64355678, 6.82758964, 5.12018169, 2.38972734, 1.5704013,
            2.94342108, 2.02978063, 0.91680865
    };

    /**
     * Constructor that initializes the TensorFlow Lite interpreter.
     *
     * @param activity  The activity context for accessing assets.
     * @throws IOException If the model file cannot be loaded.
     */
    public MyModelPredictor(Activity activity) throws IOException {
        tflite = new Interpreter(loadModelFile(activity, "model.tflite"));
    }

    /**
     * Predicts the output based on the input features.
     *
     * @param input The input features for the model.
     * @return The predicted output.
     */
    public float[] predict(float[] input) {
        // Normalize the input
        for (int i = 0; i < input.length; i++) {
            input[i] = (float) normalize(input[i], means[i], stds[i]);
        }
        // Prepare the output array
        float[][] output = new float[1][1];
        // Run the model
        tflite.run(input, output);
        // Return the output
        return output[0];
    }

    /**
     * Normalizes a value using the provided mean and standard deviation.
     *
     * @param value The value to be normalized.
     * @param mean  The mean of the feature.
     * @param std   The standard deviation of the feature.
     * @return The normalized value.
     */
    private double normalize(float value, double mean, double std) {
        return (value - mean) / std;
    }

    /**
     * Loads the model file from the assets.
     *
     * @param activity  The activity context for accessing assets.
     * @param modelFile The name of the model file in assets.
     * @return The mapped byte buffer of the model file.
     * @throws IOException If the model file cannot be loaded.
     */
    private MappedByteBuffer loadModelFile(Activity activity, String modelFile) throws IOException {
        try (FileInputStream is = new FileInputStream(activity.getAssets().openFd(modelFile).getFileDescriptor())) {
            FileChannel channel = is.getChannel();
            long startOffset = activity.getAssets().openFd(modelFile).getStartOffset();
            long declaredLength = activity.getAssets().openFd(modelFile).getDeclaredLength();
            return channel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }
}
