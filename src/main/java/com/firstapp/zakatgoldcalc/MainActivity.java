package com.firstapp.zakatgoldcalc;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // ADD THIS LINE - Make it public so AboutActivity can see it
    public static final String GITHUB_URL = "https://github.com/nurwafiqahfauzi/zakatCalc";

    private static final String TAG = "MainActivity";
    // TODO: Replace with your real GitHub URL (used by the Share action & About page)

    // Keys for state restore
    private static final String KEY_TOTAL_VALUE = "key_total_value";
    private static final String KEY_ZAKAT_PAYABLE = "key_zakat_payable";
    private static final String KEY_TOTAL_ZAKAT = "key_total_zakat";
    private static final String KEY_MINUS_URUF = "key_minus_uruf";

    private EditText etWeight, etGoldValue;
    private Spinner spinnerType;
    private TextView tvTotalValue, tvZakatPayable, tvTotalZakat, tvWeightMinusUruf;

    private NumberFormat currencyMYR;
    private final Locale localeMY = new Locale("ms", "MY");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Init views ---
        etWeight = findViewById(R.id.etWeight);
        etGoldValue = findViewById(R.id.etGoldValue);
        spinnerType = findViewById(R.id.spinnerType);
        Button btnCalculate = findViewById(R.id.btnCalculate);
        tvTotalValue = findViewById(R.id.tvTotalValue);
        tvZakatPayable = findViewById(R.id.tvZakatPayable);
        tvTotalZakat = findViewById(R.id.tvTotalZakat);
        tvWeightMinusUruf = findViewById(R.id.tvWeightMinusUruf); // helper output displayed to users

        // --- Currency formatter for MYR ---
        currencyMYR = NumberFormat.getCurrencyInstance(localeMY);
        try {
            currencyMYR.setCurrency(Currency.getInstance("MYR"));
        } catch (Exception e) {
            Log.w(TAG, "Currency MYR not set, fallback to default", e);
        }

        // --- Spinner (Keep/Wear) ---
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Keep", "Wear"}
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // --- Calculate button ---
        btnCalculate.setOnClickListener(v -> calculateAndDisplay());

        // Let "Done" on keyboard trigger calculation for convenience
        etGoldValue.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                calculateAndDisplay();
                return true;
            }
            return false;
        });

        // Restore results after rotation, if any
        if (savedInstanceState != null) {
            tvTotalValue.setText(savedInstanceState.getString(KEY_TOTAL_VALUE, ""));
            tvZakatPayable.setText(savedInstanceState.getString(KEY_ZAKAT_PAYABLE, ""));
            tvTotalZakat.setText(savedInstanceState.getString(KEY_TOTAL_ZAKAT, ""));
            tvWeightMinusUruf.setText(savedInstanceState.getString(KEY_MINUS_URUF, ""));
        }
    }

    private void calculateAndDisplay() {
        clearErrors();

        String weightStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";
        String goldValueStr = etGoldValue.getText() != null ? etGoldValue.getText().toString().trim() : "";
        String type = spinnerType.getSelectedItem() != null ? spinnerType.getSelectedItem().toString() : "Keep";

        // --- Validation ---
        if (TextUtils.isEmpty(weightStr)) {
            etWeight.setError("Please enter gold weight (gram)");
            etWeight.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(goldValueStr)) {
            etGoldValue.setError("Please enter current gold value (per gram)");
            etGoldValue.requestFocus();
            return;
        }

        double weight, goldValue;
        try {
            weight = Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            etWeight.setError("Invalid number");
            etWeight.requestFocus();
            Log.e(TAG, "Invalid weight input", e);
            return;
        }
        try {
            goldValue = Double.parseDouble(goldValueStr);
        } catch (NumberFormatException e) {
            etGoldValue.setError("Invalid number");
            etGoldValue.requestFocus();
            Log.e(TAG, "Invalid gold value input", e);
            return;
        }

        if (weight <= 0) {
            etWeight.setError("Weight must be greater than 0");
            etWeight.requestFocus();
            return;
        }
        if (goldValue <= 0) {
            etGoldValue.setError("Gold value must be greater than 0");
            etGoldValue.requestFocus();
            return;
        }

        try {
            // --- Calculation rules ---
            double uruf = type.equalsIgnoreCase("keep") ? 85.0 : 200.0;
            double totalGoldValue = weight * goldValue;

            double weightMinusUruf = weight - uruf;
            if (weightMinusUruf < 0) weightMinusUruf = 0;

            double zakatPayableValue = weightMinusUruf * goldValue;
            double totalZakat = zakatPayableValue * 0.025; // 2.5%

            // --- Display nicely formatted results ---
            tvWeightMinusUruf.setText(String.format(localeMY, "Berat melebihi uruf: %.0f g", weightMinusUruf));
            tvTotalValue.setText("Jumlah nilai emas: " + currencyMYR.format(totalGoldValue));
            tvZakatPayable.setText("Nilai kena zakat: " + currencyMYR.format(zakatPayableValue));
            tvTotalZakat.setText("Jumlah zakat (2.5%): " + currencyMYR.format(totalZakat));

            Log.d(TAG, "Calculated => type=" + type +
                    ", uruf=" + uruf +
                    ", totalGoldValue=" + totalGoldValue +
                    ", zakatPayableValue=" + zakatPayableValue +
                    ", totalZakat=" + totalZakat);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in calculation", e);
            Toast.makeText(this, "Something went wrong. Please check your inputs.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearErrors() {
        etWeight.setError(null);
        etGoldValue.setError(null);
    }

    // --- Action Bar: Share + About ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu); // requires res/menu/menu_main.xml
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            // This part is for sharing, you can add its logic later
            shareAppUrl();
            return true;

        } else if (id == R.id.action_about) {
            // Create an Intent to open AboutActivity
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareAppUrl() {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        String shareText = "Check out my Zakat Calculator app: " + GITHUB_URL;
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        try {
            startActivity(Intent.createChooser(sendIntent, "Share via"));
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "No app found to share", e);
            Toast.makeText(this, "No app available to share the link.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAboutPage() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    // --- Save results so they persist on rotation ---
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_TOTAL_VALUE, safeText(tvTotalValue));
        outState.putString(KEY_ZAKAT_PAYABLE, safeText(tvZakatPayable));
        outState.putString(KEY_TOTAL_ZAKAT, safeText(tvTotalZakat));
        outState.putString(KEY_MINUS_URUF, safeText(tvWeightMinusUruf));
    }

    private String safeText(TextView tv) {
        return tv != null && tv.getText() != null ? tv.getText().toString() : "";
    }
}
