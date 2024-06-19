package com.example.omrifit.classes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom view that draws a sinusoidal path to visually represent the number of tasks completed.
 */
public class CustomPathView extends View {
    private Paint paint;
    private int tasksCompleted;

    /**
     * Constructor that initializes the view.
     *
     * @param context The context of the view.
     * @param attrs   The attribute set for the view.
     */
    public CustomPathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initializes the paint object and its properties.
     */
    private void init() {
        paint = new Paint();
        paint.setColor(Color.WHITE); // Set color to white
        paint.setStrokeWidth(20f); // Set stroke width
        paint.setStyle(Paint.Style.STROKE); // Set style to stroke
    }

    /**
     * Sets the number of tasks completed and invalidates the view to trigger a redraw.
     *
     * @param tasks The number of tasks completed.
     */
    public void setTasksCompleted(int tasks) {
        this.tasksCompleted = tasks;
        invalidate(); // Redraw the view
    }

    /**
     * Draws the custom path on the canvas.
     *
     * @param canvas The canvas on which the background will be drawn.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Path path = new Path();

        float amplitude = 50; // Amplitude of the wave
        float frequency = 0.05f; // Frequency of the wave

        path.moveTo(getWidth() / 2, 0); // Start from the top center

        int totalLength = tasksCompleted * 100; // Calculate the total length based on tasks completed

        // Draw the sinusoidal path
        for (int y = 0; y <= totalLength; y++) {
            float x = getWidth() / 2 + amplitude * (float) Math.sin(frequency * y);
            path.lineTo(x, y);
        }

        // Draw the path on the canvas
        canvas.drawPath(path, paint);
    }
}
