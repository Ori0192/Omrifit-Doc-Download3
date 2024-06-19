package com.example.omrifit.friends_profile;

import static com.example.omrifit.fragments.HomeFragment.displayExperience;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.omrifit.R;
import com.example.omrifit.classes.ProfileInfo;
import com.example.omrifit.classes.Utility;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Activity to display the expanded view of a user's profile.
 */
public class UserExpand extends AppCompatActivity {
    private CircleImageView imgUserItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_expand);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnavigationforuser);
        bottomNavigationView.setBackground(null);

        setupUI();

        replaceFragment(new UserExpandChatFragment());
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getTitle().toString()) {
                case "Chat":
                    replaceFragment(new UserExpandChatFragment());
                    break;
                case "Gallery":
                    replaceFragment(new UserExpandGallery());
                    break;
                default:
                    break;
            }
            return true;
        });
    }

    /**
     * Set up the UI elements and initialize the user profile data.
     */
    private void setupUI() {
        imgUserItem = findViewById(R.id.img_user_epand);
        TextView textView = findViewById(R.id.textView22);

        String json = getIntent().getStringExtra("profile");
        Type type = new TypeToken<ProfileInfo>() {}.getType();
        Gson gson = new Gson();
        ProfileInfo profileInfo = gson.fromJson(json, type);

        TextView txtName = findViewById(R.id.txt_name_user_item);
        txtName.setText(profileInfo.getName());

        Utility.setImageUserPhoto(profileInfo.getId(), imgUserItem);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("user_information").child(profileInfo.getId());
        displayExperience(myRef, textView, false);
    }

    /**
     * Replace the current fragment with the specified fragment.
     * @param fragment The fragment to display.
     */
    private void replaceFragment(Fragment fragment) {
        String json = getIntent().getStringExtra("profile");

        Bundle bundle = new Bundle();
        bundle.putString("profile", json); // Pass the profile data to the fragment
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}
