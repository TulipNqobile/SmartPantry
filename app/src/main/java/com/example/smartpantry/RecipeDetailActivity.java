package com.example.smartpantry;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/** Recipe Detail screen: full ingredient list and method for the selected recipe. */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        NavHelper.applyInsets(findViewById(R.id.main));

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = new DatabaseHelper(this).getRecipe(recipeId);
        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.text_recipe_title)).setText(recipe.getName());

        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ing : recipe.getIngredients()) {
            sb.append("\u2022 ").append(ing.toString()).append("\n");
        }
        ((TextView) findViewById(R.id.text_ingredients)).setText(sb.toString().trim());
        ((TextView) findViewById(R.id.text_steps)).setText(recipe.getSteps());

        Button back = findViewById(R.id.button_back);
        back.setOnClickListener(v -> finish());
    }
}
