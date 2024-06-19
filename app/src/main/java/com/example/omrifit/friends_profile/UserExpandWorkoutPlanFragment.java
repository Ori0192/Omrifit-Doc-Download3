//package com.example.omrifit.friends_profile;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.omrifit.R;
//import com.example.omrifit.adapters.RV_workout_adapter_for_user_expand;
//import com.example.omrifit.classes.ProfileInfo;
//import com.example.omrifit.classes.Workout;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//
//import java.lang.reflect.Type;
//import java.util.ArrayList;
//import java.util.Calendar;
//
//public class UserExpandWorkoutPlanFragment extends Fragment  {
//    FirebaseDatabase database = FirebaseDatabase.getInstance();
//    TextView txt_day,txt_type;
//    ArrayList<Workout> workouts=new ArrayList<>();
//    RecyclerView recyclerView;
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        View view = inflater.inflate(R.layout.fragment_workout_fragemant, container, false);
//
//        recyclerView = view.findViewById(R.id.recyclerviewforworkoutfragment);
//
//        Bundle bundle = getArguments();
//        String json = bundle.getString("profile", "");
//        Type Type = new TypeToken<ProfileInfo>() {
//        }.getType();
//        Gson gson = new Gson();
//        ProfileInfo profileInfo = gson.fromJson(json, Type);
//
//        DatabaseReference myRef = database.getReference("user_information").child(profileInfo.getId());
//
//        myRef.child("workouts").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                workouts.clear();
//                for (DataSnapshot workoutSnapshot : dataSnapshot.getChildren()) {
//                    Workout workout = workoutSnapshot.getValue(Workout.class);
//                    if (workout != null) {
//                        workouts.add(workout);
//                    }
//                }
//                setuprecyclerview(workouts,json);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.w("Failed to read value.", databaseError.toException());
//            }
//        });
//       return view;
//    }
//
//
//    private void setuprecyclerview(ArrayList<Workout> workouts,String json) {
//        RV_workout_adapter_for_user_expand adapter = new RV_workout_adapter_for_user_expand(getContext(), workouts,json);
//        recyclerView.setAdapter(adapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//    }
//
//
//    public static int getCurrentDayInNumber() {
//        Calendar calendar = Calendar.getInstance();
//        return calendar.get(Calendar.DAY_OF_WEEK);
//    }
//
//}
//
