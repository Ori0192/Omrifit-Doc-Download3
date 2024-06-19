//package com.example.omrifit.registration_and_login;
//
//import android.content.Intent;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.widget.Button;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.cardview.widget.CardView;
//
//import com.example.omrifit.R;
//import com.example.omrifit.settings.CreateProfileActivity;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//public class PackagesActivity extends AppCompatActivity {
//    CardView cv_basic,cv_g_bros,cv_g_rats;
//    Button btn_continue;
//    String packageName="basic";
//    FirebaseAuth mAuth = FirebaseAuth.getInstance();
//    FirebaseUser user = mAuth.getCurrentUser();
//
//    FirebaseDatabase database = FirebaseDatabase.getInstance();
//    DatabaseReference myRef = database.getReference("user_information").child(user.getUid());
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.subscriptions);
//
//        cv_basic=findViewById(R.id.cv_free);
//        cv_g_bros=findViewById(R.id.cv_10dollars);
//        cv_g_rats=findViewById(R.id.cv_20dollars);
//        btn_continue=findViewById(R.id.btn_continue_packages);
//
//        cv_basic.setOnClickListener(v -> {
//            cv_basic.setBackgroundColor(Color.parseColor("#32143A"));
//            cv_g_bros.setBackgroundColor(Color.WHITE);
//            cv_g_rats.setBackgroundColor(Color.WHITE);
//            packageName="basic";
//        });
//        cv_g_bros.setOnClickListener(v -> {
//            cv_g_bros.setBackgroundColor(Color.parseColor("#32143A"));
//            cv_basic.setBackgroundColor(Color.WHITE);
//            cv_g_rats.setBackgroundColor(Color.WHITE);
//            packageName="g_bros";
//        });
//        cv_g_rats.setOnClickListener(v -> {
//            cv_g_rats.setBackgroundColor(Color.parseColor("#32143A"));
//            cv_basic.setBackgroundColor(Color.WHITE);
//            cv_g_bros.setBackgroundColor(Color.WHITE);
//            packageName="g_rats";
//        });
//        btn_continue.setOnClickListener(v -> {
//            myRef.child("package").setValue(packageName);
//            Intent intent=new Intent(PackagesActivity.this, CreateProfileActivity.class);
//            startActivity(intent);
//        });
//
//    }
//
//
//    }
