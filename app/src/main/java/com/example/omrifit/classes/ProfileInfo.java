package com.example.omrifit.classes;

/**
 * Represents the profile information of a user.
 */
public class ProfileInfo {
    private String name;
    private String gender;
    private String id;
    private int weight;
    private int height;
    private int age;
    private int beginWeight;

    /**
     * Default constructor for Firebase.
     */
    public ProfileInfo() {
        // Default constructor required for Firebase
    }

    /**
     * Constructor to initialize a ProfileInfo object with specified attributes.
     *
     * @param name        The name of the user.
     * @param gender      The gender of the user.
     * @param id          The ID of the user.
     * @param weight      The current weight of the user.
     * @param height      The height of the user.
     * @param age         The age of the user.
     * @param beginWeight The initial weight of the user.
     */
    public ProfileInfo(String name, String gender, String id, int weight, int height, int age, int beginWeight) {
        this.name = name;
        this.gender = gender;
        this.id = id;
        this.weight = weight;
        this.height = height;
        this.age = age;
        this.beginWeight = beginWeight;
    }

    /**
     * Constructor to initialize a ProfileInfo object with specified attributes, excluding initial weight.
     *
     * @param name   The name of the user.
     * @param gender The gender of the user.
     * @param weight The current weight of the user.
     * @param height The height of the user.
     * @param age    The age of the user.
     * @param id     The ID of the user.
     */
    public ProfileInfo(String name, String gender, int weight, int height, int age, String id) {
        this.name = name;
        this.gender = gender;
        this.weight = weight;
        this.height = height;
        this.age = age;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getBeginWeight() {
        return beginWeight;
    }

    public void setBeginWeight(int beginWeight) {
        this.beginWeight = beginWeight;
    }

    /**
     * Returns a string representation of the ProfileInfo object.
     *
     * @return A string representation of the ProfileInfo object.
     */
    @Override
    public String toString() {
        return "ProfileInfo{" +
                "name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", id='" + id + '\'' +
                ", weight=" + weight +
                ", height=" + height +
                ", age=" + age +
                ", beginWeight=" + beginWeight +
                '}';
    }
}
