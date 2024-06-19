//package com.example.omrifit.manager;
//
//import android.os.Bundle;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentManager;
//import androidx.fragment.app.FragmentTransaction;
//
//import com.example.omrifit.R;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//
//public class ManagerLayout extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_manager_layout);
//
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnavigationforeditors);
//        bottomNavigationView.setBackground(null);
//
//        replacefragemant(new FragmentManageTasks());
//        bottomNavigationView.setOnItemSelectedListener(item -> {
//            switch (item.getTitle().toString()) {
//                case "Tasks":
//                    replacefragemant(new FragmentManageTasks());
//                    break;
//                case "Messages":
//                    replacefragemant(new FragmentManageMessages());
//                    break;
//                default:
//                    break;
//            }
//            return true;
//        });
//    }
//    private void  replacefragemant(Fragment fragment)
//    {
//        FragmentManager fragmentManager=this.getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.frame_layoutforeditors,fragment);
//        fragmentTransaction.commit();
//    }
//
//
//}
//
//