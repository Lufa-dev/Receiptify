package com.thesis.receiptify.service;

import com.thesis.receiptify.model.Profile;
import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.model.UserInteraction;
import com.thesis.receiptify.repository.ProfileRepository;
import com.thesis.receiptify.repository.RecipeRepository;
import com.thesis.receiptify.repository.UserInteractionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InteractionService {
    private final UserInteractionRepository interactionRepository;
    private final ProfileRepository profileRepository;
    private final RecipeRepository recipeRepository;

    /**
     * Tracks a user viewing a recipe.
     * Increments the view count if there's an existing interaction or creates a new one.
     *
     * @param recipeId The ID of the viewed recipe
     * @param username The username of the user viewing the recipe
     * @throws EntityNotFoundException if the user or recipe doesn't exist
     */
    @Transactional
    public void trackRecipeView(Long recipeId, String username) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        Optional<UserInteraction> existingInteraction =
                interactionRepository.findByUserAndRecipe(user, recipe);

        UserInteraction interaction;
        if (existingInteraction.isPresent()) {
            interaction = existingInteraction.get();
            interaction.setViewCount(interaction.getViewCount() + 1);
            interaction.setLastInteraction(LocalDateTime.now());
        } else {
            interaction = UserInteraction.builder()
                    .user(user)
                    .recipe(recipe)
                    .viewCount(1)
                    .saved(false)
                    .lastInteraction(LocalDateTime.now())
                    .build();
        }

        interactionRepository.save(interaction);
    }

    /**
     * Records a user saving or unsaving a recipe.
     * Updates the saved status if there's an existing interaction or creates a new one.
     *
     * @param recipeId The ID of the recipe
     * @param username The username of the user
     * @param saved Whether the recipe is being saved (true) or unsaved (false)
     * @throws EntityNotFoundException if the user or recipe doesn't exist
     */
    @Transactional
    public void saveRecipe(Long recipeId, String username, boolean saved) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        Optional<UserInteraction> existingInteraction =
                interactionRepository.findByUserAndRecipe(user, recipe);

        UserInteraction interaction;
        if (existingInteraction.isPresent()) {
            interaction = existingInteraction.get();
            interaction.setSaved(saved);
            interaction.setLastInteraction(LocalDateTime.now());
        } else {
            interaction = UserInteraction.builder()
                    .user(user)
                    .recipe(recipe)
                    .viewCount(1)
                    .saved(saved)
                    .lastInteraction(LocalDateTime.now())
                    .build();
        }

        interactionRepository.save(interaction);
    }
}

