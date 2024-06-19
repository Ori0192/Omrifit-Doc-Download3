package com.example.omrifit.classes;

public class BodyMeasure {
    private String date;
    private int weight;

    // Default constructor
    public BodyMeasure() {
    }

    // Parameterized constructor
    public BodyMeasure(String date, int weight) {
        this.date = date;
        this.weight = weight;
    }

    // Getter for date
    public String getDate() {
        return date;
    }

    // Getter for weight
    public int getWeight() {
        return weight;
    }

    // Setter for date
    public void setDate(String date) {
        this.date = date;
    }

    // Setter for weight
    public void setWeight(int weight) {
        this.weight = weight;
    }
}
