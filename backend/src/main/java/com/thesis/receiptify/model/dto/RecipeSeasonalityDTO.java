package com.thesis.receiptify.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeSeasonalityDTO {
    private Long recipeId;
    private int seasonalScore;
    private int inSeasonCount;
    private int outOfSeasonCount;
    private int yearRoundCount;  // New field
    private int trulySeasonalCount;  // New field
    private List<IngredientSeasonalityDTO> ingredientSeasonality;
}
