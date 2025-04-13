package com.thesis.receiptify.service;

import com.thesis.receiptify.model.Profile;
import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.repository.ProfileRepository;
import com.thesis.receiptify.repository.RecipeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final RecipeRepository recipeRepository;
    private final PasswordEncoder passwordEncoder;

    public Profile getProfileByUsername(String username) {
        return profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found with username: " + username));
    }

    @Transactional
    public Profile updateProfile(Profile profile) {
        return profileRepository.save(profile);
    }

    @Transactional
    public boolean changePassword(Profile profile, String currentPassword, String newPassword) {
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, profile.getPassword())) {
            return false;
        }

        // Update password
        profile.setPassword(passwordEncoder.encode(newPassword));
        return true;
    }

    public Map<String, Object> getUserRecipeStats(String username) {
        Profile user = getProfileByUsername(username);
        List<Recipe> userRecipes = recipeRepository.findByUserOrderByCreatedAtDesc(user);

        // Count recipes created this month
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long recipesThisMonth = userRecipes.stream()
                .filter(recipe -> recipe.getCreatedAt() != null && recipe.getCreatedAt().isAfter(startOfMonth))
                .count();

        // Find most used ingredient
        Map<String, Long> ingredientCounts = new HashMap<>();
        userRecipes.forEach(recipe ->
                recipe.getIngredients().forEach(ingredient -> {
                    String ingredientName = ingredient.getName();
                    ingredientCounts.put(ingredientName, ingredientCounts.getOrDefault(ingredientName, 0L) + 1);
                })
        );

        String topIngredient = ingredientCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", userRecipes.size());
        stats.put("thisMonth", recipesThisMonth);
        stats.put("topIngredient", topIngredient);

        return stats;
    }
}
