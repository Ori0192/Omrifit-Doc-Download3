package com.example.omrifit.classes;

import java.util.ArrayList;

/**
 * Represents a muscle group and its associated exercises.
 */
public class Muscle {
    private String musclename;
    private ArrayList<Exercise> exercises;

    /**
     * Default constructor for Firebase.
     */
    public Muscle() {
        // Default constructor required for Firebase
    }

    /**
     * Constructor to initialize a Muscle object with specified muscle name and list of exercises.
     *
     * @param musclename The name of the muscle group.
     * @param exercises  The list of exercises for the muscle group.
     */
    public Muscle(String musclename, ArrayList<Exercise> exercises) {
        this.musclename = musclename;
        this.exercises = exercises;
    }

    /**
     * Constructor to initialize a Muscle object with specified muscle name.
     * Initializes the exercises list as an empty list.
     *
     * @param musclename The name of the muscle group.
     */
    public Muscle(String musclename) {
        this.musclename = musclename;
        this.exercises = new ArrayList<>();
    }

    public String getMusclename() {
        return musclename;
    }

    public void setMusclename(String musclename) {
        this.musclename = musclename;
    }

    public ArrayList<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(ArrayList<Exercise> exercises) {
        this.exercises = exercises;
    }

    /**
     * Returns a string representation of the Muscle object.
     *
     * @return A string representation of the Muscle object.
     */
    @Override
    public String toString() {
        StringBuilder exercisesStr = new StringBuilder();
        for (int i = 0; i < exercises.size(); i++) {
            exercisesStr.append("exercise").append(i).append(":").append(exercises.get(i).toString()).append("'");
        }
        return "Muscle{" +
                "musclename='" + musclename + '\'' +
                ", exercises=" + exercisesStr.toString() +
                '}';
    }
}
