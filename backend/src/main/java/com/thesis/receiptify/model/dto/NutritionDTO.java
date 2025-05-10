package com.thesis.receiptify.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionDTO {
    private int calories;        // kcal
    private double protein;      // g
    private double fat;          // g
    private double carbs;        // g
    private double fiber;        // g
    private double sugar;        // g
    private int sodium;          // mg
    private int servings;        // number of servings

    // Calculate calories from macronutrients
    public int getCaloriesFromProtein() {
        return (int) Math.round(protein * 4); // 4 calories per gram of protein
    }

    public int getCaloriesFromFat() {
        return (int) Math.round(fat * 9);     // 9 calories per gram of fat
    }

    public int getCaloriesFromCarbs() {
        return (int) Math.round(carbs * 4);   // 4 calories per gram of carbs
    }
}
