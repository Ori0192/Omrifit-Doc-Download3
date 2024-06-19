package com.example.omrifit.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.omrifit.NetworkConnection;
import com.example.omrifit.R;
import com.example.omrifit.settings.GeneralSettingsActivity;
import com.example.omrifit.community.CommunityMainFragment;
import com.example.omrifit.registration_and_login.MainActivity;
import com.example.omrifit.settings.CreateProfileActivity;
import com.example.omrifit.sport.SportFragment;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * The HomePageActivity class represents the main activity of the app, handling the bottom navigation and fragment transactions.
 */
public class HomePageActivity extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("user_information").child(user.getUid());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Customize status bar for Lollipop and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));
        }

        // Set up network connection observer
        setupNetworkObserver();

        // Set up bottom navigation
        setupBottomNavigationView();

        // Handle navigation based on intent
        handleNavigationIntent();

        // Observe new messages
        observeNewMessages();
    }

    /**
     * Sets up the network observer to monitor connectivity changes.
     */
    private void setupNetworkObserver() {
        View layoutInflater = findViewById(R.id.networkError);
        NetworkConnection networkConnection = new NetworkConnection(getApplicationContext());

        networkConnection.observe(this, isConnected -> {
            if (isConnected) {
                Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
                layoutInflater.setVisibility(View.GONE);
            } else {
                Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show();
                ImageView imageView = layoutInflater.findViewById(R.id.img_gif);
                Glide.with(this).asGif().load(R.drawable.workoutloading).into(imageView);
                layoutInflater.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Sets up the bottom navigation view and its item selection listener.
     */
    private void setupBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnavigation);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            bottomNavigationView.setBackground(null);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getTitle().toString()) {
                case "home":
                    replaceFragment(new HomeFragment());
                    break;
                case "nutrition":
                    replaceFragment(new NutritionFragment());
                    break;
                case "workout":
                    replaceFragment(new SportFragment());
                    break;
                case "chats":
                    replaceFragment(new CommunityMainFragment());
                    break;
                case "Omri":
                    replaceFragment(new ChatFragment());
                    break;
                default:
                    break;
            }
            return true;
        });
    }

    /**
     * Handles the navigation based on the received intent.
     */
    private void handleNavigationIntent() {
        if (getIntent().getBooleanExtra("toOmriChat", false)) {
            replaceFragment(new ChatFragment());
        } else {
            replaceFragment(new HomeFragment());
        }
    }

    /**
     * Observes new messages and updates the bottom navigation badge accordingly.
     */
    private void observeNewMessages() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnavigation);
        myRef.child("Omri").child("newMessages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int newMessages = snapshot.getValue(Integer.class);
                    if (newMessages > 0) {
                        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.Omri);
                        badge.setNumber(newMessages); // Set the number of unread messages
                    } else {
                        bottomNavigationView.removeBadge(R.id.Omri);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (NetworkConnection.getInstance(this).hasActiveObservers()) {
            NetworkConnection.getInstance(this).removeObservers(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(HomePageActivity.this, GeneralSettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.profile) {
            Intent intent = new Intent(HomePageActivity.this, CreateProfileActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Logs out the user and navigates to the login screen.
     */
    private void logout() {
        View view = findViewById(R.id.snackbar_container);
        Snackbar snackbar = Snackbar.make(view, "Are you sure you want to logout?", Snackbar.LENGTH_LONG);

        snackbar.setAction("Yes", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(HomePageActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        snackbar.show();
    }

    /**
     * Replaces the current fragment with the specified fragment.
     *
     * @param fragment The fragment to display.
     */
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}
