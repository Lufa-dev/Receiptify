package com.thesis.receiptify.model.dto;

import com.thesis.receiptify.model.enums.IngredientType;
import com.thesis.receiptify.model.enums.UnitType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDTO {
    private Long id;

    @NotNull(message = "Ingredient type is required")
    private IngredientType type;

    private String amount;

    private UnitType unit;

    private String name;
}
