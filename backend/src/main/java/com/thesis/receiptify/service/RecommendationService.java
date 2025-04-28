package com.thesis.receiptify.service;

import com.thesis.receiptify.model.Ingredient;
import com.thesis.receiptify.model.Profile;
import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.model.UserInteraction;
import com.thesis.receiptify.model.dto.RecipeDTO;
import com.thesis.receiptify.model.dto.RecipeSeasonalityDTO;
import com.thesis.receiptify.model.enums.IngredientType;
import com.thesis.receiptify.repository.ProfileRepository;
import com.thesis.receiptify.repository.RecipeRepository;
import com.thesis.receiptify.repository.UserInteractionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final UserInteractionRepository interactionRepository;
    private final RecipeRepository recipeRepository;
    private final ProfileRepository profileRepository;
    private final SeasonalityService seasonalityService;

    // Recommendation weights
    private static final double CONTENT_WEIGHT = 0.4;
    private static final double COLLABORATIVE_WEIGHT = 0.3;
    private static final double PREFERENCE_WEIGHT = 0.3;

    @Transactional(readOnly = true)
    public List<RecipeDTO> getRecommendationsForUser(String username, int limit) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Get user's interactions
        List<UserInteraction> interactions = interactionRepository.findByUser(user);
        Set<Long> interactedRecipeIds = interactions.stream()
                .map(i -> i.getRecipe().getId())
                .collect(Collectors.toSet());

        // Get recommendations by each method
        Map<Long, Double> contentScores = getContentBasedScores(user, interactedRecipeIds);
        Map<Long, Double> collaborativeScores = getCollaborativeScores(user, interactedRecipeIds);
        Map<Long, Double> preferenceScores = getPreferenceBasedScores(user, interactedRecipeIds);

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

    private RecipeDTO convertToDTO(Recipe recipe) {
        // Implement the mapping from Recipe entity to RecipeDTO
        // (You should already have this method in your RecipeService)
        // This is just a placeholder - replace with your actual implementation
        return RecipeDTO.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .imageUrl(recipe.getImageUrl())
                // Map other fields
                .build();
    }
}