package com.example.omrifit;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.omrifit.adapters.TaskAdapter;
import com.example.omrifit.classes.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Activity for managing tasks.
 */
public class TasksActivity extends AppCompatActivity {

    private CardView cardView;
    private FloatingActionButton floatingActionButton;
    private EditText durationEditText, titleEditText, descriptionEditText, targetEditText;
    private Spinner typeSpinner;
    private Button addButton;
    private ArrayList<Task> tasks = new ArrayList<>();
    private TaskAdapter taskAdapter;
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("tasks");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_manage_tasks);

        initializeUI();
        setupTaskTypeSpinner();
        setupDatabaseListener();
        setupButtonListeners();
    }

    /**
     * Initializes the UI components.
     */
    private void initializeUI() {
        cardView = findViewById(R.id.cv_add_task);
        floatingActionButton = findViewById(R.id.floatingActionButton3);
        titleEditText = findViewById(R.id.editTextTextTitle);
        descriptionEditText = findViewById(R.id.editTextTextDescription);
        targetEditText = findViewById(R.id.editTextTextTarget);
        typeSpinner = findViewById(R.id.spinnerType);
        addButton = findViewById(R.id.btn_continue2);
        recyclerView = findViewById(R.id.rv);
        durationEditText = findViewById(R.id.editTextTextDuration);
    }

    /**
     * Sets up the task type spinner with predefined task types.
     */
    private void setupTaskTypeSpinner() {
        String[] taskTypes = {Task.NUTRITION_TASK, Task.STEPS_TASK, Task.EXERCISE_TASK};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, taskTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
    }

    /**
     * Sets up the Firebase database listener to fetch and display tasks.
     */
    private void setupDatabaseListener() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                tasks.clear();
                int currentTaskPosition = -1;
                for (DataSnapshot shot : snapshot.getChildren()) {
                    Task task = shot.getValue(Task.class);
                    if (task != null && shot.child("isCompleted").child(user.getUid()).exists()) {
                        task.setCompleted(shot.child("isCompleted").child(user.getUid()).getValue(Boolean.class));
                    }
                    tasks.add(task);
                }

                currentTaskPosition = findCurrentTaskPosition(tasks);

                setupRecyclerView(tasks);

                if (currentTaskPosition != -1) {
                    recyclerView.scrollToPosition(currentTaskPosition);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(TasksActivity.this, "Failed to load tasks.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Finds the position of the current task that is not completed.
     *
     * @param tasks The list of tasks.
     * @return The position of the current task, or -1 if all tasks are completed.
     */
    private int findCurrentTaskPosition(ArrayList<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            if (!tasks.get(i).isCompleted()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sets up listeners for the buttons in the activity.
     */
    private void setupButtonListeners() {
        floatingActionButton.setOnClickListener(v -> cardView.setVisibility(View.VISIBLE));

        addButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            String target = targetEditText.getText().toString();
            String type = typeSpinner.getSelectedItem().toString();
            int duration = Integer.parseInt(durationEditText.getText().toString());

            if (isValidInput(title, description, target)) {
                Task newTask = new Task(title, type, description, target, duration);
                tasks.add(newTask);
                myRef.setValue(tasks);
                setupRecyclerView(tasks);
                clearInputFields();
                cardView.setVisibility(View.GONE);
            } else {
                Toast.makeText(TasksActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Validates the input fields.
     *
     * @param title       The title of the task.
     * @param description The description of the task.
     * @param target      The target of the task.
     * @return True if all input fields are filled, false otherwise.
     */
    private boolean isValidInput(String title, String description, String target) {
        return !title.isEmpty() && !description.isEmpty() && !target.isEmpty();
    }

    /**
     * Clears the input fields after adding a task.
     */
    private void clearInputFields() {
        titleEditText.setText("");
        descriptionEditText.setText("");
        targetEditText.setText("");
    }

    /**
     * Sets up the RecyclerView with the provided list of tasks.
     *
     * @param tasks The list of tasks.
     */
    private void setupRecyclerView(ArrayList<Task> tasks) {
        taskAdapter = new TaskAdapter(this, tasks);
        recyclerView.setAdapter(taskAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
