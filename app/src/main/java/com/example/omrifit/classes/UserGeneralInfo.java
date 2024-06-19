package com.example.omrifit.classes;

/**
 * Represents general information about a user, including fitness targets, daily activity, meal number, and workout frequency.
 */
public class UserGeneralInfo {
    private String target;
    private String dailyActivity;
    private String experience;
    private int mealNum;
    private int workoutsAWeek;

    /**
     * Default constructor for Firebase.
     */
    public UserGeneralInfo() {
        // Default constructor required for Firebase
    }

    /**
     * Constructor to initialize a UserGeneralInfo object with specified attributes.
     *
     * @param target       The fitness target of the user.
     * @param dailyActivity The daily activity level of the user.
     * @param mealNum      The number of meals per day.
     */
    public UserGeneralInfo(String target, String dailyActivity, int mealNum) {
        this.target = target;
        this.dailyActivity = dailyActivity;
        this.mealNum = mealNum;
    }

    /**
     * Constructor to initialize a UserGeneralInfo object with specified attributes excluding meal number.
     *
     * @param target       The fitness target of the user.
     * @param dailyActivity The daily activity level of the user.
     */
    public UserGeneralInfo(String target, String dailyActivity) {
        this.target = target;
        this.dailyActivity = dailyActivity;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public int getWorkoutsAWeek() {
        return workoutsAWeek;
    }

    public void setWorkoutsAWeek(int workoutsAWeek) {
        this.workoutsAWeek = workoutsAWeek;
    }

    public int getMealNum() {
        return mealNum;
    }

    public void setMealNum(int mealNum) {
        this.mealNum = mealNum;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getDailyActivity() {
        return dailyActivity;
    }

    public void setDailyActivity(String dailyActivity) {
        this.dailyActivity = dailyActivity;
    }
}
