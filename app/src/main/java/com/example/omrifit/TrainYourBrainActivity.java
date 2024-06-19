package com.example.omrifit;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Activity for training your brain with interesting facts and muscle information.
 */
public class TrainYourBrainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private ImageView imageView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_your_brain);

        initializeUI();
        setupDrawer();
        fetchFunFact();
    }

    /**
     * Initializes UI components.
     */
    private void initializeUI() {
        imageView = findViewById(R.id.imageView5);
        textView = findViewById(R.id.txt_muscle_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * Sets up the navigation drawer.
     */
    private void setupDrawer() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            handleNavigationItemSelected(item.getTitle().toString());
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    /**
     * Handles navigation item selection.
     *
     * @param itemTitle The title of the selected item.
     */
    private void handleNavigationItemSelected(String itemTitle) {
        switch (itemTitle) {
            case "Deltoid":
                textView.setText(R.string.deltoid_info);
                break;
            case "Biceps":
                textView.setText(R.string.biceps_info);
                break;
            case "Triceps":
                textView.setText(R.string.triceps_info);
                break;
            case "Pectoralis Major":
                textView.setText(R.string.pectoralis_info);
                break;
            case "Latissimus Dorsi":
                textView.setText(R.string.latissimus_info);
                break;
            case "Rectus Abdominis":
                textView.setText(R.string.rectus_abdominis_info);
                break;
            case "Obliques":
                textView.setText(R.string.obliques_info);
                break;
            case "Quadriceps":
                textView.setText(R.string.quadriceps_info);
                break;
            case "Hamstrings":
                textView.setText(R.string.hamstrings_info);
                break;
            case "Gastrocnemius":
                textView.setText(R.string.gastrocnemius_info);
                break;
            case "Soleus":
                textView.setText(R.string.biceps_info);
                break;
            case "Gluteus Maximus":
                textView.setText(R.string.biceps_info);
                break;
            case "Gluteus Medius":
                textView.setText(R.string.biceps_info);
                break;
            case "Trapezius":
                textView.setText(R.string.biceps_info);
                break;
            case "Rhomboids":
                textView.setText(R.string.biceps_info);
                break;
            default:
                textView.setText(R.string.app_name);
                break;
        }
        //todo change info
        addWikipediaLink();
    }

    /**
     * Adds a clickable Wikipedia link to the TextView.
     */
    private void addWikipediaLink() {
        SpannableString spannableString = new SpannableString(" see more at Wikipedia");
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 1, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), 1, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://en.wikipedia.org/wiki/Main_Page"));
                startActivity(browserIntent);
            }
        };
        spannableString.setSpan(clickableSpan, 1, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(textView.getText());
        builder.append(spannableString);
        textView.setText(builder);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Fetches a fun fact and related image from the API.
     */
    private void fetchFunFact() {
        ChatViewModel viewModel = new ChatViewModel();
        viewModel.getResponse("Can you give me an interesting fact about nutrition, a healthy lifestyle, gym, or sport?", new ChatResponseCallback() {
            @Override
            public void onSuccess(Chat chat) {
                textView.setText("Hi champ, here is a fun fact: \n" + chat.getPrompt());
                fetchImageForFact(chat.getPrompt());
            }

            @Override
            public void onError(String error) {
                // Handle error
            }
        });
    }

    /**
     * Fetches an image related to the given fact from the Pexels API.
     *
     * @param fact The fact text to search an image for.
     */
    private void fetchImageForFact(String fact) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.pexels.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PexelsService service = retrofit.create(PexelsService.class);
        service.searchPhotos(fact).enqueue(new retrofit2.Callback<PexelsResponse>() {
            @Override
            public void onResponse(retrofit2.Call<PexelsResponse> call, retrofit2.Response<PexelsResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().photos.isEmpty()) {
                    String imageUrl = response.body().photos.get(0).src.original;
                    runOnUiThread(() -> Glide.with(TrainYourBrainActivity.this).load(imageUrl).into(imageView));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<PexelsResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
