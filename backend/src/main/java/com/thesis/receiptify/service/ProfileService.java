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

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Profile getProfileByUsername(String username) {
        return profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for username: " + username));
    }

    @Transactional(readOnly = true)
    public ProfileDTO getUserProfileDTO(String username) {
        Profile profile = getProfileByUsername(username);
        return mapToDTO(profile);
    }

    @Transactional
    public Profile updateProfile(Profile profile) {
        return profileRepository.save(profile);
    }

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

    @Transactional
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        Profile profile = getProfileByUsername(username);
        return changePassword(profile, currentPassword, newPassword);
    }

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

    @Transactional(readOnly = true)
    public Map<String, Object> getUserRecipeStats(String username) {
        // Implementation depends on your specific requirements
        // This is a placeholder for your existing implementation
        Map<String, Object> stats = new HashMap<>();
        // Populate stats based on user's recipes, interactions, etc.
        return stats;
    }

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

    // Utility method to map Profile entity to ProfileDTO
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

