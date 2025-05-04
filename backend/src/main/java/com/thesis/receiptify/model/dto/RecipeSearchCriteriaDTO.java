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
public class RecipeSearchCriteriaDTO {
    // Text search
    private String searchQuery;

    // Ingredient filters
    private List<String> includeIngredients;
    private List<String> excludeIngredients;

    // Recipe characteristics
    private String category;
    private String cuisine;
    private String difficulty;
    private String costRating;

    // Numeric ranges
    private Integer minServings;
    private Integer maxServings;
    private Integer maxPrepTime;
    private Integer maxCookTime;
    private Integer maxTotalTime;

    // Dietary filters
    private List<String> dietaryTags;

    // Sorting
    private String sortBy;
    private String sortDirection;
}

