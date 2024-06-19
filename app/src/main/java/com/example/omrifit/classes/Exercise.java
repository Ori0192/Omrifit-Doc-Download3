package com.example.omrifit.classes;

public class Exercise {
    private String createdBy = "";
    private int uid;
    private int sets;
    private String name;
    private String target;
    private String repetitions;
    private String instructions;
    private String imageurl;

    /**
     * Default constructor for Firebase.
     */
    public Exercise() {
        // Default constructor required for Firebase
    }

    /**
     * Parameterized constructor to initialize an Exercise object.
     *
     * @param name         The name of the exercise.
     * @param target       The target muscle group of the exercise.
     * @param repetitions  The number of repetitions for the exercise.
     * @param instructions The instructions for performing the exercise.
     * @param imageurl     The URL of the exercise image.
     */
    public Exercise(String name, String target, String repetitions, String instructions, String imageurl) {
        this.name = name;
        this.target = target;
        this.repetitions = repetitions;
        this.instructions = instructions;
        this.imageurl = imageurl;
    }

    /**
     * Parameterized constructor to initialize an Exercise object with the creator's name.
     *
     * @param name         The name of the exercise.
     * @param target       The target muscle group of the exercise.
     * @param repetitions  The number of repetitions for the exercise.
     * @param instructions The instructions for performing the exercise.
     * @param imageurl     The URL of the exercise image.
     * @param createdBy    The creator of the exercise.
     */
    public Exercise(String name, String target, String repetitions, String instructions, String imageurl, String createdBy) {
        this.name = name;
        this.target = target;
        this.repetitions = repetitions;
        this.instructions = instructions;
        this.imageurl = imageurl;
        this.createdBy = createdBy;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(String repetitions) {
        this.repetitions = repetitions;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    /**
     * Gets the creator of the exercise.
     * If the createdBy field is empty, it returns "Omri".
     *
     * @return The creator of the exercise.
     */
    public String getCreatedBy() {
        if (createdBy.isEmpty()) {
            return "Omri";
        }
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Returns a string representation of the Exercise object.
     *
     * @return A string representation of the Exercise object.
     */
    @Override
    public String toString() {
        return "Exercise{" +
                "name='" + name + '\'' +
                ", target='" + target + '\'' +
                ", repetitions='" + repetitions + '\'' +
                ", instructions='" + instructions + '\'' +
                ", imageurl='" + imageurl + '\'' +
                '}';
    }
}
