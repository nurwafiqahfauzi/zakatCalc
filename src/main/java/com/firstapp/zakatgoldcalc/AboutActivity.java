package com.firstapp.zakatgoldcalc;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    private static final String TAG = "AboutActivity";

    // --- Replace with your real details ---
    private static final String DEV_NAME = "NUR WAFIQAH BINTI MOHD FAUZI";
    private static final String STUDENT_NO = "2024542807";
    private static final String PROGRAMME = "CDCS251";
    private static final String COPYRIGHT = "© 2025 MARA University of Technology / Nur Wafiqah Mohd Fauzi. All rights reserved.";
    // Re-use the URL from MainActivity to keep it consistent
    private static final String GITHUB_URL = "https://github.com/nurwafiqahfauzi/zakatCalc"; // The single source of truth

    private static final String WEBSITE_URL = GITHUB_URL; // Use the constant from this file
    private TextView tvDevName, tvStudentNo, tvProgramme, tvCopyright, tvWebsite;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("About");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvDevName = findViewById(R.id.tvDevName);
        tvStudentNo = findViewById(R.id.tvStudentNo);
        tvProgramme = findViewById(R.id.tvProgramme);
        tvWebsite = findViewById(R.id.tvWebsite);
        // Optional: you can include a copyright line
        // Ensure you have this TextView in your layout, or remove this line.
        // e.g., <TextView android:id="@+id/tvCopyright" .../>
        try {
            // If you added it in XML
            int id = getResources().getIdentifier("tvCopyright", "id", getPackageName());
            if (id != 0) {
                tvCopyright = findViewById(id);
            }
        } catch (Exception ignored) { }

        // Fill details
        tvDevName.setText("Developer: " + DEV_NAME);
        tvStudentNo.setText("Student No: " + STUDENT_NO);
        tvProgramme.setText("Programme: " + PROGRAMME);

        if (tvCopyright != null) {
            tvCopyright.setText(COPYRIGHT);
        }

        // Make a clickable URL text programmatically
        SpannableString link = new SpannableString(WEBSITE_URL);
        link.setSpan(new URLSpan(WEBSITE_URL), 0, link.length(), 0);
        tvWebsite.setText(link);
        tvWebsite.setMovementMethod(LinkMovementMethod.getInstance());

        // Optional: open URL on click (fallback if device can't autolink)
        tvWebsite.setOnClickListener(v -> {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL));
                startActivity(browserIntent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "No browser found", e);
                Toast.makeText(this, "No browser app found to open the link.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Action Bar: Share (same as main) ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu); // or reuse menu_main if you prefer
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_share) {
            shareAppUrl();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareAppUrl() {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        // Use the GITHUB_URL constant defined at the top of this file
        String shareText = "Check out this Zakat Calculator app: " + GITHUB_URL;
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        try {
            startActivity(Intent.createChooser(sendIntent, "Share via"));
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "No app found to share", e);
            Toast.makeText(this, "No app available to share the link.", Toast.LENGTH_SHORT).show();
        }
    }
}