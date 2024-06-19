package com.example.omrifit.community;

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

public class CommunityMainFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chats_fragments, container, false);
        setHasOptionsMenu(true);

        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomnavigationforchats);
        bottomNavigationView.setBackground(null);

        replacefragemant(new GroupFragment());
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getTitle().toString()) {
                case "comunities":
                    replacefragemant(new GroupFragment());
                    break;
                case "friends":
                    replacefragemant(new FriendsFragment());
                    break;
                case "requests":
                    replacefragemant(new RequestsFragment());
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
    private void  replacefragemant(Fragment fragment)
    {
        FragmentManager fragmentManager=requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layoutforchats,fragment);
        fragmentTransaction.commit();
    }
}

