package com.example.omrifit.sport;

import static com.example.omrifit.fragments.HomeFragment.setTodayWorkout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.omrifit.R;
import com.example.omrifit.classes.Exercise;
import com.example.omrifit.classes.Muscle;
import com.example.omrifit.classes.Workout;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Fragment for setting up workout schedules.
 */
public class WorkoutSettingsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private TextView title;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private ListView listView;
    private CompactCalendarView compactCalendarView;
    private String selectedWorkoutType = null;
    private int color = 0;
    private Calendar calendar2 = Calendar.getInstance();
    private int[] colors = new int[]{Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.GRAY};
    private Spinner spinnerSunday, spinnerMonday, spinnerTuesday, spinnerWednesday, spinnerThursday, spinnerFriday, spinnerSaturday;
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
    private static final long DOUBLE_CLICK_TIME_DELTA = 600; // 600 milliseconds

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ws, container, false);

        initializeUI(view);

        loadUserWorkouts(view);

        return view;
    }

    /**
     * Initializes the UI elements and Firebase authentication.
     *
     * @param view the root view of the fragment
     */
    private void initializeUI(View view) {
        listView = view.findViewById(R.id.lv);
        compactCalendarView = view.findViewById(R.id.compactcalendar_view);
        compactCalendarView.setUseThreeLetterAbbreviation(true);

        calendar2.set(Calendar.DAY_OF_MONTH, 1); // Set the day to 1, the first day of the month

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("user_information").child(user.getUid());

        spinnerSunday = view.findViewById(R.id.spinner_sunday);
        spinnerMonday = view.findViewById(R.id.spinner_monday);
        spinnerTuesday = view.findViewById(R.id.spinner_tuesday);
        spinnerWednesday = view.findViewById(R.id.spinner_wednesday);
        spinnerThursday = view.findViewById(R.id.spinner_thursday);
        spinnerFriday = view.findViewById(R.id.spinner_friday);
        spinnerSaturday = view.findViewById(R.id.spinner_saturday);
    }

    /**
     * Loads the user workouts from Firebase and sets up listeners.
     *
     * @param view the root view of the fragment
     */
    private void loadUserWorkouts(View view) {
        myRef.child("workouts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String[] workoutAccordingToDayPos = new String[7];
                ArrayList<String> workoutsNum = new ArrayList<>();
                for (int i = 1; i < 8; i++) {
                    if (dataSnapshot.child("days").child(String.valueOf(i)).getValue(String.class) != null || dataSnapshot.child("days").child(String.valueOf(i)).getValue(String.class).equals("Rest")) {
                        workoutAccordingToDayPos[i - 1] = dataSnapshot.child("days").child(String.valueOf(i)).getValue(String.class);
                    } else {
                        workoutAccordingToDayPos[i - 1] = "Rest";
                    }
                }
                for (DataSnapshot shot : dataSnapshot.getChildren()) {
                    if (shot.child("type").getValue(String.class) != null && !"Rest".equals(shot.child("type").getValue(String.class))) {
                        workoutsNum.add(shot.child("type").getValue(String.class));
                    }
                }
                updateWorkoutListView(workoutsNum);
                setOnClickDate(view, workoutAccordingToDayPos, workoutsNum);
                handleMonthScroll(view, workoutAccordingToDayPos, Calendar.getInstance().getTime(), workoutsNum);
                setUpSpinners(workoutsNum, workoutAccordingToDayPos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("DBError", "loadPost:onCancelled", error.toException());
            }
        });
    }


    /**
     * Sets up the spinners with workout options.
     *
     * @param workoutsNum              the list of workout types
     * @param workoutAccordingToDayPos the array of user workouts according to the day
     */
    private void setUpSpinners(ArrayList<String> workoutsNum, String[] workoutAccordingToDayPos) {

        ArrayList<String> cotemporaryWorkoutNum = new ArrayList<>();
        for (String str : workoutsNum) {
            cotemporaryWorkoutNum.add(str);
        }
        cotemporaryWorkoutNum.remove("+");
        cotemporaryWorkoutNum.add("Rest");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, cotemporaryWorkoutNum);
        adapter.setDropDownViewResource(R.layout.spinner_item);

        spinnerSunday.setAdapter(adapter);
        spinnerMonday.setAdapter(adapter);
        spinnerTuesday.setAdapter(adapter);
        spinnerWednesday.setAdapter(adapter);
        spinnerThursday.setAdapter(adapter);
        spinnerFriday.setAdapter(adapter);
        spinnerSaturday.setAdapter(adapter);

        // Set the selected workout for each day
        spinnerSunday.setSelection(cotemporaryWorkoutNum.indexOf(workoutAccordingToDayPos[0]));
        spinnerMonday.setSelection(cotemporaryWorkoutNum.indexOf(workoutAccordingToDayPos[1]));
        spinnerTuesday.setSelection(cotemporaryWorkoutNum.indexOf(workoutAccordingToDayPos[2]));
        spinnerWednesday.setSelection(cotemporaryWorkoutNum.indexOf(workoutAccordingToDayPos[3]));
        spinnerThursday.setSelection(cotemporaryWorkoutNum.indexOf(workoutAccordingToDayPos[4]));
        spinnerFriday.setSelection(cotemporaryWorkoutNum.indexOf(workoutAccordingToDayPos[5]));
        spinnerSaturday.setSelection(cotemporaryWorkoutNum.indexOf(workoutAccordingToDayPos[6]));

        // Add selection listeners
        spinnerSunday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                myRef.child("workouts").child("days").child("1").setValue(cotemporaryWorkoutNum.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerMonday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                myRef.child("workouts").child("days").child("2").setValue(cotemporaryWorkoutNum.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerTuesday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                myRef.child("workouts").child("days").child("3").setValue(cotemporaryWorkoutNum.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerWednesday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                myRef.child("workouts").child("days").child("4").setValue(cotemporaryWorkoutNum.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerThursday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                myRef.child("workouts").child("days").child("5").setValue(cotemporaryWorkoutNum.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerFriday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                myRef.child("workouts").child("days").child("6").setValue(cotemporaryWorkoutNum.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerSaturday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                myRef.child("workouts").child("days").child("7").setValue(cotemporaryWorkoutNum.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Sets up the listener for date click and month scroll on the calendar view.
     *
     * @param view        the root view of the fragment
     * @param workouts    the list of user workouts
     * @param workoutsNum
     */
    private void setOnClickDate(View view, String[] workouts, ArrayList<String> workoutsNum) {
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                handleMonthScroll(view, workouts, firstDayOfNewMonth, workoutsNum);
            }

            @Override
            public void onDayClick(Date dateClicked) {
                handleDayClick(dateClicked);
            }
        });
    }

    /**
     * Handles the month scroll event to load events for the specific month.
     *
     * @param view                     the root view of the fragment
     * @param workoutAccordingToDayPos the array of user workouts according to the day
     * @param firstDayOfNewMonth       the first day of the new month
     * @param workoutsNum
     */
    private void handleMonthScroll(View view, String[] workoutAccordingToDayPos, Date firstDayOfNewMonth, ArrayList<String> workoutsNum) {
        title = view.findViewById(R.id.calendar_title);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        title.setText(sdf.format(firstDayOfNewMonth));

        compactCalendarView.removeAllEvents();
        for (int i = 1; i < 7; i++) {
            if (workoutAccordingToDayPos[i] != null) {
                if (!workoutAccordingToDayPos[i].equals("Rest")) {
                    loadEventsForSpecificDayInMonth(firstDayOfNewMonth, i + 1, workoutAccordingToDayPos[i], colors[workoutsNum.indexOf(workoutAccordingToDayPos[i])]);
                }
            }
        }
    }

    /**
     * Handles the day click event on the calendar.
     *
     * @param dateClicked the date clicked by the user
     */
    private void handleDayClick(Date dateClicked) {
        Calendar today = Calendar.getInstance();
        if (today.getTime().before(dateClicked)) {
            if (selectedWorkoutType != null) {
                showAddEventDialog(dateClicked);
            }
            List<Event> events = compactCalendarView.getEvents(dateClicked);
            if (!events.isEmpty()) {
                showEventDetailsDialog(events.get(0), dateClicked);
            }
        } else {
            Toast.makeText(requireContext(), "The date is invalid", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Shows a dialog to add an event to the calendar.
     *
     * @param dateClicked the date clicked by the user
     */
    private void showAddEventDialog(Date dateClicked) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Add event")
                .setMessage("do you wish to add " + selectedWorkoutType + " as event?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    addEventToCalendar(dateClicked, selectedWorkoutType, color);
                    selectedWorkoutType = null;
                })
                .setNegativeButton("No", (dialog, which) -> selectedWorkoutType = null)
                .show();
    }

    /**
     * Adds an event to the calendar.
     *
     * @param date        the date to add the event
     * @param workoutType the type of workout
     * @param color       the color of the event
     */
    private void addEventToCalendar(Date date, String workoutType, int color) {
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        if (date.equals(currentDate)) {
            setTodayWorkout();
        } else {
            long timeInMillis = date.getTime();
            Event event = new Event(color, timeInMillis, workoutType);
            compactCalendarView.addEvent(event);
            handleExceptions(date);
        }
    }

    /**
     * Handles exceptions for the specified date.
     *
     * @param date the date to handle exceptions
     */
    private void handleExceptions(Date date) {
        myRef.child("exceptions").child("remove").child(sdf2.format(date)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    myRef.child("exceptions").child("remove").child(sdf2.format(date)).removeValue();
                } else {
                    myRef.child("exceptions").child("add").child(sdf2.format(date)).setValue(1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("DBError", "handleExceptions:onCancelled", error.toException());
            }
        });
    }

    /**
     * Shows a dialog with event details.
     *
     * @param event       the event to show details for
     * @param dateClicked the date clicked by the user
     */
    private void showEventDetailsDialog(Event event, Date dateClicked) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Event details:")
                .setMessage("details: " + event.getData())
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss())
                .setNegativeButton("Remove", (dialog, id) -> removeEventFromDate(dateClicked))
                .create()
                .show();
    }

    /**
     * Removes an event from the specified date.
     *
     * @param dateClicked the date to remove the event from
     */
    private void removeEventFromDate(Date dateClicked) {
        compactCalendarView.removeEvents(dateClicked);
        handleExceptions(dateClicked);
    }

    /**
     * Loads events for a specific day in the month.
     *
     * @param firstDayOfNewMonth the first day of the new month
     * @param dayOfWeek          the day of the week to load events for
     * @param eventType          the type of event
     * @param color              the color of the event
     */
    private void loadEventsForSpecificDayInMonth(Date firstDayOfNewMonth, int dayOfWeek, String eventType, int color) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(firstDayOfNewMonth);

        Calendar today = Calendar.getInstance();
        String titleText = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar2.getTime());

        if (cal.compareTo(calendar2) >= 0 || cal.compareTo(today) >= 0 || title.getText().toString().equals(titleText)) {
            int month = cal.get(Calendar.MONTH);
            while (cal.get(Calendar.MONTH) == month) {
                if (cal.get(Calendar.DAY_OF_WEEK) == dayOfWeek && !cal.before(today)) {
                    Calendar checkCal = (Calendar) cal.clone();
                    if (cal.get(Calendar.DAY_OF_WEEK) == dayOfWeek) {
                        checkAndAddEvent("remove", checkCal, checkCal.getTime(), dayOfWeek, eventType, color);
                    } else {
                        checkAndAddEvent("add", checkCal, checkCal.getTime(), dayOfWeek, eventType, color);
                    }
                }
                cal.add(Calendar.DATE, 1);
            }
        }
    }

    /**
     * Checks if an event can be added or removed based on exceptions and adds/removes it accordingly.
     *
     * @param target     the target operation ("add" or "remove")
     * @param cal        the calendar instance
     * @param date       the date to check
     * @param dayOfWeek  the day of the week
     * @param eventType  the type of event
     * @param color      the color of the event
     */
    private void checkAndAddEvent(String target, Calendar cal, Date date, int dayOfWeek, String eventType, int color) {
        DatabaseReference exceptionsRef = myRef.child("exceptions").child(target);
        String dateString = sdf2.format(date);

        exceptionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isException = dataSnapshot.child(dateString).exists();
                if ((!isException && target.equals("remove")) || (isException && target.equals("add"))) {
                    cal.set(Calendar.HOUR_OF_DAY, 19);
                    cal.set(Calendar.MINUTE, 0);
                    long timeInMillis = cal.getTimeInMillis();
                    Event event = new Event(color, timeInMillis, eventType);
                    compactCalendarView.addEvent(event);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("DBError", "checkAndAddEvent:onCancelled", databaseError.toException());
            }
        });
    }

    /**
     * Updates the ListView with workout types and sets up item click and long click listeners.
     *
     * @param workoutNum the list of workout types
     */
    private void updateWorkoutListView(ArrayList<String> workoutNum) {
        if (!workoutNum.contains("+") && workoutNum.size() < 6) {
            workoutNum.add("+");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Objects.requireNonNull(getContext()), R.layout.workout_item, workoutNum) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                int colorIndex = position % colors.length;
                view.setBackgroundColor(colors[colorIndex]);
                return view;
            }
        };

        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener((parent, view, position, id) -> handleItemLongClick(adapter, workoutNum, position));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private long lastClickTime = 0;
            private String lastClickedItem = null;
            private Handler handler = new Handler();

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (!selectedItem.equals("+")) {
                    long clickTime = System.currentTimeMillis();

                    if (selectedItem.equals(lastClickedItem) && clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                        handler.removeCallbacksAndMessages(null);
                        lastClickedItem = null;
                        selectedWorkoutType = selectedItem;
                        color = colors[position];
                    } else {
                        lastClickedItem = selectedItem;
                        handler.postDelayed(() -> {
                            if (lastClickedItem.equals(selectedItem)) {
                                onItemSingleClick(selectedItem);
                            }
                        }, DOUBLE_CLICK_TIME_DELTA);
                    }
                    lastClickTime = clickTime;
                } else {
                    if (workoutNum.size() - 1 < 6) {
                        createNewWorkout(workoutNum.size() - 1);
                    } else {
                        Toast.makeText(requireContext(), "You've reached the maximum amount of workouts", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            /**
             * Handles the single click event on a workout item.
             *
             * @param item the clicked item
             */
            private void onItemSingleClick(String item) {
                if (!item.equals("+")) {
                    Intent intent = new Intent(requireContext(), DetailWorkout.class);
                    intent.putExtra("type", item);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * Handles the long click event on a workout item.
     *
     * @param adapter    the ArrayAdapter for the ListView
     * @param workoutNum the list of workout types
     * @param position   the position of the clicked item
     * @return true if the click was handled, false otherwise
     */
    private boolean handleItemLongClick(ArrayAdapter<String> adapter, ArrayList<String> workoutNum, int position) {
        if (position != workoutNum.size() - 1) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteWorkoutItem(adapter, workoutNum, position))
                    .setNegativeButton("Cancel", null)
                    .show();
        }
        return true;
    }

    /**
     * Deletes a workout item from the list and updates the database.
     *
     * @param adapter    the ArrayAdapter for the ListView
     * @param workoutNum the list of workout types
     * @param position   the position of the item to delete
     */
    private void deleteWorkoutItem(ArrayAdapter<String> adapter, ArrayList<String> workoutNum, int position) {
        String workoutType = workoutNum.get(position);
        myRef.child("workouts").child(workoutType).removeValue();
        workoutNum.remove(position);
        adapter.notifyDataSetChanged();
    }


    /**
     * Creates a new workout and updates the ListView.
     *
     * @param workoutNumLength the current length of the workout list
     */
    private void createNewWorkout(int workoutNumLength) {
        String type = ((char) (65 + workoutNumLength)) + "";
        ArrayList<Exercise> exercises = new ArrayList<>(Arrays.asList(new Exercise("Bent over row", "row exercises", "10-12", "example", "example")));
        ArrayList<Muscle> muscles = new ArrayList<>(Arrays.asList(new Muscle("", exercises)));
        Workout workout = new Workout(type, type, muscles);
        myRef.child("workouts").child(type).setValue(workout);
        updateWorkoutListView(new ArrayList<>(Arrays.asList(type)));
    }
}
