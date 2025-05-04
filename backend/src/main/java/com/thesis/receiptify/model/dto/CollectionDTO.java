package com.thesis.receiptify.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class CollectionDTO {
    private Long id;

    @NotBlank(message = "Collection name is required")
    @Size(max = 50, message = "Name must be less than 50 characters")
    private String name;

    @Size(max = 200, message = "Description must be less than 200 characters")
    private String description;

    private boolean isDefault;

    private UserDTO user;

    private Set<Long> recipeIds = new HashSet<>();

    private int recipeCount;
}
