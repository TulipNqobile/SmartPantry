package com.example.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite helper: creates the tables, seeds 20 recipes on first run,
 * and provides full CRUD for pantry items plus read access to recipes.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smartpantry.db";
    private static final int DB_VERSION = 1;

    // Pantry table
    public static final String T_PANTRY = "pantry";
    // Recipe tables
    public static final String T_RECIPE = "recipes";
    public static final String T_RECIPE_ING = "recipe_ingredients";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // needed for ON DELETE CASCADE
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PANTRY + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "expiry_date TEXT)");

        db.execSQL("CREATE TABLE " + T_RECIPE + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "steps TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + T_RECIPE_ING + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "FOREIGN KEY(recipe_id) REFERENCES " + T_RECIPE + "(id) ON DELETE CASCADE)");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPE_ING);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPE);
        db.execSQL("DROP TABLE IF EXISTS " + T_PANTRY);
        onCreate(db);
    }

    // ---------------------------------------------------------------- PANTRY CRUD

    /** CREATE */
    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(T_PANTRY, null, toValues(item));
    }

    /** READ (all) */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(T_PANTRY, null, null, null, null, null, "name COLLATE NOCASE ASC");
        try {
            while (c.moveToNext()) {
                list.add(fromCursor(c));
            }
        } finally {
            c.close();
        }
        return list;
    }

    /** READ (one) - used to pre-fill the Edit screen. Returns null if not found. */
    public PantryItem getPantryItem(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(T_PANTRY, null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        try {
            return c.moveToFirst() ? fromCursor(c) : null;
        } finally {
            c.close();
        }
    }

    /** UPDATE */
    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(T_PANTRY, toValues(item), "id=?", new String[]{String.valueOf(item.getId())});
    }

    /** DELETE */
    public int deletePantryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(T_PANTRY, "id=?", new String[]{String.valueOf(id)});
    }

    private ContentValues toValues(PantryItem item) {
        ContentValues v = new ContentValues();
        v.put("name", item.getName());
        v.put("quantity", item.getQuantity());
        v.put("unit", item.getUnit());
        v.put("expiry_date", item.getExpiryDate());
        return v;
    }

    private PantryItem fromCursor(Cursor c) {
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.getString(c.getColumnIndexOrThrow("expiry_date")));
    }

    // ---------------------------------------------------------------- RECIPES (read only)

    /** Loads every recipe together with its ingredient list. */
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(T_RECIPE, null, null, null, null, null, "name ASC");
        try {
            while (c.moveToNext()) {
                Recipe r = new Recipe(
                        c.getLong(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getString(c.getColumnIndexOrThrow("steps")));
                loadIngredients(db, r);
                recipes.add(r);
            }
        } finally {
            c.close();
        }
        return recipes;
    }

    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(T_RECIPE, null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (!c.moveToFirst()) return null;
            Recipe r = new Recipe(
                    c.getLong(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getString(c.getColumnIndexOrThrow("steps")));
            loadIngredients(db, r);
            return r;
        } finally {
            c.close();
        }
    }

    private void loadIngredients(SQLiteDatabase db, Recipe r) {
        Cursor c = db.query(T_RECIPE_ING, null, "recipe_id=?",
                new String[]{String.valueOf(r.getId())}, null, null, "id ASC");
        try {
            while (c.moveToNext()) {
                r.addIngredient(new RecipeIngredient(
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getDouble(c.getColumnIndexOrThrow("quantity")),
                        c.getString(c.getColumnIndexOrThrow("unit"))));
            }
        } finally {
            c.close();
        }
    }

    // ---------------------------------------------------------------- SEED DATA

    /** Each ingredient string is "name|quantity|unit". */
    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs on Toast",
                "1. Whisk the eggs.\n2. Melt butter in a pan and scramble the eggs gently.\n3. Toast the bread and serve the eggs on top.",
                "egg|2|pcs", "bread|2|pcs", "butter|10|g");
        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta.\n2. Fry chopped onion and garlic in olive oil.\n3. Add chopped tomatoes and simmer 10 minutes.\n4. Toss with the pasta.",
                "pasta|200|g", "tomato|3|pcs", "onion|1|pcs", "garlic|2|pcs", "olive oil|1|tbsp");
        addRecipe(db, "Cheese Omelette",
                "1. Beat the eggs.\n2. Melt butter in a pan and pour in the eggs.\n3. Add grated cheese, fold and serve.",
                "egg|3|pcs", "cheese|50|g", "butter|10|g");
        addRecipe(db, "Egg Fried Rice",
                "1. Fry chopped onion.\n2. Add cooked rice and soy sauce.\n3. Push aside, scramble the eggs, then mix everything together.",
                "rice|200|g", "egg|2|pcs", "onion|1|pcs", "soy sauce|1|tbsp");
        addRecipe(db, "Pancakes",
                "1. Mix flour, sugar, egg and milk into a batter.\n2. Fry ladles of batter in buttered pan until golden on both sides.",
                "flour|150|g", "milk|250|ml", "egg|1|pcs", "sugar|1|tbsp", "butter|20|g");
        addRecipe(db, "Garlic Bread",
                "1. Mash crushed garlic into soft butter.\n2. Spread on the bread.\n3. Bake at 200C for 8 minutes.",
                "bread|4|pcs", "butter|40|g", "garlic|2|pcs");
        addRecipe(db, "Mashed Potatoes",
                "1. Boil peeled potatoes until soft.\n2. Drain and mash with butter, warm milk and salt.",
                "potato|4|pcs", "butter|30|g", "milk|50|ml", "salt|1|tsp");
        addRecipe(db, "Vegetable Soup",
                "1. Chop the potatoes, carrots and onion.\n2. Simmer in the stock for 25 minutes.\n3. Season and serve.",
                "potato|2|pcs", "carrot|2|pcs", "onion|1|pcs", "stock|500|ml");
        addRecipe(db, "Tuna Mayo Sandwich",
                "1. Drain the tuna and mix with mayonnaise.\n2. Spread on bread and close the sandwich.",
                "tuna|1|pcs", "mayonnaise|2|tbsp", "bread|2|pcs");
        addRecipe(db, "Cheese Toastie",
                "1. Butter the outside of the bread.\n2. Place cheese between slices.\n3. Toast in a pan until golden and melted.",
                "bread|2|pcs", "cheese|60|g", "butter|10|g");
        addRecipe(db, "Banana Pancakes",
                "1. Mash the bananas.\n2. Mix with eggs and flour.\n3. Fry small spoonfuls until golden.",
                "banana|2|pcs", "egg|2|pcs", "flour|50|g");
        addRecipe(db, "Lentil Curry",
                "1. Fry onion and garlic.\n2. Add curry powder, tomatoes and lentils.\n3. Add water and simmer for 25 minutes.",
                "lentil|200|g", "onion|1|pcs", "tomato|2|pcs", "garlic|2|pcs", "curry powder|1|tbsp");
        addRecipe(db, "Rice and Beans",
                "1. Fry the onion.\n2. Add beans, rice, salt and water.\n3. Cook covered until the rice is tender.",
                "rice|200|g", "beans|200|g", "onion|1|pcs", "salt|1|tsp");
        addRecipe(db, "Peanut Butter Banana Toast",
                "1. Toast the bread.\n2. Spread with peanut butter.\n3. Top with sliced banana.",
                "bread|2|pcs", "peanut butter|2|tbsp", "banana|1|pcs");
        addRecipe(db, "Baked Potato with Cheese",
                "1. Pierce the potatoes and bake at 200C for 50 minutes.\n2. Split open, add butter and grated cheese.",
                "potato|2|pcs", "cheese|60|g", "butter|20|g");
        addRecipe(db, "Spaghetti Aglio e Olio",
                "1. Boil the pasta.\n2. Gently fry sliced garlic and chilli flakes in olive oil.\n3. Toss with the pasta.",
                "pasta|200|g", "garlic|4|pcs", "olive oil|3|tbsp", "chilli flakes|1|tsp");
        addRecipe(db, "Cheesy Macaroni",
                "1. Boil the pasta.\n2. Melt butter, add milk and cheese to make a sauce.\n3. Stir in the pasta.",
                "pasta|200|g", "cheese|100|g", "milk|200|ml", "butter|20|g");
        addRecipe(db, "French Toast",
                "1. Whisk eggs, milk and sugar.\n2. Dip the bread slices.\n3. Fry until golden on both sides.",
                "bread|4|pcs", "egg|2|pcs", "milk|100|ml", "sugar|1|tsp");
        addRecipe(db, "Banana Oatmeal",
                "1. Heat oats and milk, stirring, for 5 minutes.\n2. Top with sliced banana.",
                "oat|80|g", "milk|250|ml", "banana|1|pcs");
        addRecipe(db, "Chicken Rice Bowl",
                "1. Cut the chicken and fry with onion.\n2. Add soy sauce.\n3. Serve over cooked rice.",
                "chicken|300|g", "rice|200|g", "onion|1|pcs", "soy sauce|1|tbsp");
    }

    private void addRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues rv = new ContentValues();
        rv.put("name", name);
        rv.put("steps", steps);
        long recipeId = db.insert(T_RECIPE, null, rv);

        for (String line : ingredients) {
            String[] p = line.split("\\|");
            ContentValues iv = new ContentValues();
            iv.put("recipe_id", recipeId);
            iv.put("name", p[0]);
            iv.put("quantity", Double.parseDouble(p[1]));
            iv.put("unit", p[2]);
            db.insert(T_RECIPE_ING, null, iv);
        }
    }
}
