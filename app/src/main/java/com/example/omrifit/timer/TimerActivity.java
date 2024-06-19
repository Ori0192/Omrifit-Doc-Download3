package com.example.omrifit.timer;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;


import com.example.omrifit.R;
import com.example.omrifit.TimerService;

/**
 * TimerActivity is responsible for managing the timer functionality within the app.
 * It includes starting, pausing, resuming, and canceling the timer.
 */
public class TimerActivity extends Activity {

    private Button startButton, btnMinus, btnPlus, btnCancel, btnStart;
    private TextView txtProgress;
    private ProgressBar progressBar;
    private ConstraintLayout constraintLayout;
    private Intent intent;
    private String initialTime;
    private boolean isReceiverRegistered = false;
    private long totalTimeInMilliseconds;
    private Button[] buttons;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        preferences = getSharedPreferences("timer_prefs", MODE_PRIVATE);

        initializeUI();
        setButtonListeners();
        checkScheduleExactAlarmPermission();

        startButton.setOnClickListener(view -> {
            String buttonText = startButton.getText().toString();
            intent = new Intent(TimerActivity.this, TimerService.class);
            if ("START".equals(buttonText)) {
                initialTime = txtProgress.getText().toString();
                progressBar.setProgress(100);
                intent.setAction("START");
                adjustLayoutSize(ViewGroup.LayoutParams.MATCH_PARENT);
                long countdownTime = convertTimeTextToMilliseconds(txtProgress.getText().toString());
                intent.putExtra("countdownTime", countdownTime);
                startService(intent);
                startButton.setText("PAUSE");
                btnCancel.setVisibility(View.VISIBLE);
                toggleVisibility(View.GONE);
                saveTimerState(true, "PAUSE");
            } else if ("PAUSE".equals(buttonText)) {
                intent.setAction("PAUSE");
                startService(intent);
                startButton.setText("CONTINUE");
                saveTimerState(true, "CONTINUE");
            } else if ("CONTINUE".equals(buttonText)) {
                intent.setAction("CONTINUE");
                startService(intent);
                startButton.setText("PAUSE");
                saveTimerState(true, "PAUSE");
            }
            registerBroadcastReceiver();
        });
    }

    /**
     * Checks and requests permission for scheduling exact alarms on devices running Android S and above.
     */
    private void checkScheduleExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    /**
     * Sets up listeners for the buttons to handle time adjustments and timer control.
     */
    private void setButtonListeners() {
        btnMinus.setOnClickListener(v -> updateTime(-5));
        btnPlus.setOnClickListener(v -> updateTime(5));
        for (Button button : buttons) {
            button.setOnClickListener(v -> setTime(button.getText().toString()));
        }
        btnCancel.setOnClickListener(v -> {
            intent.setAction("CANCEL");
            startService(intent);
            btnCancel.setVisibility(View.GONE);
            adjustLayoutSize(ViewGroup.LayoutParams.WRAP_CONTENT);
            startButton.setText("START");
            toggleVisibility(View.VISIBLE);
            saveTimerState(false, "START");
        });
    }

    /**
     * Sets the timer's display to a specific time based on the button pressed.
     *
     * @param timeString The time to set in minutes.
     */
    private void setTime(String timeString) {
        try {
            int timeInMinutes = Integer.parseInt(timeString);
            txtProgress.setText(String.format("%02d:%02d", timeInMinutes / 60, timeInMinutes % 60));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the timer's display by adding or subtracting seconds.
     *
     * @param deltaSeconds The number of seconds to add or subtract.
     */
    private void updateTime(int deltaSeconds) {
        String currentTime = txtProgress.getText().toString();
        try {
            String[] parts = currentTime.split(":");
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            int totalSeconds = minutes * 60 + seconds + deltaSeconds;
            if (totalSeconds < 0) totalSeconds = 0;
            txtProgress.setText(String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the user interface components.
     */
    private void initializeUI() {
        progressBar = findViewById(R.id.progressBar);
        txtProgress = findViewById(R.id.txtProgress);
        btnMinus = findViewById(R.id.btnback);
        btnPlus = findViewById(R.id.btnnext);
        btnStart = findViewById(R.id.btnStart);
        startButton = findViewById(R.id.btnStart);
        btnCancel = findViewById(R.id.btnCancel);
        constraintLayout = findViewById(R.id.cons_timer);
        progressBar.setProgress(100);
        buttons = new Button[]{
                findViewById(R.id.btn20),
                findViewById(R.id.btn30),
                findViewById(R.id.btn45),
                findViewById(R.id.btn60),
                findViewById(R.id.btn90),
                findViewById(R.id.btn120)
        };
    }

    /**
     * Converts the time text to milliseconds.
     *
     * @param timeText The time text in the format mm:ss.
     * @return The time in milliseconds.
     */
    private long convertTimeTextToMilliseconds(String timeText) {
        long millis = 0;
        try {
            String[] parts = timeText.split(":");
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            millis = (minutes * 60 + seconds) * 1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        totalTimeInMilliseconds = millis;
        return millis;
    }

    /**
     * Registers the broadcast receiver to listen for timer updates.
     */
    private void registerBroadcastReceiver() {
        if (!isReceiverRegistered) {
            registerReceiver(broadcastReceiver, new IntentFilter(TimerService.BROADCAST_ACTION));
            isReceiverRegistered = true;
        }
    }

    /**
     * Adjusts the layout size of the constraint layout.
     *
     * @param size The size to set.
     */
    private void adjustLayoutSize(int size) {
        ViewGroup.LayoutParams layoutParams = constraintLayout.getLayoutParams();
        layoutParams.height = size;
        layoutParams.width = size;
        constraintLayout.setLayoutParams(layoutParams);
    }

    /**
     * BroadcastReceiver to handle updates from TimerService.
     */
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("timerCancelled", false)) {
                handleTimerCancellation();
            } else {
                updateUI(intent);
            }
        }
    };

    /**
     * Handles the timer cancellation event.
     */
    private void handleTimerCancellation() {
        txtProgress.setText(initialTime);
        progressBar.setProgress(100);
        startButton.setText("START");
        adjustLayoutSize(ViewGroup.LayoutParams.WRAP_CONTENT);
        toggleVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterBroadcastReceiver();
        adjustLayoutSize(ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreTimerState();
        registerBroadcastReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterBroadcastReceiver();
    }

    /**
     * Unregisters the broadcast receiver.
     */
    private void unregisterBroadcastReceiver() {
        if (isReceiverRegistered) {
            unregisterReceiver(broadcastReceiver);
            isReceiverRegistered = false;
        }
    }

    /**
     * Updates the UI with the remaining time and progress.
     *
     * @param intent The intent containing the timer updates.
     */
    private void updateUI(Intent intent) {
        int timeLeft = intent.getIntExtra("timeLeft", 0);
        int mins = timeLeft / 60000;
        int secs = (timeLeft % 60000) / 1000;
        txtProgress.setText(String.format("%02d:%02d", mins, secs));
        int progress = (int) ((double) timeLeft / totalTimeInMilliseconds * 100);
        progressBar.setProgress(progress);
    }

    /**
     * Toggles the visibility of all views except the timer components.
     *
     * @param visibility The visibility state to set (View.VISIBLE, View.GONE, etc.).
     */
    private void toggleVisibility(int visibility) {
        btnMinus.setVisibility(visibility);
        btnPlus.setVisibility(visibility);
        for (Button button : buttons) {
            button.setVisibility(visibility);
        }
    }

    /**
     * Saves the state of the timer to SharedPreferences.
     *
     * @param isRunning Indicates if the timer is currently running.
     * @param buttonText The text to set for the start button.
     */
    private void saveTimerState(boolean isRunning, String buttonText) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isRunning", isRunning);
        editor.putString("buttonText", buttonText);
        editor.apply();
    }

    /**
     * Restores the state of the timer from SharedPreferences.
     */
    private void restoreTimerState() {
        boolean isRunning = preferences.getBoolean("isRunning", false);
        String buttonText = preferences.getString("buttonText", "START");
        startButton.setText(buttonText);
        if (isRunning) {
            btnCancel.setVisibility(View.VISIBLE);
            toggleVisibility(View.GONE);
        } else {
            btnCancel.setVisibility(View.GONE);
            toggleVisibility(View.VISIBLE);
        }
    }
}
