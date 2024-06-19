package com.example.omrifit.classes;

import android.os.Build;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a workout plan, including its type, name, target muscles, and scheduled days.
 */
public class Workout {
    private String type;
    private String name;
    private List<Muscle> musclesForWorkout;

    /**
     * Default constructor for Firebase.
     */
    public Workout() {
        // Default constructor required for Firebase
    }



    /**
     * Constructor to initialize a Workout object with specified attributes, excluding days.
     *
     * @param type             The type of workout (e.g., Cardio, Strength).
     * @param name             The name of the workout.
     * @param musclesForWorkout The list of muscles targeted by the workout.
     */
    public Workout(String type, String name, List<Muscle> musclesForWorkout) {
        this.type = type;
        this.name = name;
        this.musclesForWorkout = musclesForWorkout;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Muscle> getMusclesForWorkout() {
        return musclesForWorkout;
    }

    public void setMusclesForWorkout(List<Muscle> musclesForWorkout) {
        this.musclesForWorkout = musclesForWorkout;
    }


    /**
     * Removes a muscle from the workout based on its name.
     *
     * @param muscleName The name of the muscle to be removed.
     */
    public void removeMuscle(String muscleName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            musclesForWorkout.removeIf(muscle -> Objects.equals(muscle.getMusclename(), muscleName));
        }
    }

    /**
     * Returns a string representation of the Workout object.
     *
     * @return A string representation of the Workout object.
     */
    @Override
    public String toString() {
        StringBuilder musclesForWorkoutStr = new StringBuilder();
        for (int i = 0; i < musclesForWorkout.size(); i++) {
            musclesForWorkoutStr.append("muscle").append(i).append(":").append(musclesForWorkout.get(i).toString()).append("'");
        }
        return "Workout{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", musclesForWorkout=" + musclesForWorkoutStr +
                '}';
    }
}
