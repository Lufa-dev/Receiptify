package com.thesis.receiptify.service;

import com.thesis.receiptify.model.Ingredient;
import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.model.dto.IngredientSeasonalityDTO;
import com.thesis.receiptify.model.dto.RecipeSeasonalityDTO;
import com.thesis.receiptify.model.enums.IngredientSeasonality;
import com.thesis.receiptify.model.enums.IngredientSeasonality.SeasonalityStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeasonalityService {

    /**
     * Gets the current month for seasonality calculations
     *
     * @return the current month
     */
    public Month getCurrentMonth() {
        return LocalDate.now().getMonth();
    }

    /**
     * Checks if an ingredient is in season for the current month
     *
     * @param ingredient the ingredient to check
     * @return true if the ingredient is in season
     */
    public boolean isInSeason(Ingredient ingredient) {
        if (ingredient == null || ingredient.getType() == null) {
            return false;
        }

        return ingredient.getType().getSeasonality().isInSeason(getCurrentMonth());
    }

    /**
     * Gets the seasonality status of an ingredient for the current month
     *
     * @param ingredient the ingredient to check
     * @return the seasonality status
     */
    public SeasonalityStatus getSeasonalityStatus(Ingredient ingredient) {
        if (ingredient == null || ingredient.getType() == null) {
            return SeasonalityStatus.OUT_OF_SEASON;
        }

        return ingredient.getType().getSeasonality().getStatus(getCurrentMonth());
    }

    /**
     * Creates a seasonality DTO for an ingredient
     *
     * @param ingredient the ingredient
     * @return the seasonality information
     */
    public IngredientSeasonalityDTO getIngredientSeasonality(Ingredient ingredient) {
        if (ingredient == null || ingredient.getType() == null) {
            return null;
        }

        Month currentMonth = getCurrentMonth();
        IngredientSeasonality seasonality = ingredient.getType().getSeasonality();
        SeasonalityStatus status = seasonality.getStatus(currentMonth);

        return IngredientSeasonalityDTO.builder()
                .ingredientId(ingredient.getId())
                .ingredientName(ingredient.getName())
                .seasonality(seasonality.getDisplayName())
                .status(status.getDisplayName())
                .isInSeason(status == SeasonalityStatus.IN_SEASON)
                .isComingSoon(status == SeasonalityStatus.COMING_SOON)
                .build();
    }

    /**
     * Analyzes a recipe for seasonality information
     *
     * @param recipe the recipe to analyze
     * @return the seasonality analysis for the recipe
     */
    public RecipeSeasonalityDTO analyzeRecipeSeasonality(Recipe recipe) {
        if (recipe == null || recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            return RecipeSeasonalityDTO.builder()
                    .recipeId(recipe != null ? recipe.getId() : null)
                    .seasonalScore(0)
                    .inSeasonCount(0)
                    .outOfSeasonCount(0)
                    .ingredientSeasonality(new ArrayList<>())
                    .build();
        }

        List<IngredientSeasonalityDTO> ingredientSeasonalities = recipe.getIngredients().stream()
                .map(this::getIngredientSeasonality)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        long inSeasonCount = ingredientSeasonalities.stream()
                .filter(IngredientSeasonalityDTO::isInSeason)
                .count();

        long outOfSeasonCount = ingredientSeasonalities.size() - inSeasonCount;

        // Calculate a seasonal score from 0-100
        int seasonalScore = ingredientSeasonalities.isEmpty() ? 0 :
                (int) Math.round((double) inSeasonCount / ingredientSeasonalities.size() * 100);

        return RecipeSeasonalityDTO.builder()
                .recipeId(recipe.getId())
                .seasonalScore(seasonalScore)
                .inSeasonCount((int) inSeasonCount)
                .outOfSeasonCount((int) outOfSeasonCount)
                .ingredientSeasonality(ingredientSeasonalities)
                .build();
    }
}
