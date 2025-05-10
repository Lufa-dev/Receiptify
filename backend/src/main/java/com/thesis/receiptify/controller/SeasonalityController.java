package com.thesis.receiptify.controller;

import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.model.dto.RecipeSeasonalityDTO;
import com.thesis.receiptify.service.RecipeService;
import com.thesis.receiptify.service.SeasonalityService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/seasonality")
@RequiredArgsConstructor
public class SeasonalityController {

    private final SeasonalityService seasonalityService;
    private final RecipeService recipeService;

    /**
     * Get seasonality analysis for a specific recipe
     *
     * @param recipeId the recipe ID
     * @return seasonality information
     */
    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<?> getRecipeSeasonality(@PathVariable Long recipeId) {
        try {
            Recipe recipe = recipeService.getRecipeEntityById(recipeId);
            RecipeSeasonalityDTO seasonalityDTO = seasonalityService.analyzeRecipeSeasonality(recipe);
            return ResponseEntity.ok(seasonalityDTO);
        } catch (EntityNotFoundException e) {
            // Handle not found exception
            Map<String, String> error = new HashMap<>();
            error.put("error", "Recipe not found with id: " + recipeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            // Handle other exceptions
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error analyzing recipe seasonality: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get the current month being used for seasonality calculations
     *
     * @return the current month
     */
    @GetMapping("/current-month")
    public ResponseEntity<String> getCurrentMonth() {
        return ResponseEntity.ok(seasonalityService.getCurrentMonth().toString());
    }
}