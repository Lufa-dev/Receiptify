package com.thesis.receiptify.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDTO {
    private Long id;

    @NotBlank(message = "Recipe title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    private String title;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    private String imageUrl;

    @NotEmpty(message = "At least one ingredient is required")
    @Valid
    private List<IngredientDTO> ingredients;

    @NotEmpty(message = "At least one step is required")
    @Valid
    private List<RecipeStepDTO> steps;

    private UserDTO user;

    private String category;
    private String cuisine;
    private Integer servings;
    private String difficulty;
    private String costRating;

    private Integer prepTime;
    private Integer cookTime;
    private Integer bakingTime;
    private Integer bakingTemp;
    private Integer panSize;
    private String bakingMethod;

    private Double averageRating;
    private Integer totalRatings;
    private Integer totalComments;
    private Integer userRating;

    private RecipeSeasonalityDTO seasonalityInfo;
}
