package com.example.smartpantry;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

/** Settings screen: toggle for expiring-soon highlighting, saved in SharedPreferences. */
public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS = "smartpantry_prefs";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        NavHelper.applyInsets(findViewById(R.id.main));

        final SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        SwitchCompat toggle = findViewById(R.id.switch_expiry);
        toggle.setChecked(prefs.getBoolean(KEY_EXPIRY_ALERTS, true));
        toggle.setOnCheckedChangeListener((button, isChecked) ->
                prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, isChecked).apply());

        DatabaseHelper db = new DatabaseHelper(this);
        TextView stats = findViewById(R.id.text_stats);
        stats.setText("Ingredients in pantry: " + db.getAllPantryItems().size()
                + "\nRecipes in database: " + db.getAllRecipes().size());

        NavHelper.setup(this, R.id.nav_settings);
    }
}
