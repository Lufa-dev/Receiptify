package com.thesis.receiptify.service;

import com.thesis.receiptify.model.Ingredient;
import com.thesis.receiptify.model.Profile;
import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.model.UserInteraction;
import com.thesis.receiptify.model.dto.*;
import com.thesis.receiptify.model.enums.IngredientType;
import com.thesis.receiptify.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for generating personalized recipe recommendations.
 * Uses content-based, collaborative filtering, and user preference approaches
 * to create a hybrid recommendation system.
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final UserInteractionRepository interactionRepository;
    private final RecipeRepository recipeRepository;
    private final ProfileRepository profileRepository;
    private final SeasonalityService seasonalityService;
    private final RatingRepository ratingRepository;
    private final CommentRepository commentRepository;

    // Recommendation weights
    private static final double CONTENT_WEIGHT = 0.4;
    private static final double COLLABORATIVE_WEIGHT = 0.3;
    private static final double PREFERENCE_WEIGHT = 0.3;

    /**
     * Gets personalized recipe recommendations for a specific user.
     * Uses a hybrid approach combining content-based, collaborative, and preference-based recommendations.
     *
     * @param username The username of the user
     * @param limit The maximum number of recommendations to return
     * @param includePrevious Whether to include recipes the user has already interacted with
     * @return List of recommended recipes
     * @throws EntityNotFoundException if the user doesn't exist
     */
    @Transactional(readOnly = true)
    public List<RecipeDTO> getRecommendationsForUser(String username, int limit, boolean includePrevious) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Get user's interactions
        List<UserInteraction> interactions = interactionRepository.findByUser(user);
        Set<Long> interactedRecipeIds = interactions.stream()
                .map(i -> i.getRecipe().getId())
                .collect(Collectors.toSet());

        // Get recommendations by each method
        Map<Long, Double> contentScores = getContentBasedScores(user, includePrevious ? new HashSet<>() : interactedRecipeIds);
        Map<Long, Double> collaborativeScores = getCollaborativeScores(user, includePrevious ? new HashSet<>() : interactedRecipeIds);
        Map<Long, Double> preferenceScores = getPreferenceBasedScores(user, includePrevious ? new HashSet<>() : interactedRecipeIds);

        // Combine all recommendation scores with weights
        Map<Long, Double> combinedScores = new HashMap<>();

        Set<Long> allRecipeIds = new HashSet<>();
        allRecipeIds.addAll(contentScores.keySet());
        allRecipeIds.addAll(collaborativeScores.keySet());
        allRecipeIds.addAll(preferenceScores.keySet());

        for (Long recipeId : allRecipeIds) {
            double combinedScore =
                    (contentScores.getOrDefault(recipeId, 0.0) * CONTENT_WEIGHT) +
                            (collaborativeScores.getOrDefault(recipeId, 0.0) * COLLABORATIVE_WEIGHT) +
                            (preferenceScores.getOrDefault(recipeId, 0.0) * PREFERENCE_WEIGHT);

            combinedScores.put(recipeId, combinedScore);
        }

        // Get top recipes by score
        List<Long> topRecipeIds = combinedScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Recipe> recommendedRecipes = recipeRepository.findAllById(topRecipeIds);

        // Sort by score
        Map<Long, Integer> recipeIdToPosition = new HashMap<>();
        for (int i = 0; i < topRecipeIds.size(); i++) {
            recipeIdToPosition.put(topRecipeIds.get(i), i);
        }

        recommendedRecipes.sort(Comparator.comparingInt(r ->
                recipeIdToPosition.getOrDefault(r.getId(), Integer.MAX_VALUE)));

        // Convert to DTOs
        return recommendedRecipes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds recipes similar to the specified recipe based on ingredients, category, and cuisine.
     *
     * @param recipeId The ID of the recipe to find similar recipes for
     * @param limit The maximum number of similar recipes to return
     * @return List of similar recipes
     * @throws EntityNotFoundException if the recipe doesn't exist
     */
    @Transactional(readOnly = true)
    public List<RecipeDTO> getSimilarRecipes(Long recipeId, int limit) {
        Recipe targetRecipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        List<Recipe> allRecipes = recipeRepository.findAll();
        Map<Long, Double> similarityScores = new HashMap<>();

        for (Recipe recipe : allRecipes) {
            if (!recipe.getId().equals(recipeId)) {
                double similarity = calculateRecipeSimilarity(targetRecipe, recipe);
                similarityScores.put(recipe.getId(), similarity);
            }
        }

        List<Long> similarRecipeIds = similarityScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Recipe> similarRecipes = recipeRepository.findAllById(similarRecipeIds);

        // Sort by similarity score
        Map<Long, Integer> idToPosition = new HashMap<>();
        for (int i = 0; i < similarRecipeIds.size(); i++) {
            idToPosition.put(similarRecipeIds.get(i), i);
        }

        similarRecipes.sort(Comparator.comparingInt(r ->
                idToPosition.getOrDefault(r.getId(), Integer.MAX_VALUE)));

        return similarRecipes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets recipes that are currently in season based on their ingredients.
     *
     * @param limit The maximum number of seasonal recipes to return
     * @return List of seasonal recipes
     */
    @Transactional(readOnly = true)
    public List<RecipeDTO> getSeasonalRecommendations(int limit) {
        List<Recipe> allRecipes = recipeRepository.findAll();
        Map<Recipe, Integer> recipeSeasonalScores = new HashMap<>();

        for (Recipe recipe : allRecipes) {
            RecipeSeasonalityDTO seasonality = seasonalityService.analyzeRecipeSeasonality(recipe);
            recipeSeasonalScores.put(recipe, seasonality.getSeasonalScore());
        }

        return recipeSeasonalScores.entrySet().stream()
                .sorted(Map.Entry.<Recipe, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> convertToDTO(entry.getKey()))
                .collect(Collectors.toList());
    }

    /**
     * Generates content-based recommendation scores based on user's interaction history.
     * Finds recipes similar to those the user has interacted with frequently.
     *
     * @param user The user profile
     * @param interactedRecipeIds Set of recipe IDs the user has already interacted with
     * @return Map of recipe IDs to recommendation scores
     */
    private Map<Long, Double> getContentBasedScores(Profile user, Set<Long> interactedRecipeIds) {
        Map<Long, Double> scores = new HashMap<>();

        if (interactedRecipeIds.isEmpty()) {
            return scores; // No previous interactions
        }

        // Get user's favorite recipes (high view count or rated highly)
        List<UserInteraction> favoriteInteractions = interactionRepository.findByUserOrderByViewCountDesc(user);

        // Only consider top interactions
        List<UserInteraction> topInteractions = favoriteInteractions.stream()
                .limit(5)
                .collect(Collectors.toList());

        // Collect all recipes
        List<Recipe> allRecipes = recipeRepository.findAll();

        // For each of user's favorite recipes, find similar ones
        for (UserInteraction interaction : topInteractions) {
            Recipe favoriteRecipe = interaction.getRecipe();

            for (Recipe candidate : allRecipes) {
                // Skip recipes the user has already interacted with
                if (interactedRecipeIds.contains(candidate.getId())) {
                    continue;
                }

                double similarity = calculateRecipeSimilarity(favoriteRecipe, candidate);

                // Add to scores, accounting for multiple similar recipes
                scores.put(candidate.getId(),
                        Math.max(similarity, scores.getOrDefault(candidate.getId(), 0.0)));
            }
        }

        return scores;
    }

    /**
     * Generates collaborative filtering recommendation scores based on similar users.
     * Finds users with similar tastes and recommends recipes they've interacted with.
     *
     * @param user The user profile
     * @param interactedRecipeIds Set of recipe IDs the user has already interacted with
     * @return Map of recipe IDs to recommendation scores
     */
    private Map<Long, Double> getCollaborativeScores(Profile user, Set<Long> interactedRecipeIds) {
        Map<Long, Double> scores = new HashMap<>();

        // Get all users
        List<Profile> allUsers = profileRepository.findAll();

        // Get user's interactions
        List<UserInteraction> userInteractions = interactionRepository.findByUser(user);

        // Build similarity scores between users
        Map<Profile, Double> userSimilarities = new HashMap<>();

        for (Profile otherUser : allUsers) {
            if (otherUser.getId().equals(user.getId())) {
                continue; // Skip the current user
            }

            List<UserInteraction> otherUserInteractions = interactionRepository.findByUser(otherUser);

            // Calculate similarity between users
            double similarity = calculateUserSimilarity(userInteractions, otherUserInteractions);

            if (similarity > 0) {
                userSimilarities.put(otherUser, similarity);
            }
        }

        // Get top similar users
        List<Profile> similarUsers = userSimilarities.entrySet().stream()
                .sorted(Map.Entry.<Profile, Double>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // For each similar user, consider their favorite recipes
        for (Profile similarUser : similarUsers) {
            double userSimilarity = userSimilarities.get(similarUser);

            List<UserInteraction> similarUserInteractions =
                    interactionRepository.findByUserOrderByViewCountDesc(similarUser);

            for (UserInteraction interaction : similarUserInteractions) {
                Long recipeId = interaction.getRecipe().getId();

                // Skip recipes the user has already interacted with
                if (interactedRecipeIds.contains(recipeId)) {
                    continue;
                }

                // Score is weighted by user similarity and interaction strength
                double interactionStrength = interaction.getViewCount() * (interaction.getSaved() ? 2.0 : 1.0);
                double score = userSimilarity * interactionStrength;

                scores.put(recipeId, scores.getOrDefault(recipeId, 0.0) + score);
            }
        }

        // Normalize scores to 0-1 range
        if (!scores.isEmpty()) {
            double maxScore = scores.values().stream().max(Double::compare).get();
            if (maxScore > 0) {
                scores.replaceAll((k, v) -> v / maxScore);
            }
        }

        return scores;
    }

    /**
     * Generates preference-based recommendation scores based on user preferences.
     * Matches recipes to user's preferred categories, cuisines, and ingredients.
     *
     * @param user The user profile
     * @param interactedRecipeIds Set of recipe IDs the user has already interacted with
     * @return Map of recipe IDs to recommendation scores
     */
    private Map<Long, Double> getPreferenceBasedScores(Profile user, Set<Long> interactedRecipeIds) {
        Map<Long, Double> scores = new HashMap<>();

        // Get all recipes
        List<Recipe> allRecipes = recipeRepository.findAll();

        // Score each recipe based on user preferences
        for (Recipe recipe : allRecipes) {
            if (interactedRecipeIds.contains(recipe.getId())) {
                continue; // Skip recipes the user has already interacted with
            }

            double score = calculatePreferenceMatchScore(recipe, user);
            if (score > 0) {
                scores.put(recipe.getId(), score);
            }
        }

        return scores;
    }

    /**
     * Calculates similarity between two recipes based on ingredients, category, and cuisine.
     * Uses Jaccard similarity for ingredient comparison and exact matching for categories.
     *
     * @param recipe1 The first recipe
     * @param recipe2 The second recipe
     * @return A similarity score between 0.0 (no similarity) and 1.0 (identical)
     */
    private double calculateRecipeSimilarity(Recipe recipe1, Recipe recipe2) {
        // Extract ingredient types for comparison
        Set<IngredientType> ingredients1 = recipe1.getIngredients().stream()
                .map(Ingredient::getType)
                .collect(Collectors.toSet());

        Set<IngredientType> ingredients2 = recipe2.getIngredients().stream()
                .map(Ingredient::getType)
                .collect(Collectors.toSet());

        // Calculate Jaccard similarity for ingredients
        double ingredientSimilarity = calculateJaccardSimilarity(ingredients1, ingredients2);

        // Calculate other similarities
        double categorySimilarity = 0.0;
        if (recipe1.getCategory() != null && recipe2.getCategory() != null) {
            categorySimilarity = recipe1.getCategory().equals(recipe2.getCategory()) ? 1.0 : 0.0;
        }

        double cuisineSimilarity = 0.0;
        if (recipe1.getCuisine() != null && recipe2.getCuisine() != null) {
            cuisineSimilarity = recipe1.getCuisine().equals(recipe2.getCuisine()) ? 1.0 : 0.0;
        }

        // Weighted combination of similarities
        return (ingredientSimilarity * 0.6) + (categorySimilarity * 0.2) + (cuisineSimilarity * 0.2);
    }

    /**
     * Calculates similarity between two users based on their recipe interactions.
     * Uses Jaccard similarity on the sets of recipes each user has interacted with.
     *
     * @param user1Interactions Interactions of the first user
     * @param user2Interactions Interactions of the second user
     * @return A similarity score between 0.0 (no similarity) and 1.0 (identical)
     */
    private double calculateUserSimilarity(List<UserInteraction> user1Interactions, List<UserInteraction> user2Interactions) {
        // Extract recipe IDs interacted with by each user
        Set<Long> user1RecipeIds = user1Interactions.stream()
                .map(i -> i.getRecipe().getId())
                .collect(Collectors.toSet());

        Set<Long> user2RecipeIds = user2Interactions.stream()
                .map(i -> i.getRecipe().getId())
                .collect(Collectors.toSet());

        // Calculate Jaccard similarity between the sets of interacted recipes
        return calculateJaccardSimilarity(user1RecipeIds, user2RecipeIds);
    }

    /**
     * Calculates how well a recipe matches a user's preferences.
     * Considers preferred categories, cuisines, ingredients, difficulty, and prep time.
     *
     * @param recipe The recipe to evaluate
     * @param user The user profile with preferences
     * @return A score between 0.0 (no match) and 1.0 (perfect match)
     */
    private double calculatePreferenceMatchScore(Recipe recipe, Profile user) {
        double score = 0.0;

        // Check category preference
        if (recipe.getCategory() != null &&
                user.getPreferredCategories().contains(recipe.getCategory())) {
            score += 0.4;
        }

        // Check cuisine preference
        if (recipe.getCuisine() != null &&
                user.getPreferredCuisines().contains(recipe.getCuisine())) {
            score += 0.4;
        }

        // Check ingredient preferences
        Set<IngredientType> recipeIngredientTypes = recipe.getIngredients().stream()
                .map(Ingredient::getType)
                .collect(Collectors.toSet());

        // Favorite ingredients boost score
        Set<IngredientType> favoriteMatches = new HashSet<>(recipeIngredientTypes);
        favoriteMatches.retainAll(user.getFavoriteIngredients());
        score += favoriteMatches.size() * 0.2;

        // Disliked ingredients reduce score
        Set<IngredientType> dislikedMatches = new HashSet<>(recipeIngredientTypes);
        dislikedMatches.retainAll(user.getDislikedIngredients());
        score -= dislikedMatches.size() * 0.5;

        // Check difficulty preference
        if (user.getDifficultyPreference() != null && recipe.getDifficulty() != null &&
                recipe.getDifficulty().equals(user.getDifficultyPreference())) {
            score += 0.3;
        }

        // Check prep time preference
        if (user.getMaxPrepTime() != null && recipe.getPrepTime() != null &&
                recipe.getPrepTime() <= user.getMaxPrepTime()) {
            score += 0.3;
        }

        // Check seasonality preference
        if (Boolean.TRUE.equals(user.getPreferSeasonalRecipes())) {
            RecipeSeasonalityDTO seasonality = seasonalityService.analyzeRecipeSeasonality(recipe);
            double seasonalBoost = seasonality.getSeasonalScore() / 100.0 * 0.5;
            score += seasonalBoost;
        }

        return Math.max(0.0, Math.min(1.0, score));
    }

    /**
     * Calculates Jaccard similarity between two sets.
     * Jaccard similarity is defined as the size of the intersection divided by the size of the union.
     *
     * @param <T> The type of elements in the sets
     * @param set1 The first set
     * @param set2 The second set
     * @return A similarity score between 0.0 (no similarity) and 1.0 (identical)
     */
    private <T> double calculateJaccardSimilarity(Set<T> set1, Set<T> set2) {
        if (set1.isEmpty() && set2.isEmpty()) {
            return 0.0; // Both empty sets - no similarity
        }

        Set<T> union = new HashSet<>(set1);
        union.addAll(set2);

        Set<T> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        return (double) intersection.size() / union.size();
    }

    /**
     * Converts a Recipe entity to a RecipeDTO with detailed information.
     *
     * @param recipe The Recipe entity
     * @return The corresponding RecipeDTO
     */
    private RecipeDTO convertToDTO(Recipe recipe) {
        // Get rating information
        Double averageRating = ratingRepository.getAverageRatingByRecipeId(recipe.getId());
        Integer totalRatings = ratingRepository.countByRecipeId(recipe.getId());
        Integer totalComments = commentRepository.countByRecipeId(recipe.getId());

        // Map ingredients
        List<IngredientDTO> ingredientDTOs = recipe.getIngredients().stream()
                .map(ingredient -> IngredientDTO.builder()
                        .id(ingredient.getId())
                        .type(ingredient.getType())
                        .amount(ingredient.getAmount())
                        .unit(ingredient.getUnit())
                        .name(ingredient.getName())
                        .build())
                .collect(Collectors.toList());

        // Map steps
        List<RecipeStepDTO> stepDTOs = recipe.getSteps().stream()
                .map(step -> RecipeStepDTO.builder()
                        .id(step.getId())
                        .stepNumber(step.getStepNumber())
                        .instruction(step.getInstruction())
                        .build())
                .collect(Collectors.toList());

        return RecipeDTO.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .imageUrl(recipe.getImageUrl())
                .ingredients(ingredientDTOs)
                .steps(stepDTOs)
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
                .user(UserDTO.builder()
                        .id(recipe.getUser().getId())
                        .username(recipe.getUser().getUsername())
                        .firstName(recipe.getUser().getFirstName())
                        .lastName(recipe.getUser().getLastName())
                        .build())
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalRatings(totalRatings != null ? totalRatings : 0)
                .totalComments(totalComments != null ? totalComments : 0)
                .build();
    }
}