package com.example.omrifit;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * TimerService is a background service for managing a countdown timer.
 * It provides actions to start, pause, continue, and cancel the timer.
 */
public class TimerService extends Service {

    public static final String BROADCAST_ACTION = "com.example.omrifit.TimerService";
    private static final String CHANNEL_ID = "TimerServiceChannel";
    private final Handler handler = new Handler();
    private long endTime;
    private long timeLeftWhenStopped = 0;
    private Intent intent;
    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
        mediaPlayer = MediaPlayer.create(this, R.raw.household_alarm_clock_beep_tone); // Ensure you have an alarm_sound.mp3 in res/raw
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case "CONTINUE":
                    continueTimer();
                    break;
                case "PAUSE":
                    pauseTimer();
                    break;
                case "CANCEL":
                    cancelTimer();
                    break;
                case "START":
                    long countdownTime = intent.getLongExtra("countdownTime", 0);
                    startTimer(countdownTime);
                    break;
            }
        }
        return START_STICKY;
    }

    /**
     * Starts the countdown timer.
     *
     * @param countdownTime The duration of the timer in milliseconds.
     */
    private void startTimer(long countdownTime) {
        endTime = SystemClock.elapsedRealtime() + countdownTime;
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000);
    }

    /**
     * Cancels the countdown timer.
     */
    private void cancelTimer() {
        handler.removeCallbacks(sendUpdatesToUI);
        Intent cancelIntent = new Intent(BROADCAST_ACTION);
        cancelIntent.putExtra("timerCancelled", true);
        sendBroadcast(cancelIntent);
        stopAlarm();
    }

    /**
     * Runnable to send updates to the UI.
     */
    private final Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            long timeLeft = endTime - SystemClock.elapsedRealtime();
            if (timeLeft <= 0) {
                timeLeft = 0;
                handler.removeCallbacks(this);
                intent.putExtra("timerFinished", true);
                sendBroadcast(intent);
                playAlarm();
                showNotification();
            } else {
                handler.postDelayed(this, 1000);
            }
            intent.putExtra("timeLeft", (int) timeLeft);
            sendBroadcast(intent);
        }
    };

    /**
     * Continues the countdown timer.
     */
    private void continueTimer() {
        endTime = SystemClock.elapsedRealtime() + timeLeftWhenStopped;
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000);
    }

    /**
     * Pauses the countdown timer.
     */
    private void pauseTimer() {
        timeLeftWhenStopped = endTime - SystemClock.elapsedRealtime();
        handler.removeCallbacks(sendUpdatesToUI);
    }

    /**
     * Plays the alarm sound.
     */
    private void playAlarm() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    /**
     * Stops the alarm sound.
     */
    private void stopAlarm() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.prepareAsync(); // Prepare the MediaPlayer to be reused
        }
    }

    /**
     * Creates a notification channel for Android O and above.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Timer Service Channel";
            String description = "Channel for Timer Service notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Shows a notification when the timer finishes.
     */
    private void showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_timer) // Ensure you have a timer icon in res/drawable
                .setContentTitle("Timer Finished")
                .setContentText("The countdown timer has finished.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(sendUpdatesToUI);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
