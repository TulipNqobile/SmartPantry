package com.example.smartpantry;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/** Add/Edit Ingredient screen: CREATE and UPDATE, with input validation. */
public class AddEditItemActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";
    private static final String[] UNITS = {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "pcs"};

    private EditText etName, etQuantity, etExpiry;
    private Spinner spUnit;
    private DatabaseHelper db;
    private PantryItem existing; // null when adding a new item

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);
        NavHelper.applyInsets(findViewById(R.id.main));

        db = new DatabaseHelper(this);
        etName = findViewById(R.id.edit_name);
        etQuantity = findViewById(R.id.edit_quantity);
        etExpiry = findViewById(R.id.edit_expiry);
        spUnit = findViewById(R.id.spinner_unit);
        TextView title = findViewById(R.id.text_title);
        Button save = findViewById(R.id.button_save);
        Button cancel = findViewById(R.id.button_cancel);

        spUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, UNITS));

        // If the Intent contains an item id we are editing an existing item
        long itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            existing = db.getPantryItem(itemId);
        }
        if (existing != null) {
            title.setText("Edit Ingredient");
            save.setText("Update");
            etName.setText(existing.getName());
            etQuantity.setText(PantryAdapter.formatQty(existing.getQuantity()));
            etExpiry.setText(existing.getExpiryDate());
            for (int i = 0; i < UNITS.length; i++) {
                if (UNITS[i].equalsIgnoreCase(existing.getUnit())) {
                    spUnit.setSelection(i);
                }
            }
        }

        save.setOnClickListener(v -> saveItem());
        cancel.setOnClickListener(v -> finish());
    }

    private void saveItem() {
        String name = etName.getText().toString().trim();
        String qtyText = etQuantity.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();

        // ---- validation ----
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        if (!name.matches(".*[A-Za-z].*")) {
            etName.setError("Enter a valid ingredient name");
            etName.requestFocus();
            return;
        }
        double quantity;
        try {
            quantity = Double.parseDouble(qtyText);
        } catch (NumberFormatException e) {
            etQuantity.setError("Enter a valid quantity");
            etQuantity.requestFocus();
            return;
        }
        if (quantity <= 0 || quantity > 100000) {
            etQuantity.setError("Quantity must be greater than 0");
            etQuantity.requestFocus();
            return;
        }
        if (!expiry.isEmpty() && !isValidDate(expiry)) {
            etExpiry.setError("Use the format yyyy-MM-dd, e.g. 2026-11-30");
            etExpiry.requestFocus();
            return;
        }

        String unit = spUnit.getSelectedItem().toString();

        if (existing == null) {
            PantryItem item = new PantryItem(0, name, quantity, unit, expiry);
            db.addPantryItem(item);                       // CREATE
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            existing.setName(name);
            existing.setQuantity(quantity);
            existing.setUnit(unit);
            existing.setExpiryDate(expiry);
            db.updatePantryItem(existing);                // UPDATE
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private boolean isValidDate(String s) {
        if (s.length() != 10) return false;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);
        try {
            format.parse(s);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
