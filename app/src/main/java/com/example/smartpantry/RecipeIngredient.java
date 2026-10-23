package com.example.smartpantry;

/** One required ingredient line inside a recipe. */
public class RecipeIngredient {
    private final String name;
    private final double quantity;
    private final String unit;

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }

    @Override
    public String toString() {
        String q = (quantity == Math.floor(quantity)) ? String.valueOf((long) quantity) : String.valueOf(quantity);
        return q + " " + unit + " " + name;
    }
}
