package com.example.omrifit.sport;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.example.omrifit.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * SportFragment is responsible for managing and displaying the sport-related fragments within the app.
 * It includes a BottomNavigationView to switch between different sport-related fragments.
 */
public class SportFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sport_fragment, container, false);
        setHasOptionsMenu(true);

        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomnavigationforsport);
        bottomNavigationView.setBackground(null);

        // Set the initial fragment to WorkoutFragment
        replaceFragment(new WorkoutFragment());

        // Handle navigation item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getTitle().toString()) {
                case "workout":
                    replaceFragment(new WorkoutFragment());
                    break;
                case "workout dates":
                    replaceFragment(new WorkoutSettingsFragment());
                    break;
                default:
                    break;
            }
            return true;
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate your menu resource or modify the menu
        inflater.inflate(R.menu.main_menu, menu);
    }

    /**
     * Replaces the current fragment with the specified fragment.
     *
     * @param fragment The fragment to display.
     */
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layoutforsport, fragment);
        fragmentTransaction.commit();
    }
}
