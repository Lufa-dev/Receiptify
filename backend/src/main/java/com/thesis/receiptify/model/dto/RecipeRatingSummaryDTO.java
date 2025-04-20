package com.thesis.receiptify.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeRatingSummaryDTO {
    private Long recipeId;
    private Double averageRating;
    private Integer totalRatings;
    private Integer totalComments;
}
