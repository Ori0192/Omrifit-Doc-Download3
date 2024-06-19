package com.example.omrifit.classes;

/**
 * Represents a task with specific details such as name, type, description, target, duration, and status.
 */
public class Task {
    public static final String NUTRITION_TASK = "nutritionTask";
    public static final String STEPS_TASK = "stepsTask";
    public static final String EXERCISE_TASK = "exerciseTask";

    private String name;
    private String type;
    private String description;
    private boolean isCompleted;
    private String target;
    private int duration;
    private boolean active;

    /**
     * Default constructor for Firebase.
     */
    public Task() {
        // Default constructor required for Firebase
    }

    /**
     * Constructor to initialize a Task object with specified attributes.
     *
     * @param name        The name of the task.
     * @param type        The type of the task (e.g., nutrition, steps, exercise).
     * @param description The description of the task.
     * @param target      The target or goal of the task.
     * @param duration    The duration of the task in days.
     */
    public Task(String name, String type, String description, String target, int duration) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.isCompleted = false;
        this.target = target;
        this.duration = duration;
        this.active = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns a string representation of the Task object.
     *
     * @return A string representation of the Task object.
     */
    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
