package com.thesis.receiptify.service;

import com.thesis.receiptify.model.Ingredient;
import com.thesis.receiptify.model.Profile;
import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.model.RecipeStep;
import com.thesis.receiptify.model.dto.IngredientDTO;
import com.thesis.receiptify.model.dto.RecipeDTO;
import com.thesis.receiptify.model.dto.RecipeStepDTO;
import com.thesis.receiptify.repository.ProfileRepository;
import com.thesis.receiptify.repository.RecipeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final ProfileRepository profileRepository;

    @Transactional
    public RecipeDTO createRecipe(RecipeDTO recipeDTO, String username) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Recipe recipe = Recipe.builder()
                .title(recipeDTO.getTitle())
                .description(recipeDTO.getDescription())
                .imageUrl(recipeDTO.getImageUrl())
                .user(user)
                .ingredients(new ArrayList<>())
                .steps(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        // Add ingredients
        if (recipeDTO.getIngredients() != null) {
            for (IngredientDTO ingredientDTO : recipeDTO.getIngredients()) {
                Ingredient ingredient = Ingredient.builder()
                        .type(ingredientDTO.getType())
                        .amount(ingredientDTO.getAmount())
                        .unit(ingredientDTO.getUnit())
                        .name(ingredientDTO.getName() != null ? ingredientDTO.getName() : ingredientDTO.getType().getDisplayName())
                        .build();
                recipe.addIngredient(ingredient);
            }
        }

        // Add steps
        if (recipeDTO.getSteps() != null) {
            for (RecipeStepDTO stepDTO : recipeDTO.getSteps()) {
                RecipeStep step = RecipeStep.builder()
                        .stepNumber(stepDTO.getStepNumber())
                        .instruction(stepDTO.getInstruction())
                        .build();
                recipe.addStep(step);
            }
        }

        Recipe savedRecipe = recipeRepository.save(recipe);
        return mapToDTO(savedRecipe);
    }

    @Transactional(readOnly = true)
    public RecipeDTO getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));
        return mapToDTO(recipe);
    }

    @Transactional(readOnly = true)
    public Page<RecipeDTO> getAllRecipes(Pageable pageable) {
        return recipeRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<RecipeDTO> getUserRecipes(String username, Pageable pageable) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return recipeRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<RecipeDTO> searchRecipes(String query, Pageable pageable) {
        return recipeRepository.searchRecipes(query, pageable)
                .map(this::mapToDTO);
    }

    @Transactional
    public RecipeDTO updateRecipe(Long id, RecipeDTO recipeDTO, String username) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        // Check if the user is the owner of the recipe
        if (!recipe.getUser().getUsername().equals(username)) {
            throw new SecurityException("You don't have permission to update this recipe");
        }

        recipe.setTitle(recipeDTO.getTitle());
        recipe.setDescription(recipeDTO.getDescription());
        recipe.setImageUrl(recipeDTO.getImageUrl());
        recipe.setUpdatedAt(LocalDateTime.now());

        // Clear and re-add ingredients
        recipe.getIngredients().clear();
        for (IngredientDTO ingredientDTO : recipeDTO.getIngredients()) {
            Ingredient ingredient = Ingredient.builder()
                    .type(ingredientDTO.getType())
                    .amount(ingredientDTO.getAmount())
                    .unit(ingredientDTO.getUnit())
                    .name(ingredientDTO.getName() != null ? ingredientDTO.getName() : ingredientDTO.getType().getDisplayName())
                    .build();
            recipe.addIngredient(ingredient);
        }

        // Clear and re-add steps
        recipe.getSteps().clear();
        for (RecipeStepDTO stepDTO : recipeDTO.getSteps()) {
            RecipeStep step = RecipeStep.builder()
                    .stepNumber(stepDTO.getStepNumber())
                    .instruction(stepDTO.getInstruction())
                    .build();
            recipe.addStep(step);
        }

        Recipe updatedRecipe = recipeRepository.save(recipe);
        return mapToDTO(updatedRecipe);
    }

    @Transactional
    public void deleteRecipe(Long id, String username) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        // Check if the user is the owner of the recipe
        if (!recipe.getUser().getUsername().equals(username)) {
            throw new SecurityException("You don't have permission to delete this recipe");
        }

        recipeRepository.delete(recipe);
    }

    // Helper methods to map between entities and DTOs
    private RecipeDTO mapToDTO(Recipe recipe) {
        List<IngredientDTO> ingredientDTOs = recipe.getIngredients().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        List<RecipeStepDTO> stepDTOs = recipe.getSteps().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return RecipeDTO.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .imageUrl(recipe.getImageUrl())
                .ingredients(ingredientDTOs)
                .steps(stepDTOs)
                .build();
    }

    private IngredientDTO mapToDTO(Ingredient ingredient) {
        return IngredientDTO.builder()
                .id(ingredient.getId())
                .type(ingredient.getType())
                .amount(ingredient.getAmount())
                .unit(ingredient.getUnit())
                .name(ingredient.getName())
                .build();
    }

    private RecipeStepDTO mapToDTO(RecipeStep step) {
        return RecipeStepDTO.builder()
                .id(step.getId())
                .stepNumber(step.getStepNumber())
                .instruction(step.getInstruction())
                .build();
    }
}