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

