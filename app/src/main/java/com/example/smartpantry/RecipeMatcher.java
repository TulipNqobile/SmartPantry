package com.example.smartpantry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Core business logic: the STRICT-MATCHING rule (assignment Section 2.3).
 * A recipe is suggested only if EVERY ingredient is in the pantry in at least the required quantity.
 */
public class RecipeMatcher {

    /** Returns only the recipes that can be made right now from the given pantry. */
    public static List<Recipe> getSuggestedRecipes(List<PantryItem> pantry, List<Recipe> allRecipes) {
        // 1. Build a lookup: normalised name -> (base unit -> total quantity in that base unit).
        //    Totals are summed so two entries of "egg" (e.g. 2 + 3) count as 5.
        Map<String, Map<String, Double>> stock = new HashMap<>();
        for (PantryItem item : pantry) {
            String key = normalizeName(item.getName());
            String baseUnit = baseUnit(item.getUnit());
            double baseQty = item.getQuantity() * factorToBase(item.getUnit());
            Map<String, Double> byUnit = stock.get(key);
            if (byUnit == null) {
                byUnit = new HashMap<>();
                stock.put(key, byUnit);
            }
            Double existing = byUnit.get(baseUnit);
            byUnit.put(baseUnit, (existing == null ? 0.0 : existing) + baseQty);
        }

        // 2. A recipe qualifies only if ALL of its ingredients pass.
        List<Recipe> suggested = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (canMake(recipe, stock)) {
                suggested.add(recipe);
            }
        }
        return suggested;
    }

    private static boolean canMake(Recipe recipe, Map<String, Map<String, Double>> stock) {
        if (recipe.getIngredients().isEmpty()) return false;
        for (RecipeIngredient ing : recipe.getIngredients()) {
            Map<String, Double> byUnit = stock.get(normalizeName(ing.getName()));
            if (byUnit == null) return false;                      // ingredient missing entirely
            Double have = byUnit.get(baseUnit(ing.getUnit()));
            if (have == null) return false;                        // incompatible unit type (e.g. have pieces, need grams)
            double need = ing.getQuantity() * factorToBase(ing.getUnit());
            if (have + 1e-9 < need) return false;                  // not enough quantity
        }
        return true;
    }

    /** Lower-cases, trims and singularises so "Tomatoes" and "tomato" match. Applied to BOTH sides. */
    public static String normalizeName(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        if (s.endsWith("ies") && s.length() > 4) return s.substring(0, s.length() - 3) + "y";   // berries -> berry
        if (s.endsWith("oes") && s.length() > 4) return s.substring(0, s.length() - 2);          // tomatoes -> tomato
        if (s.endsWith("ches") || s.endsWith("shes") || s.endsWith("sses") || s.endsWith("xes"))
            return s.substring(0, s.length() - 2);                                               // peaches -> peach
        if (s.endsWith("ss")) return s;
        if (s.endsWith("s") && s.length() > 3) return s.substring(0, s.length() - 1);            // eggs -> egg
        return s;
    }

    /** Maps a unit to its base unit: grams, millilitres or pieces. */
    public static String baseUnit(String unit) {
        String u = cleanUnit(unit);
        switch (u) {
            case "g": case "gram": case "kg": case "kilogram": case "mg":
                return "g";
            case "ml": case "l": case "litre": case "liter": case "tsp": case "teaspoon":
            case "tbsp": case "tablespoon": case "cup":
                return "ml";
            default:
                return "pcs"; // pcs, piece, whole, clove, slice, can, or blank
        }
    }

    /** Multiplier that converts a quantity in this unit into its base unit. */
    public static double factorToBase(String unit) {
        switch (cleanUnit(unit)) {
            case "kg": case "kilogram": return 1000.0;
            case "mg": return 0.001;
            case "l": case "litre": case "liter": return 1000.0;
            case "tsp": case "teaspoon": return 5.0;
            case "tbsp": case "tablespoon": return 15.0;
            case "cup": return 240.0;
            default: return 1.0;
        }
    }

    private static String cleanUnit(String unit) {
        if (unit == null) return "";
        String u = unit.trim().toLowerCase(Locale.ROOT);
        if (u.endsWith("s") && u.length() > 2) u = u.substring(0, u.length() - 1); // "cups" -> "cup", "grams" -> "gram"
        return u;
    }
}
