package com.thesis.receiptify.model.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeStepDTO {
    private Long id;

    @NotNull(message = "Step number is required")
    @PositiveOrZero(message = "Step number must be zero or positive")
    private Integer stepNumber;

    @NotBlank(message = "Instruction is required")
    @Size(max = 1000, message = "Instruction must be less than 1000 characters")
    private String instruction;
}
