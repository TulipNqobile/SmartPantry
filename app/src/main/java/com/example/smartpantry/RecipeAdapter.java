package com.example.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** Custom adapter that lists the recipes the user can make right now. */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface OnRecipeClick {
        void onClick(Recipe recipe);
    }

    private final List<Recipe> recipes;
    private final OnRecipeClick listener;

    public RecipeAdapter(List<Recipe> recipes, OnRecipeClick listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Recipe recipe = recipes.get(position);
        holder.name.setText(recipe.getName());
        holder.summary.setText(recipe.getIngredients().size() + " ingredients - all in your pantry");
        holder.itemView.setOnClickListener(v -> listener.onClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, summary;

        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.text_recipe_name);
            summary = v.findViewById(R.id.text_recipe_summary);
        }
    }
}
