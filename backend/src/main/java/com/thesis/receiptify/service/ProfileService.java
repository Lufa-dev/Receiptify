package com.thesis.receiptify.service;

import com.thesis.receiptify.model.Profile;
import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.model.dto.ProfileDTO;
import com.thesis.receiptify.model.enums.IngredientType;
import com.thesis.receiptify.repository.ProfileRepository;
import com.thesis.receiptify.repository.RecipeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for managing user profile operations.
 * Handles retrieving, updating user profiles and preferences.
 */
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final RecipeRepository recipeRepository;

    private static final Set<String> COMMON_STAPLES = Set.of(
            "SALT", "PEPPER", "WATER", "OIL", "OLIVE_OIL", "BUTTER",
            "GARLIC", "ONIONS", "FLOUR", "SUGAR"
    );

    /**
     * Retrieves a user profile by username.
     *
     * @param username The username to look up
     * @return The user profile
     * @throws EntityNotFoundException if the profile doesn't exist
     */
    @Transactional(readOnly = true)
    public Profile getProfileByUsername(String username) {
        return profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for username: " + username));
    }

    /**
     * Retrieves a user profile DTO by username.
     *
     * @param username The username to look up
     * @return The user profile DTO
     * @throws EntityNotFoundException if the profile doesn't exist
     */
    @Transactional(readOnly = true)
    public ProfileDTO getUserProfileDTO(String username) {
        Profile profile = getProfileByUsername(username);
        return mapToDTO(profile);
    }

    /**
     * Updates profile data for a user.
     *
     * @param username The username of the profile to update
     * @param profileData Map of profile fields to update
     * @return The updated profile DTO
     * @throws EntityNotFoundException if the profile doesn't exist
     */
    @Transactional
    public ProfileDTO updateProfileData(String username, Map<String, String> profileData) {
        Profile profile = getProfileByUsername(username);

        // Update profile fields if provided
        if (profileData.containsKey("firstName")) {
            profile.setFirstName(profileData.get("firstName"));
        }
        if (profileData.containsKey("lastName")) {
            profile.setLastName(profileData.get("lastName"));
        }
        if (profileData.containsKey("email")) {
            profile.setEmail(profileData.get("email"));
        }

        Profile updatedProfile = profileRepository.save(profile);
        return mapToDTO(updatedProfile);
    }

    /**
     * Changes a user's password after verifying the current password.
     *
     * @param username The username of the user
     * @param currentPassword The current password for verification
     * @param newPassword The new password to set
     * @return true if password was changed successfully, false if current password is incorrect
     * @throws EntityNotFoundException if the profile doesn't exist
     */
    @Transactional
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        Profile profile = getProfileByUsername(username);
        return changePassword(profile, currentPassword, newPassword);
    }

    /**
     * Changes a user's password after verifying the current password.
     *
     * @param profile The user profile
     * @param currentPassword The current password for verification
     * @param newPassword The new password to set
     * @return true if password was changed successfully, false if current password is incorrect
     */
    @Transactional
    public boolean changePassword(Profile profile, String currentPassword, String newPassword) {
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, profile.getPassword())) {
            return false;
        }

        // Update password
        profile.setPassword(passwordEncoder.encode(newPassword));
        profileRepository.save(profile);
        return true;
    }

    /**
     * Retrieves statistics about a user's recipes.
     *
     * @param username The username to get statistics for
     * @return Map of statistics including total recipes, recipes created this month, top ingredient
     * @throws EntityNotFoundException if the profile doesn't exist
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserRecipeStats(String username) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Map<String, Object> stats = new HashMap<>();
        Map<String, Long> ingredientCounts = new HashMap<>();

        // Get all user's recipes
        List<Recipe> userRecipes = recipeRepository.findByUserOrderByCreatedAtDesc(user);

        // Count total recipes
        int totalRecipes = userRecipes.size();
        stats.put("total", totalRecipes);

        // Count recipes created this month
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long thisMonthRecipes = userRecipes.stream()
                .filter(recipe -> recipe.getCreatedAt() != null && recipe.getCreatedAt().isAfter(startOfMonth))
                .count();
        stats.put("thisMonth", thisMonthRecipes);

        // Find most used ingredient (top ingredient)
        if (!userRecipes.isEmpty()) {

            userRecipes.forEach(recipe -> {
                recipe.getIngredients().forEach(ingredient -> {
                    String ingredientName = ingredient.getName();
                    IngredientType type = ingredient.getType();

                    // Skip common staples
                    if (type != null && !COMMON_STAPLES.contains(type.name())) {
                        ingredientCounts.put(ingredientName,
                                ingredientCounts.getOrDefault(ingredientName, 0L) + 1);
                    }
                });
            });


            String topIngredient = ingredientCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("");

            stats.put("topIngredient", topIngredient);
        } else {
            stats.put("topIngredient", "");
        }

        return stats;
    }

    /**
     * Updates user preferences.
     *
     * @param username The username of the profile to update
     * @param preferences Map of preference fields to update
     * @return The updated profile DTO
     * @throws EntityNotFoundException if the profile doesn't exist
     */
    @Transactional
    public ProfileDTO updateUserPreferences(String username, Map<String, Object> preferences) {
        Profile profile = getProfileByUsername(username);

        // Update preference fields
        if (preferences.containsKey("preferredCategories")) {
            @SuppressWarnings("unchecked")
            List<String> categories = (List<String>) preferences.get("preferredCategories");
            profile.setPreferredCategories(new HashSet<>(categories));
        }

        if (preferences.containsKey("preferredCuisines")) {
            @SuppressWarnings("unchecked")
            List<String> cuisines = (List<String>) preferences.get("preferredCuisines");
            profile.setPreferredCuisines(new HashSet<>(cuisines));
        }

        if (preferences.containsKey("favoriteIngredients")) {
            @SuppressWarnings("unchecked")
            List<String> favoriteIngredients = (List<String>) preferences.get("favoriteIngredients");
            Set<IngredientType> ingredientTypes = favoriteIngredients.stream()
                    .map(IngredientType::valueOf)
                    .collect(Collectors.toSet());
            profile.setFavoriteIngredients(ingredientTypes);
        }

        if (preferences.containsKey("dislikedIngredients")) {
            @SuppressWarnings("unchecked")
            List<String> dislikedIngredients = (List<String>) preferences.get("dislikedIngredients");
            Set<IngredientType> ingredientTypes = dislikedIngredients.stream()
                    .map(IngredientType::valueOf)
                    .collect(Collectors.toSet());
            profile.setDislikedIngredients(ingredientTypes);
        }

        if (preferences.containsKey("maxPrepTime")) {
            profile.setMaxPrepTime((Integer) preferences.get("maxPrepTime"));
        }

        if (preferences.containsKey("difficultyPreference")) {
            profile.setDifficultyPreference((String) preferences.get("difficultyPreference"));
        }

        if (preferences.containsKey("preferSeasonalRecipes")) {
            profile.setPreferSeasonalRecipes((Boolean) preferences.get("preferSeasonalRecipes"));
        }

        Profile updatedProfile = profileRepository.save(profile);
        return mapToDTO(updatedProfile);
    }

    /**
     * Retrieves user preferences.
     *
     * @param username The username to get preferences for
     * @return Map of user preferences
     * @throws EntityNotFoundException if the profile doesn't exist
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserPreferences(String username) {
        Profile profile = getProfileByUsername(username);

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("preferredCategories", profile.getPreferredCategories());
        preferences.put("preferredCuisines", profile.getPreferredCuisines());
        preferences.put("favoriteIngredients", profile.getFavoriteIngredients());
        preferences.put("dislikedIngredients", profile.getDislikedIngredients());
        preferences.put("maxPrepTime", profile.getMaxPrepTime());
        preferences.put("difficultyPreference", profile.getDifficultyPreference());
        preferences.put("preferSeasonalRecipes", profile.getPreferSeasonalRecipes());

        return preferences;
    }

    /**
     * Maps a Profile entity to a ProfileDTO.
     *
     * @param profile The Profile entity
     * @return The corresponding ProfileDTO
     */
    private ProfileDTO mapToDTO(Profile profile) {
        return ProfileDTO.builder()
                .id(profile.getId())
                .username(profile.getUsername())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .email(profile.getEmail())
                .preferredCategories(profile.getPreferredCategories())
                .preferredCuisines(profile.getPreferredCuisines())
                .favoriteIngredients(profile.getFavoriteIngredients().stream()
                        .map(IngredientType::name)
                        .collect(Collectors.toSet()))
                .dislikedIngredients(profile.getDislikedIngredients().stream()
                        .map(IngredientType::name)
                        .collect(Collectors.toSet()))
                .maxPrepTime(profile.getMaxPrepTime())
                .difficultyPreference(profile.getDifficultyPreference())
                .preferSeasonalRecipes(profile.getPreferSeasonalRecipes())
                .build();
    }
}

