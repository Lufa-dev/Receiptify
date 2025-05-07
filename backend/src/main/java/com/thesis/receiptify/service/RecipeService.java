package com.thesis.receiptify.service;

import com.thesis.receiptify.model.*;
import com.thesis.receiptify.model.Collection;
import com.thesis.receiptify.model.dto.*;
import com.thesis.receiptify.repository.*;
import com.thesis.receiptify.repository.specification.RecipeSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final ProfileRepository profileRepository;
    private final CollectionService collectionService;
    private final CollectionRepository collectionRepository;
    private final RatingRepository ratingRepository;
    private final CommentRepository commentRepository;
    private final SeasonalityService seasonalityService;

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
                .category(recipeDTO.getCategory())
                .cuisine(recipeDTO.getCuisine())
                .servings(recipeDTO.getServings())
                .difficulty(recipeDTO.getDifficulty())
                .costRating(recipeDTO.getCostRating())
                .prepTime(recipeDTO.getPrepTime())
                .cookTime(recipeDTO.getCookTime())
                .bakingTime(recipeDTO.getBakingTime())
                .bakingTemp(recipeDTO.getBakingTemp())
                .panSize(recipeDTO.getPanSize())
                .bakingMethod(recipeDTO.getBakingMethod())
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

        collectionService.handleNewRecipe(savedRecipe, username);

        return mapToDTO(savedRecipe, null);
    }

    @Transactional(readOnly = true)
    public RecipeDTO getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));
        return mapToDTO(recipe, null);
    }

    @Transactional(readOnly = true)
    public RecipeDTO getRecipeById(Long id, String username) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        // If username is provided, get the user's rating for this recipe
        Integer userRating = null;
        if (username != null) {
            Profile user = profileRepository.findByUsername(username)
                    .orElse(null);
            if (user != null) {
                Optional<Rating> rating = ratingRepository.findByUserAndRecipe(user, recipe);
                if (rating.isPresent()) {
                    userRating = rating.get().getStars();
                }
            }
        }

        return mapToDTO(recipe, userRating);
    }

    @Transactional(readOnly = true)
    public Page<RecipeDTO> getAllRecipes(Pageable pageable) {
        return recipeRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(recipe -> mapToDTO(recipe, null));
    }

    @Transactional(readOnly = true)
    public Page<RecipeDTO> getUserRecipes(String username, Pageable pageable) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return recipeRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(recipe -> mapToDTO(recipe, null));
    }

    @Transactional(readOnly = true)
    public Page<RecipeDTO> searchRecipes(String query, Pageable pageable) {
        return recipeRepository.searchRecipes(query, pageable)
                .map(recipe -> mapToDTO(recipe, null));
    }

    @Transactional(readOnly = true)
    public Page<RecipeDTO> advancedSearchRecipes(RecipeSearchCriteriaDTO criteria, Pageable pageable) {
        RecipeSpecification specification = new RecipeSpecification(criteria);
        return recipeRepository.findAll(specification, pageable)
                .map(recipe -> mapToDTO(recipe, null));
    }

    @Transactional(readOnly = true)
    public Map<String, List<String>> getSearchFilterOptions() {
        Map<String, List<String>> options = new HashMap<>();

        // Get available categories and cuisines
        options.put("categories", recipeRepository.findDistinctCategories());
        options.put("cuisines", recipeRepository.findDistinctCuisines());

        // Add other static options
        options.put("difficulties", Arrays.asList("easy", "medium", "hard"));
        options.put("costRatings", Arrays.asList("budget", "moderate", "expensive"));

        return options;
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

        recipe.setCategory(recipeDTO.getCategory());
        recipe.setCuisine(recipeDTO.getCuisine());
        recipe.setServings(recipeDTO.getServings());
        recipe.setDifficulty(recipeDTO.getDifficulty());
        recipe.setCostRating(recipeDTO.getCostRating());
        recipe.setPrepTime(recipeDTO.getPrepTime());
        recipe.setCookTime(recipeDTO.getCookTime());
        recipe.setBakingTime(recipeDTO.getBakingTime());
        recipe.setBakingTemp(recipeDTO.getBakingTemp());
        recipe.setPanSize(recipeDTO.getPanSize());
        recipe.setBakingMethod(recipeDTO.getBakingMethod());

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
        return mapToDTO(updatedRecipe, null);
    }

    @Transactional
    public void deleteRecipe(Long id, String username) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        // Check if the user is the owner of the recipe
        if (!recipe.getUser().getUsername().equals(username)) {
            throw new SecurityException("You don't have permission to delete this recipe");
        }

        List<Collection> collections = collectionRepository.findAllContainingRecipe(recipe);

        // Remove the recipe from all collections
        for (Collection collection : collections) {
            collection.removeRecipe(recipe);
            collectionRepository.save(collection);
        }

        recipeRepository.delete(recipe);
    }

    @Transactional(readOnly = true)
    public Recipe getRecipeEntityById(Long id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));
    }

    @Transactional(readOnly = true)
    public RecipeDTO getRecipeWithSeasonality(Long id, String username) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        // Get standard recipe DTO
        RecipeDTO recipeDTO = getRecipeById(id, username);

        // Add seasonality information
        RecipeSeasonalityDTO seasonalityDTO = seasonalityService.analyzeRecipeSeasonality(recipe);
        recipeDTO.setSeasonalityInfo(seasonalityDTO);

        return recipeDTO;
    }

    @Transactional(readOnly = true)
    public Page<RecipeDTO> findSeasonalRecipes(int minSeasonalScore, Pageable pageable) {
        // Get all recipes
        List<Recipe> allRecipes = recipeRepository.findAll();

        // Process them to include seasonality information and filter by score
        List<RecipeDTO> seasonalRecipes = allRecipes.stream()
                .map(recipe -> {
                    RecipeDTO dto = mapToDTO(recipe, null);
                    RecipeSeasonalityDTO seasonalityDTO = seasonalityService.analyzeRecipeSeasonality(recipe);
                    dto.setSeasonalityInfo(seasonalityDTO);
                    return dto;
                })
                .filter(dto -> dto.getSeasonalityInfo().getSeasonalScore() >= minSeasonalScore)
                // Sort by seasonality score in descending order
                .sorted(Comparator.comparing(dto -> dto.getSeasonalityInfo().getSeasonalScore(), Comparator.reverseOrder()))
                .collect(Collectors.toList());

        // Create a new page with the filtered and sorted content
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), seasonalRecipes.size());

        return new PageImpl<>(
                start >= seasonalRecipes.size() ? List.of() : seasonalRecipes.subList(start, end),
                pageable,
                seasonalRecipes.size()
        );
    }

    @Transactional(readOnly = true)
    public Page<RecipeDTO> getFeaturedRecipes(Pageable pageable) {
        return recipeRepository.findByFeaturedTrueOrderByFeaturedAtDesc(pageable)
                .map(recipe -> mapToDTO(recipe, null));
    }

    // Helper methods to map between entities and DTOs
    private RecipeDTO mapToDTO(Recipe recipe, Integer userRating) {
        List<IngredientDTO> ingredientDTOs = recipe.getIngredients().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        List<RecipeStepDTO> stepDTOs = recipe.getSteps().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        // Get rating information
        Double averageRating = ratingRepository.getAverageRatingByRecipeId(recipe.getId());
        Integer totalRatings = ratingRepository.countByRecipeId(recipe.getId());
        Integer totalComments = commentRepository.countByRecipeId(recipe.getId());

        return RecipeDTO.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .imageUrl(recipe.getImageUrl())
                .ingredients(ingredientDTOs)
                .steps(stepDTOs)
                .user(UserDTO.builder()
                        .id(recipe.getUser().getId())
                        .username(recipe.getUser().getUsername())
                        .firstName(recipe.getUser().getFirstName())
                        .lastName(recipe.getUser().getLastName())
                        .build())
                .category(recipe.getCategory())
                .cuisine(recipe.getCuisine())
                .servings(recipe.getServings())
                .difficulty(recipe.getDifficulty())
                .costRating(recipe.getCostRating())
                .prepTime(recipe.getPrepTime())
                .cookTime(recipe.getCookTime())
                .bakingTime(recipe.getBakingTime())
                .bakingTemp(recipe.getBakingTemp())
                .panSize(recipe.getPanSize())
                .bakingMethod(recipe.getBakingMethod())
                .dietaryTags(recipe.getDietaryTags())
                // Add rating information
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalRatings(totalRatings != null ? totalRatings : 0)
                .totalComments(totalComments != null ? totalComments : 0)
                .userRating(userRating)
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