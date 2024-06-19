package com.example.omrifit.settings;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.omrifit.R;
import com.example.omrifit.classes.ProfileInfo;
import com.example.omrifit.fragments.HomePageActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * CreateProfileActivity handles the creation and updating of the user's profile information.
 */
public class CreateProfileActivity extends AppCompatActivity {

    // UI components
    Spinner spinnerAge, spinnerHeight, spinnerWeight;
    TextView btn_male, btn_female;
    Button btn_next;
    CircleImageView id_profile_image;
    TextInputEditText edt_name;

    // Firebase components
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("user_information").child(user.getUid());

    // Constants and member variables
    private static final int REQUEST_IMAGE_CAPTURE = 4;
    private static final int REQUEST_GALLERY_PHOTO = 5;
    private static final int SELECT_IMAGE_REQUEST_CODE = 1;
    private static final int PERMISSIONS_REQUEST = 100;
    Uri selectedImageUri = Uri.parse("https://ih1.redbubble.net/image.1046392292.3346/st,small,507x507-pad,600x600,f8f8f8.jpg");
    boolean first_entrance;
    String name, gender = "Male";
    int weight, height, age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fff);

        // Initialize UI components
        edt_name = findViewById(R.id.edt_name);
        spinnerAge = findViewById(R.id.spn_Type);
        spinnerHeight = findViewById(R.id.spn_height);
        spinnerWeight = findViewById(R.id.spn_weight);
        btn_male = findViewById(R.id.btn_male);
        btn_female = findViewById(R.id.btn_female);
        btn_next = findViewById(R.id.btn_next);
        id_profile_image = findViewById(R.id.img_user_epand);

        // Check if it's the user's first entrance
        first_entrance = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getBoolean("first_entrance", false);
        loadCurrentUserDetails();

        // Set up spinners with data
        setupSpinners();

        // Load profile image from Firebase Storage
        loadProfileImage();

        // Set up gender selection buttons
        setupGenderButtons();

        // Set up the next button click listener
        btn_next.setOnClickListener(v -> saveProfileInfo());

        // Set up profile image click listener to select or capture image
        id_profile_image.setOnClickListener(v -> requestPermissions());
    }

    /**
     * Loads the current user's details from the database and updates the UI.
     */
    private void loadCurrentUserDetails() {
        myRef.child("user_profile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Load and set existing data
                    ProfileInfo profileInfo = snapshot.getValue(ProfileInfo.class);
                    if (profileInfo != null) {
                        edt_name.setText(profileInfo.getName());
                        spinnerAge.setSelection(profileInfo.getAge() - 14);
                        spinnerHeight.setSelection(profileInfo.getHeight() - 140);
                        spinnerWeight.setSelection(profileInfo.getWeight() - 40);
                        // Set gender button states based on loaded data
                        if (profileInfo.getGender().equals("Male")) {
                            gender = btn_male.getText().toString();
                            btn_male.setBackgroundResource(R.color.blue);
                            btn_female.setBackgroundResource(R.color.colorPrimaryDark);
                        } else {
                            gender = btn_female.getText().toString();
                            btn_female.setBackgroundResource(R.color.blue);
                            btn_male.setBackgroundResource(R.color.colorPrimaryDark);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle potential errors
            }
        });
    }

    /**
     * Requests necessary permissions for accessing the camera and storage.
     */
    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST);
        } else {
            setupAddImageButton();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, proceed with accessing camera and storage
                setupAddImageButton();
            } else {
                Toast.makeText(this, "Permissions are required to use the camera and storage.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Uploads the selected image to Firebase Storage.
     */
    public void uploadImage(Uri uri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(user.getUid() + "_img_profile");

        imageRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image successfully uploaded
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                });
    }

    /**
     * Sets up the spinners with age, height, and weight data.
     */
    private void setupSpinners() {
        ArrayAdapter<String> adapterAge = new ArrayAdapter<>(this, R.layout.spinner_item, getAge());
        adapterAge.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinnerAge.setAdapter(adapterAge);

        ArrayAdapter<String> adapterHeight = new ArrayAdapter<>(this, R.layout.spinner_item, getHeight());
        adapterHeight.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinnerHeight.setAdapter(adapterHeight);

        ArrayAdapter<String> adapterWeight = new ArrayAdapter<>(this, R.layout.spinner_item, getWeight());
        adapterWeight.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinnerWeight.setAdapter(adapterWeight);
    }

    /**
     * Loads the profile image from Firebase Storage and displays it in the ImageView.
     */
    private void loadProfileImage() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(user.getUid() + "_img_profile");

        imageRef.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).into(id_profile_image));
    }

    /**
     * Sets up click listeners for the gender selection buttons.
     */
    private void setupGenderButtons() {
        btn_female.setOnClickListener(v -> {
            btn_female.setBackgroundResource(R.color.blue);
            btn_male.setBackgroundResource(R.color.colorPrimaryDark);
            gender = btn_female.getText().toString();
        });

        btn_male.setOnClickListener(v -> {
            btn_female.setBackgroundResource(R.color.colorPrimaryDark);
            btn_male.setBackgroundResource(R.color.blue);
            gender = btn_male.getText().toString();
        });
    }

    /**
     * Saves the profile information entered by the user and uploads the profile image.
     */
    private void saveProfileInfo() {
        name = edt_name.getText().toString();
        if (!name.isEmpty()) {
            weight = spinnerWeight.getSelectedItemPosition() + 40;
            age = spinnerAge.getSelectedItemPosition() + 14;
            height = spinnerHeight.getSelectedItemPosition() + 140;

            uploadImage(selectedImageUri);

            ProfileInfo newProfile = new ProfileInfo(name, gender, weight, height, age, user.getUid());

            myRef.child("user_profile").setValue(newProfile);
            if (first_entrance) {
                myRef.child("begin_weight").setValue(weight);
                Intent intent = new Intent(CreateProfileActivity.this, GeneralSettingsActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(CreateProfileActivity.this, HomePageActivity.class);
                startActivity(intent);
            }
        } else {
            Toast.makeText(CreateProfileActivity.this, "Enter your name", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Sets up the dialog for adding an image by either capturing from the camera or selecting from the gallery.
     */
    private void setupAddImageButton() {
        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Take image from-");

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else if (which == 1) {
                openGallery();
            }
        });

        builder.show();
    }

    /**
     * Opens the camera for capturing an image.
     */
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    /**
     * Opens the gallery for selecting an image.
     */
    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            selectedImageUri = data.getData();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            selectedImageUri = saveBitmapToCache(imageBitmap, this);
            id_profile_image.setImageBitmap(imageBitmap);
        } else if (requestCode == REQUEST_GALLERY_PHOTO && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            selectedImageUri = saveBitmapToCache(imageBitmap, this);
            id_profile_image.setImageBitmap(imageBitmap);
        }
    }

    /**
     * Saves a bitmap to cache and returns its URI.
     *
     * @param bitmap  The bitmap to save.
     * @param context The context.
     * @return The URI of the saved bitmap.
     */
    public Uri saveBitmapToCache(Bitmap bitmap, Context context) {
        String fileName = "temp_image_" + System.currentTimeMillis() + ".jpeg";

        File directory = context.getExternalCacheDir();
        if (directory == null) {
            directory = context.getCacheDir();
        }

        File imageFile = new File(directory, fileName);

        try (OutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return Uri.fromFile(imageFile);
    }

    /**
     * Returns an ArrayList of age values from 14 to 98.
     *
     * @return An ArrayList of age values.
     */
    private ArrayList<String> getAge() {
        ArrayList<String> arrayList = new ArrayList<>();

        for (int i = 14; i < 99; i++) {
            arrayList.add(String.valueOf(i));
        }

        return arrayList;
    }

    /**
     * Returns an ArrayList of height values from 140 cm to 219 cm.
     *
     * @return An ArrayList of height values.
     */
    private ArrayList<String> getHeight() {
        ArrayList<String> arrayList = new ArrayList<>();

        for (int i = 140; i < 220; i++) {
            arrayList.add(i + " cm");
        }

        return arrayList;
    }

    /**
     * Returns an ArrayList of weight values from 40 kg to 299 kg.
     *
     * @return An ArrayList of weight values.
     */
    private ArrayList<String> getWeight() {
        ArrayList<String> arrayList = new ArrayList<>();

        for (int i = 40; i < 300; i++) {
            arrayList.add(i + " kg");
        }

        return arrayList;
    }
}
