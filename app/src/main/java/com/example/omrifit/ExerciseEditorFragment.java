package com.example.omrifit;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.omrifit.adapters.Exercise_Recyclerview_Adapter;
import com.example.omrifit.classes.Exercise;
import com.example.omrifit.classes.Message;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Fragment for managing and editing exercises.
 */
public class ExerciseEditorFragment extends Fragment implements Recyclerview_Interface {

    private Spinner spinChoiceToShow;
    private RecyclerView recyclerView;
    private String choice;
    private ArrayList<Exercise> exercises;
    private DatabaseReference exercisesRef;
    private FloatingActionButton fab;
    private View view;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private DatabaseReference myRef;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_editot_exercise, container, false);

        initializeUI(view);
        setupSpinners();
        setupEventListeners();

        return view;
    }

    /**
     * Initialize UI elements.
     */
    private void initializeUI(View view) {
        spinChoiceToShow = view.findViewById(R.id.spin_choisetoshow);
        recyclerView = view.findViewById(R.id.recyclerviewforworkoutfragment);
        fab = view.findViewById(R.id.fabButton);

        choice = "row exercises";
        exercisesRef = FirebaseDatabase.getInstance().getReference("exercises");
        myRef = FirebaseDatabase.getInstance().getReference("user_information").child(user.getUid());
    }

    /**
     * Set up the exercise category spinner.
     */
    private void setupSpinners() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, getExerciseCategories());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinChoiceToShow.setAdapter(adapter);
    }

    /**
     * Get the list of exercise categories.
     *
     * @return List of exercise categories.
     */
    private List<String> getExerciseCategories() {
        List<String> items = new ArrayList<>();
        items.add("row exercises");
        items.add("pull exercises");
        items.add("upper chest exercises");
        items.add("middle chest exercises");
        items.add("lower chest exercises");
        items.add("front shoulder exercises");
        items.add("middle shoulder exercises");
        items.add("back shoulder exercises");
        items.add("biceps exercises");
        items.add("triceps exercises");
        items.add("forearms exercises");
        items.add("abs exercises");
        items.add("hamstring exercises");
        items.add("quads exercises");
        items.add("calves exercises");
        return items;
    }

    /**
     * Set up event listeners for the UI elements.
     */
    private void setupEventListeners() {
        fab.setOnClickListener(v -> createExerciseRequest());

        spinChoiceToShow.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choice = parent.getItemAtPosition(position).toString();
                showDetails(choice);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });
    }

    /**
     * Shows a dialog to request a new exercise.
     */
    private void createExerciseRequest() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Request Exercise");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter exercise name");

        final TextView textView = new TextView(requireContext());
        textView.setText("Please enter the name of the exercise you want:");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        layout.addView(textView);
        layout.addView(input);

        builder.setView(layout);

        builder.setPositiveButton("Request", (dialog, which) -> {
            String exercise = input.getText().toString();
            handleExerciseRequest(exercise);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Handles the logic for requesting a new exercise.
     *
     * @param exercise The name of the exercise.
     */
    private void handleExerciseRequest(String exercise) {
        String prompt = "Can you add " + exercise + " to the exercises menu please?";
        myRef.child("Omri").child("chat").push().setValue(new Message(prompt, Message.SENT_BY_ME));
        ChatViewModel viewModel = new ChatViewModel();
        viewModel.getResponse("which of the following categories do you think this exercise " + exercise + " is the most belong to:" +
                " 1.row exercises 2.pull exercises 3.upper chest exercises 4.middle chest exercises 5.lower chest exercises 6.front shoulder exercises" +
                " 7.middle shoulder exercises 8.back shoulder exercises 9.biceps exercises 10.triceps exercises 11.forearms exercises 12.abs exercises 13.hamstring exercises" +
                "14.quads exercises 15.calves exercises 16. other. answer only but only the category number without any single extra token if you dont recognize the exercises return 16", new ChatResponseCallback() {
            @Override
            public void onSuccess(Chat chat) {
                if (chat.getPrompt().contains("16")) {
                    Toast.makeText(requireContext(), "Private", Toast.LENGTH_SHORT).show();
                    myRef.child("Omri").child("chat").push().setValue(new Message("Hi sorry but we couldn't recognize your exercise please try again", Message.SENT_BY_OMRI));
                } else {
                    Toast.makeText(requireContext(), chat.getPrompt(), Toast.LENGTH_SHORT).show();
                    try {
                        String category = getExerciseCategories().get(Integer.parseInt(chat.getPrompt()) - 1);
                        addExercise(exercise, category, "10-12", "none");
                    } catch (Exception e) {
                        myRef.child("Omri").child("chat").push().setValue(new Message("Hi there was an error, please try again", Message.SENT_BY_OMRI));
                        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                createSnackBar(exercise);
            }

            @Override
            public void onError(String error) {
                // Handle error
            }
        });
    }

    /**
     * Displays a snackbar with a message.
     *
     * @param exercise The exercise name.
     */
    private void createSnackBar(String exercise) {
        Snackbar snackbar = Snackbar.make(view, "We received your request for: " + exercise + ". We'll let you know if it's accepted.", Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blueblack));
        ((TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text)).setTextColor(Color.WHITE);

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar sb) {
                // Optional actions when Snackbar is shown
            }

            @Override
            public void onDismissed(Snackbar sb, int event) {
                // Optional actions when Snackbar is dismissed
            }
        });

        snackbar.show();

        new Handler().postDelayed(() -> fadeOutSnackbar(snackbar.getView()), 2500);
    }

    /**
     * Fades out the snackbar.
     *
     * @param view The snackbar view.
     */
    private void fadeOutSnackbar(View view) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(1000);
        fadeOut.start();
    }

    /**
     * Adds a new exercise to the database.
     *
     * @param name         The name of the exercise.
     * @param target       The target muscle group.
     * @param repetitions  The repetitions.
     * @param instructions The instructions.
     */
    public void addExercise(String name, String target, String repetitions, String instructions) {
        exercisesRef.child(target).child(normalizeString(name)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(requireContext(), "Exercise with this name already exists", Toast.LENGTH_SHORT).show();
                } else {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("https://api.pexels.com/v1/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    PexelsService service = retrofit.create(PexelsService.class);

                    service.searchPhotos(name).enqueue(new retrofit2.Callback<PexelsResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<PexelsResponse> call, retrofit2.Response<PexelsResponse> response) {
                            if (response.isSuccessful() && response.body() != null && !response.body().photos.isEmpty()) {
                                String imageUrl = response.body().photos.get(0).src.original;
                                Exercise exercise = new Exercise(name, target, repetitions, instructions, imageUrl);
                                exercise.setSets(4);
                                exercisesRef.child(target).child(normalizeString(name)).setValue(exercise);
                                myRef.child("Omri").child("chat").push().setValue(new Message("Hi your request for " + name +
                                        " has been accepted, you can find it in the exercises menu on the " + target + " section.", Message.SENT_BY_OMRI));
                                myRef.child("Omri").child("newMessages").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            myRef.child("Omri").child("newMessages").setValue(snapshot.getValue(Integer.class) + 1);
                                        } else {
                                            myRef.child("Omri").child("newMessages").setValue(1);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<PexelsResponse> call, Throwable t) {
                            t.printStackTrace();
                            Exercise exercise = new Exercise(name, target, repetitions, instructions, "none");
                            exercise.setSets(4);
                            exercisesRef.child(target).child(normalizeString(name)).setValue(exercise);
                            myRef.child("Omri").child("chat").push().setValue(new Message("Hi your request for " + name +
                                    " has been accepted, you can find it in the exercises menu on the " + target + " section.", Message.SENT_BY_OMRI));
                            myRef.child("Omri").child("newMessages").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        myRef.child("Omri").child("newMessages").setValue(snapshot.getValue(Integer.class) + 1);
                                    } else {
                                        myRef.child("Omri").child("newMessages").setValue(1);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("addExercise", "Failed to read value.", databaseError.toException());
            }
        });
    }

    /**
     * Normalizes a string by converting it to lowercase and removing punctuation and spaces.
     *
     * @param str The string to normalize.
     * @return The normalized string.
     */
    private static String normalizeString(String str) {
        str = str.toLowerCase();
        str = str.replaceAll("[\\p{Punct}\\s]", "");
        return str;
    }

    /**
     * Shows the details of the selected category.
     *
     * @param choice The selected category.
     */
    private void showDetails(String choice) {
        exercisesRef.child(choice).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                exercises = new ArrayList<>();
                for (DataSnapshot targetSnapshot : dataSnapshot.getChildren()) {
                    Exercise exercise = targetSnapshot.getValue(Exercise.class);
                    if (exercise != null) {
                        exercises.add(exercise);
                    }
                }
                updateRecyclerView(exercises);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("Failed to read value.", error.toException());
            }
        });
    }

    /**
     * Updates the RecyclerView with the provided list of exercises.
     *
     * @param exercises The list of exercises.
     */
    private void updateRecyclerView(ArrayList<Exercise> exercises) {
        Toast.makeText(requireContext(), "update: " + exercises.size(), Toast.LENGTH_SHORT).show();
        Exercise_Recyclerview_Adapter adapter = new Exercise_Recyclerview_Adapter(requireContext(), exercises, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    @Override
    public void ClickOnce(int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(choice).child(exercises.get(position).getName());
        ref.removeValue();
    }

    @Override
    public void onclick(int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("exercises").child(choice).child(exercises.get(position).getName());
        ref.removeValue();
        showDetails(choice);
    }
}
