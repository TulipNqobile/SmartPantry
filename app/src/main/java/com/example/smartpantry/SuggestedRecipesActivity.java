package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/** Suggested Recipes screen: runs the strict-matching rule against the current pantry. */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private RecyclerView recycler;
    private TextView emptyView, countView;
    private final List<Recipe> recipes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);
        NavHelper.applyInsets(findViewById(R.id.main));

        db = new DatabaseHelper(this);
        emptyView = findViewById(R.id.text_empty);
        countView = findViewById(R.id.text_count);

        recycler = findViewById(R.id.recycler_recipes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(recipes, recipe -> {
            Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
            startActivity(intent);
        });
        recycler.setAdapter(adapter);

        NavHelper.setup(this, R.id.nav_recipes);
    }

    /** Re-run the matching every time the screen is shown, so it always reflects the current pantry. */
    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        List<PantryItem> pantry = db.getAllPantryItems();
        List<Recipe> allRecipes = db.getAllRecipes();
        List<Recipe> suggested = RecipeMatcher.getSuggestedRecipes(pantry, allRecipes);

        recipes.clear();
        recipes.addAll(suggested);
        adapter.notifyDataSetChanged();

        boolean empty = suggested.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        countView.setText(suggested.size() + " of " + allRecipes.size() + " recipes you can make right now");
    }
}
