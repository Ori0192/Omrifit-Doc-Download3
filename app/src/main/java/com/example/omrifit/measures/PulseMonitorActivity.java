package com.example.omrifit.measures;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import com.example.omrifit.R;


/**
 * Activity to monitor and display heart rate using the device's heart rate sensor.
 */
public class PulseMonitorActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor heartRateSensor;
    private TextView pulseTextView;

    /**
     * Initializes the activity, sets up the UI, and registers the heart rate sensor listener.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse_monitor);

        pulseTextView = findViewById(R.id.pulseTextView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        if (heartRateSensor != null) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            pulseTextView.setText("Heart beat sensor doesn't exist");
        }
    }

    /**
     * Called when there is a new sensor event.
     * @param event The SensorEvent provided by the sensor.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            float pulseValue = event.values[0];
            pulseTextView.setText("פולס: " + pulseValue);
        }
    }

    /**
     * Called when the accuracy of the registered sensor has changed.
     * @param sensor The sensor being monitored.
     * @param accuracy The new accuracy of this sensor.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle sensor accuracy changes if needed
    }

    /**
     * Called when the activity is paused. Unregisters the sensor listener to save power.
     */
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    /**
     * Called when the activity is resumed. Re-registers the sensor listener.
     */
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
