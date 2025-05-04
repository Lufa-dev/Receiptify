package com.thesis.receiptify.controller;

import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.model.dto.NutritionDTO;
import com.thesis.receiptify.service.NutritionService;
import com.thesis.receiptify.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    private final NutritionService nutritionService;
    private final RecipeService recipeService;

    /**
     * Get nutrition information for a specific recipe
     */
    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<?> getRecipeNutrition(@PathVariable Long recipeId) {
        try {
            Recipe recipe = recipeService.getRecipeEntityById(recipeId);
            NutritionDTO nutrition = nutritionService.calculateNutrition(recipe);

            // Calculate daily values percentages
            Map<String, Integer> dailyValues = nutritionService.calculateDailyValues(nutrition);

            Map<String, Object> response = new HashMap<>();
            response.put("nutrition", nutrition);
            response.put("dailyValues", dailyValues);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to calculate nutrition: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}