package com.example.omrifit.registration_and_login;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.omrifit.NetworkConnection;
import com.example.omrifit.R;
import com.example.omrifit.fragments.HomePageActivity;
import com.example.omrifit.settings.CreateProfileActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference myRef;
    private View bmView;  // Member variable to hold the view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("user_information");

        bmView = findViewById(R.id.bm);  // Find the view by ID
        setupGoogleSignIn();
        setupNetworkConnection();
        setupButtons();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkCurrentUser();
    }

    /**
     * Checks if there is a current authenticated user. If a user is authenticated,
     * checks if the user has enabled biometric authentication.
     */
    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (!currentUser.isEmailVerified()) {
                currentUser.delete();
            }
            // Show biometric view when user is verified but further checks needed
            bmView.setVisibility(View.VISIBLE);
            myRef.child(currentUser.getUid()).child("isThereScreenLock")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean isThereScreenLock = Boolean.TRUE.equals(snapshot.getValue(boolean.class));
                            if (isThereScreenLock) {
                                authenticateBiometric();
                            } else {
                                launchHomeActivity();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        } else {
            // Hide biometric view if no user or user not verified
            bmView.setVisibility(View.GONE);
        }
    }

    /**
     * Sets up Google Sign-In options and initializes the GoogleSignInClient.
     */
    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))  // Updated to use resource string
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(v -> signIn());
    }

    /**
     * Sets up network connection observer and handles connectivity changes.
     */
    private void setupNetworkConnection() {
        View layoutInflater = findViewById(R.id.networkError);
        NetworkConnection networkConnection = new NetworkConnection(getApplicationContext());

        networkConnection.observe(this, isConnected -> {
            if (isConnected) {
                Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
                ImageView imageView = layoutInflater.findViewById(R.id.img_gif);
                Glide.with(this).asGif().load(R.drawable.workoutloading).into(imageView);
                layoutInflater.setVisibility(View.GONE);
            } else {
                Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show();
                layoutInflater.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Sets up the sign-up and login buttons and their click listeners.
     */
    private void setupButtons() {
        Button btn_signup = findViewById(R.id.btn_signup);
        Button btn_login = findViewById(R.id.btn_login);
        btn_signup.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SignupActivity.class)));
        btn_login.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));
    }

    /**
     * Initiates biometric authentication.
     */
    private void authenticateBiometric() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                launchHomeActivity();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for Omrifit")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();
        biometricPrompt.authenticate(promptInfo);
    }

    /**
     * Launches the HomePageActivity.
     */
    private void launchHomeActivity() {
        Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Starts the Google Sign-In process.
     */
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data));
        }
    }

    /**
     * Handles the result of the Google Sign-In intent.
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.w("MainActivity", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Failed to sign in: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Authenticates the user with Firebase using the Google ID token.
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("MainActivity", "signInWithCredential:success");
                        launchHomeActivity();
                    } else {
                        Log.w("MainActivity", "signInWithCredential:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
