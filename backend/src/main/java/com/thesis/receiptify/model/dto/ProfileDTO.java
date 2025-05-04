package com.thesis.receiptify.model.dto;

import com.thesis.receiptify.model.enums.IngredientType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String roles;

    // Preference fields
    private Set<String> preferredCategories = new HashSet<>();
    private Set<String> preferredCuisines = new HashSet<>();
    private Set<String> favoriteIngredients = new HashSet<>();
    private Set<String> dislikedIngredients = new HashSet<>();
    private Integer maxPrepTime;
    private String difficultyPreference;
    private Boolean preferSeasonalRecipes = false;
}
