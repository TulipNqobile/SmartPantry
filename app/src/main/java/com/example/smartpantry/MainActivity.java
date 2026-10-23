package com.example.smartpantry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/** Pantry List screen: READ (list) and DELETE, and launches Add/Edit with Intents. */
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private TextView emptyView;
    private final List<PantryItem> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NavHelper.applyInsets(findViewById(R.id.main));

        db = new DatabaseHelper(this);
        emptyView = findViewById(R.id.text_empty);

        RecyclerView recycler = findViewById(R.id.recycler_pantry);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(items, new PantryAdapter.Listener() {
            @Override
            public void onEdit(PantryItem item) {
                // Intent carries the item id to the Add/Edit screen
                Intent intent = new Intent(MainActivity.this, AddEditItemActivity.class);
                intent.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, item.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(final PantryItem item) {
                confirmDelete(item);
            }
        });
        recycler.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddEditItemActivity.class)));

        NavHelper.setup(this, R.id.nav_pantry);
    }

    /** Reload every time the screen comes back, so changes made elsewhere show up. */
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS, MODE_PRIVATE);
        adapter.setHighlightExpiring(prefs.getBoolean(SettingsActivity.KEY_EXPIRY_ALERTS, true));
        loadItems();
    }

    private void loadItems() {
        items.clear();
        items.addAll(db.getAllPantryItems());
        adapter.notifyDataSetChanged();
        emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void confirmDelete(final PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deletePantryItem(item.getId());
                    loadItems();
                    Toast.makeText(MainActivity.this, "Ingredient deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
