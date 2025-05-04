package com.thesis.receiptify.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientSeasonalityDTO {
    private Long ingredientId;
    private String ingredientName;
    private String seasonality;
    private String status;
    private boolean isInSeason;
    private boolean isComingSoon;
}
