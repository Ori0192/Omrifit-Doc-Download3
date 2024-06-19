package com.example.omrifit.classes;

/**
 * Represents a product with nutritional information.
 */
public class Product {
    public static final String PER_UNIT = "per unit";
    public static final String PER_100G = "per 100g";

    private String name;
    private String measurementform;
    private int calories;
    private int protein;
    private int carbs;
    private int fat;

    /**
     * Default constructor for Firebase.
     */
    public Product() {
        // Default constructor required for Firebase
    }

    /**
     * Constructor to initialize a Product object with specified attributes.
     *
     * @param name            The name of the product.
     * @param measurementForm The form of measurement (e.g., per unit, per 100g).
     * @param calories        The number of calories in the product.
     * @param protein         The amount of protein in grams.
     * @param carbs           The amount of carbohydrates in grams.
     * @param fat             The amount of fat in grams.
     */
    public Product(String name, String measurementForm, int calories, int protein, int carbs, int fat) {
        this.name = name;
        this.measurementform = measurementForm;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMeasurementform() {
        return measurementform;
    }

    public void setMeasurementform(String measurementform) {
        this.measurementform = measurementform;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public int getProtein() {
        return protein;
    }

    public void setProtein(int protein) {
        this.protein = protein;
    }

    public int getCarbs() {
        return carbs;
    }

    public void setCarbs(int carbs) {
        this.carbs = carbs;
    }

    public int getFat() {
        return fat;
    }

    public void setFat(int fat) {
        this.fat = fat;
    }
}
