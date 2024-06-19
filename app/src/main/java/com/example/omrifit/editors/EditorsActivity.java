package com.example.omrifit.editors;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.omrifit.ExerciseEditorFragment;

import com.example.omrifit.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * EditorsActivity handles the navigation between different editor fragments within the app.
 */
public class EditorsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_editors);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnavigationforeditors);
        bottomNavigationView.setBackground(null);

        // Load the default fragment
        replaceFragment(new ExerciseEditorFragment());

        // Set up navigation item selection listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getTitle().toString()) {
                case "exercises":
                    replaceFragment(new ExerciseEditorFragment());
                    break;
                case "products":
                    replaceFragment(new ProductsEditorFragment());
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
     * @param fragment The fragment to display.
     */
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layoutforeditors, fragment);
        fragmentTransaction.commit();
    }
}
