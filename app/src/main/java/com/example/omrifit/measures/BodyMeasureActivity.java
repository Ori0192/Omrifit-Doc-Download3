package com.example.omrifit.measures;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.omrifit.NetworkConnection;

import com.example.omrifit.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Activity to manage body measurements such as weight and fat percentage.
 */
public class BodyMeasureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measures);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnavigationformeasures);
        bottomNavigationView.setBackground(null);

        NetworkConnection networkConnection = new NetworkConnection(getApplicationContext());
        View layoutInflater = findViewById(R.id.networkError);

        // Observe network connectivity changes
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

        // Initialize with the WeightMeasureFragment
        replaceFragment(new WeightMeasureFragment());

        // Set up bottom navigation item selection listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getTitle().toString()) {
                case "Weight":
                    replaceFragment(new WeightMeasureFragment());
                    break;
                case "Fat":
                    replaceFragment(new FatMeasureFragment());
                    break;
                default:
                    break;
            }
            return true;
        });
    }

    /**
     * Replaces the current fragment with the specified fragment.
     *
     * @param fragment The new fragment to display.
     */
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layoutformeasues, fragment);
        fragmentTransaction.commit();
    }
}
