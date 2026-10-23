package com.example.smartpantry;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Shared helpers: wires up the bottom navigation bar and keeps content clear of system bars. */
public class NavHelper {

    /** Connects the bottom navigation bar; each tab starts the matching Activity using an Intent. */
    public static void setup(final Activity activity, final int selectedId) {
        BottomNavigationView nav = activity.findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(selectedId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId) {
                return true;
            }
            Class<?> target;
            if (id == R.id.nav_pantry) {
                target = MainActivity.class;
            } else if (id == R.id.nav_recipes) {
                target = SuggestedRecipesActivity.class;
            } else {
                target = SettingsActivity.class;
            }
            Intent intent = new Intent(activity, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            activity.startActivity(intent);
            return true;
        });
    }

    /** Adds padding so the layout is not hidden behind the status bar or navigation bar. */
    public static void applyInsets(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }
}
