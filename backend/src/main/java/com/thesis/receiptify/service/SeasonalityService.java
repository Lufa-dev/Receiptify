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

/**
 * Service responsible for analyzing and determining recipe and ingredient seasonality.
 * Evaluates how seasonal a recipe is based on its ingredients and the current month.
 */
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
     * Analyzes a recipe for seasonality with an improved algorithm that:
     * 1. Gives bonus points for truly seasonal ingredients
     * 2. Slightly penalizes recipes with only year-round ingredients
     *
     * The seasonality score ranges from 0-100, where:
     * - 0: No ingredients are in season
     * - 100: All ingredients are in season
     *
     * @param recipe The recipe to analyze
     * @return The seasonality analysis for the recipe with score and breakdown
     */
    public RecipeSeasonalityDTO analyzeRecipeSeasonality(Recipe recipe) {
        if (recipe == null || recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            return RecipeSeasonalityDTO.builder()
                    .recipeId(recipe != null ? recipe.getId() : null)
                    .seasonalScore(0)
                    .inSeasonCount(0)
                    .outOfSeasonCount(0)
                    .yearRoundCount(0)
                    .trulySeasonalCount(0)
                    .ingredientSeasonality(new ArrayList<>())
                    .build();
        }

        List<IngredientSeasonalityDTO> ingredientSeasonalities = recipe.getIngredients().stream()
                .map(this::getIngredientSeasonality)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        // Count ingredients by seasonality type
        long inSeasonCount = ingredientSeasonalities.stream()
                .filter(IngredientSeasonalityDTO::isInSeason)
                .count();

        long outOfSeasonCount = ingredientSeasonalities.size() - inSeasonCount;

        // Count year-round ingredients
        long yearRoundCount = ingredientSeasonalities.stream()
                .filter(dto -> dto.getSeasonality().toLowerCase().contains("year-round"))
                .count();

        // Count truly seasonal ingredients (in season but not year-round)
        long trulySeasonalCount = inSeasonCount - yearRoundCount;

        // Calculate basic seasonal score (as before)
        int basicScore = ingredientSeasonalities.isEmpty() ? 0 :
                (int) Math.round((double) inSeasonCount / ingredientSeasonalities.size() * 100);

        // Apply a bonus for recipes with seasonal (non-year-round) ingredients
        // and a slight penalty for recipes with only year-round ingredients
        int adjustedScore = basicScore;

        if (trulySeasonalCount > 0) {
            // Bonus for having truly seasonal ingredients (up to +15 points)
            int seasonalBonus = (int) Math.min(15, Math.round((double) trulySeasonalCount / ingredientSeasonalities.size() * 30));
            adjustedScore = Math.min(100, adjustedScore + seasonalBonus);
        } else if (yearRoundCount == ingredientSeasonalities.size() && yearRoundCount > 0) {
            // Small penalty for recipes with only year-round ingredients
            adjustedScore = Math.max(0, adjustedScore - 10);
        }

        return RecipeSeasonalityDTO.builder()
                .recipeId(recipe.getId())
                .seasonalScore(adjustedScore)
                .inSeasonCount((int) inSeasonCount)
                .outOfSeasonCount((int) outOfSeasonCount)
                .yearRoundCount((int) yearRoundCount)
                .trulySeasonalCount((int) trulySeasonalCount)
                .ingredientSeasonality(ingredientSeasonalities)
                .build();
    }
}
