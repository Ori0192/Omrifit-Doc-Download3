package com.example.omrifit.registration_and_login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.omrifit.R;
import com.example.omrifit.settings.CreateProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity for user login.
 */
public class LoginActivity extends AppCompatActivity {
    private EditText edtMail, edtPass;
    private Button btnContinue;
    private TextView txtForgotPassword;
    private ImageView imgOmri;
    private FirebaseAuth mAuth;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeUI();

        setupPasswordFocusChangeListener();

        setupForgotPasswordListener();

        setupLoginButtonListener();
    }

    /**
     * Initializes the UI elements and Firebase authentication.
     */
    private void initializeUI() {
        intent = new Intent(LoginActivity.this, MainActivity.class);

        txtForgotPassword = findViewById(R.id.txt_forgot_password);
        edtMail = findViewById(R.id.edt_mail_log);
        edtPass = findViewById(R.id.edt_pass_log);
        imgOmri = findViewById(R.id.imgOmri);
        btnContinue = findViewById(R.id.btn_continue_login);

        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Sets up the listener for focus change on the password EditText.
     * Changes the ImageView drawable based on focus state.
     */
    private void setupPasswordFocusChangeListener() {
        edtPass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    imgOmri.setImageDrawable(getResources().getDrawable(R.drawable.omri5new));
                } else {
                    imgOmri.setImageDrawable(getResources().getDrawable(R.drawable.omri4new));
                }
            }
        });
    }

    /**
     * Sets up the listener for the forgot password TextView.
     * Sends a password reset email if the email field is not empty.
     */
    private void setupForgotPasswordListener() {
        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtMail.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "We sent you instructions for password updating", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Error occurred while sending the message", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    /**
     * Sets up the listener for the continue button.
     * Authenticates the user with email and password.
     */
    private void setupLoginButtonListener() {
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });
    }

    /**
     * Logs in the user with email and password.
     * Navigates to the appropriate activity based on login success and first entrance.
     */
    private void loginUser() {
        String inputMail = edtMail.getText().toString();
        String inputPass = edtPass.getText().toString();

        if (inputMail.isEmpty() || inputPass.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Some fields are missing,please fill them up.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(inputMail, inputPass)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            handleSuccessfulLogin();
                        } else {
                            Toast.makeText(LoginActivity.this, "Connection lost please check your email and password.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Handles actions after successful login.
     * Navigates to CreateProfileActivity or MainActivity based on first entrance.
     */
    private void handleSuccessfulLogin() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (getSharedPreferences("MyPrefs", MODE_PRIVATE).getBoolean("first_entrance", false)) {
            startActivity(new Intent(LoginActivity.this, CreateProfileActivity.class));
        } else {
            startActivity(intent);
        }
        finish();
    }
}
