package com.example.omrifit.registration_and_login;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.omrifit.R;
import com.example.omrifit.settings.CreateProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

/**
 * Activity for user signup.
 */
public class SignupActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword, edtSecondPassword;
    private Button btnSignup;
    private FirebaseAuth mAuth;
    private ProgressDialog verificationDialog;
    private Handler handler = new Handler();
    private ImageView imgOmri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initializeUI();

        setupPasswordFocusChangeListener();

        setupPolicyLinks();

        setupSignupButtonListener();
    }

    /**
     * Initializes the UI elements and Firebase authentication.
     */
    private void initializeUI() {
        mAuth = FirebaseAuth.getInstance();
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_pass);
        edtSecondPassword = findViewById(R.id.edt_pass_verification);
        imgOmri = findViewById(R.id.imgOmri);
        btnSignup = findViewById(R.id.btn_continue_signup);
    }

    /**
     * Sets up the listener for focus change on the password EditTexts.
     * Changes the ImageView drawable based on focus state.
     */
    private void setupPasswordFocusChangeListener() {
        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    imgOmri.setImageDrawable(getResources().getDrawable(R.drawable.omri5new));
                } else {
                    imgOmri.setImageDrawable(getResources().getDrawable(R.drawable.omri4new));
                }
            }
        };

        edtPassword.setOnFocusChangeListener(onFocusChangeListener);
        edtSecondPassword.setOnFocusChangeListener(onFocusChangeListener);
    }

    /**
     * Sets up the clickable policy links in the signup text.
     */
    private void setupPolicyLinks() {
        CardView cardViewPolicy = findViewById(R.id.cvpolicy);
        TextView txtPolicyCard = findViewById(R.id.textViewpolicycard);
        Button btnAcceptPolicy = findViewById(R.id.buttonacceptpolicy);

        btnAcceptPolicy.setOnClickListener(v -> cardViewPolicy.setVisibility(View.GONE));

        SpannableString ss = new SpannableString("By signing up, you agree to the Terms of Service and Privacy Policy");
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                cardViewPolicy.setVisibility(View.VISIBLE);
                txtPolicyCard.setText(R.string.termsofservice);
                Toast.makeText(SignupActivity.this, "TERMS CHOSEN", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
        ClickableSpan clickableSpan2 = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                cardViewPolicy.setVisibility(View.VISIBLE);
                txtPolicyCard.setText(R.string.policy);
                Toast.makeText(SignupActivity.this, "PRIVACY CHOSEN", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };

        ss.setSpan(clickableSpan1, 31, 48, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(clickableSpan2, 52, 67, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView textViewPolicy = findViewById(R.id.txt_policy);
        textViewPolicy.setText(ss);
        textViewPolicy.setMovementMethod(LinkMovementMethod.getInstance());
        textViewPolicy.setHighlightColor(Color.TRANSPARENT);
    }

    /**
     * Sets up the listener for the signup button.
     * Validates the input and performs signup if valid.
     */
    private void setupSignupButtonListener() {
        btnSignup.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirmPassword = edtSecondPassword.getText().toString().trim();

            if (validateInput(email, password, confirmPassword)) {
                performSignup(email, password);
            }
        });
    }

    /**
     * Validates the user input for email and password.
     *
     * @param email           the email entered by the user
     * @param password        the password entered by the user
     * @param confirmPassword the confirmation password entered by the user
     * @return true if input is valid, false otherwise
     */
    private boolean validateInput(String email, String password, String confirmPassword) {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isEmailValid(email)) {
            edtEmail.setError("Invalid email format");
            return false;
        }

        if (assessPasswordStrength(password) < 3) {
            edtPassword.setError("Password is too weak");
            return false;
        }

        return true;
    }

    /**
     * Checks if the email format is valid.
     *
     * @param email the email to check
     * @return true if email format is valid, false otherwise
     */
    private boolean isEmailValid(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    /**
     * Assesses the strength of the password.
     *
     * @param password the password to assess
     * @return an integer representing the strength of the password
     */
    private int assessPasswordStrength(String password) {
        int strengthPoints = 0;

        if (password.length() >= 8) strengthPoints++;
        if (password.matches(".*[a-z].*")) strengthPoints++;
        if (password.matches(".*[A-Z].*")) strengthPoints++;
        if (password.matches(".*[0-9].*")) strengthPoints++;
        if (password.matches(".*[^a-zA-Z0-9].*")) strengthPoints++;

        return strengthPoints;
    }

    /**
     * Performs the signup process with Firebase authentication.
     *
     * @param email    the email entered by the user
     * @param password the password entered by the user
     */
    private void performSignup(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        sendVerificationEmail();
                    } else {
                        Toast.makeText(SignupActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Sends a verification email to the user.
     */
    private void sendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            showVerificationDialog();
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Verification email sent.", Toast.LENGTH_SHORT).show();
                            checkEmailVerification(user);
                        } else {
                            Toast.makeText(SignupActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                            if (verificationDialog.isShowing()) {
                                verificationDialog.dismiss();
                            }
                        }
                    });
        }
    }

    /**
     * Displays a dialog while waiting for email verification.
     */
    private void showVerificationDialog() {
        verificationDialog = new ProgressDialog(this);
        verificationDialog.setTitle("Email Verification");
        verificationDialog.setMessage("Waiting for email verification...");
        verificationDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Resend", (dialog, which) -> sendVerificationEmail());
        verificationDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            verificationDialog.dismiss();
            Objects.requireNonNull(mAuth.getCurrentUser()).delete();
        });
        verificationDialog.setCancelable(false);
        verificationDialog.show();
    }

    /**
     * Checks the email verification status periodically.
     *
     * @param user the FirebaseUser whose email verification status is to be checked
     */
    private void checkEmailVerification(FirebaseUser user) {
        Runnable emailVerificationCheckRunnable = new Runnable() {
            @Override
            public void run() {
                user.reload().addOnSuccessListener(aVoid -> {
                    if (user.isEmailVerified()) {
                        handler.removeCallbacks(this);
                        if (verificationDialog.isShowing()) {
                            verificationDialog.dismiss();
                        }
                        proceedToNextScreen();
                    } else {
                        handler.postDelayed(this, 2000);
                    }
                });
            }
        };
        handler.post(emailVerificationCheckRunnable);
    }

    /**
     * Proceeds to the next screen after successful email verification.
     */
    private void proceedToNextScreen() {
        Intent intent = new Intent(SignupActivity.this, CreateProfileActivity.class);
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("first_entrance", true).apply();
        startActivity(intent);
        finish();
    }
}
