package com.example.omrifit.classes;

/**
 * Represents a specific time with hours and minutes.
 */
public class Time {
    private int hours;
    private int minutes;

    /**
     * Default constructor for Firebase.
     */
    public Time() {
        // Default constructor required for Firebase
    }

    /**
     * Constructor to initialize a Time object with specified hours and minutes.
     *
     * @param hours   The hours part of the time.
     * @param minutes The minutes part of the time.
     */
    public Time(int hours, int minutes) {
        this.hours = hours;
        this.minutes = minutes;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    /**
     * Returns a string representation of the Time object in the format HH:MM.
     *
     * @return A string representation of the Time object.
     */
    @Override
    public String toString() {
        return String.format("%02d:%02d", hours, minutes);
    }
}
