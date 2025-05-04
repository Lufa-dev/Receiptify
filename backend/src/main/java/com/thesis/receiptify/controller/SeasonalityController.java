package com.thesis.receiptify.controller;

import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.model.dto.RecipeSeasonalityDTO;
import com.thesis.receiptify.service.RecipeService;
import com.thesis.receiptify.service.SeasonalityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<RecipeSeasonalityDTO> getRecipeSeasonality(@PathVariable Long recipeId) {
        try {
            Recipe recipe = recipeService.getRecipeEntityById(recipeId);
            RecipeSeasonalityDTO seasonalityDTO = seasonalityService.analyzeRecipeSeasonality(recipe);
            return ResponseEntity.ok(seasonalityDTO);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
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