package com.thesis.receiptify.service;

import com.thesis.receiptify.model.Profile;
import com.thesis.receiptify.model.Rating;
import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.model.dto.RatingDTO;
import com.thesis.receiptify.model.dto.RecipeRatingSummaryDTO;
import com.thesis.receiptify.model.dto.UserDTO;
import com.thesis.receiptify.repository.ProfileRepository;
import com.thesis.receiptify.repository.RatingRepository;
import com.thesis.receiptify.repository.RecipeRepository;
import com.thesis.receiptify.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final RecipeRepository recipeRepository;
    private final ProfileRepository profileRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public RatingDTO rateRecipe(RatingDTO ratingDTO, String username) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Recipe recipe = recipeRepository.findById(ratingDTO.getRecipeId())
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        // Check if user is the owner of the recipe
        if (recipe.getUser().getUsername().equals(username)) {
            throw new IllegalStateException("You cannot rate your own recipe");
        }

        // Check if user has already rated this recipe
        Optional<Rating> existingRating = ratingRepository.findByUserAndRecipe(user, recipe);

        Rating rating;
        if (existingRating.isPresent()) {
            // Update existing rating
            rating = existingRating.get();
            rating.setStars(ratingDTO.getStars());
            rating.setUpdatedAt(LocalDateTime.now());
        } else {
            // Create new rating
            rating = Rating.builder()
                    .stars(ratingDTO.getStars())
                    .user(user)
                    .recipe(recipe)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        Rating savedRating = ratingRepository.save(rating);
        return mapToDTO(savedRating);
    }

    @Transactional(readOnly = true)
    public RatingDTO getUserRatingForRecipe(Long recipeId, String username) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        Rating rating = ratingRepository.findByUserAndRecipe(user, recipe)
                .orElse(null);

        return rating != null ? mapToDTO(rating) : null;
    }

    @Transactional(readOnly = true)
    public RecipeRatingSummaryDTO getRecipeRatingSummary(Long recipeId) {
        // Check if recipe exists
        if (!recipeRepository.existsById(recipeId)) {
            throw new EntityNotFoundException("Recipe not found");
        }

        Double averageRating = ratingRepository.getAverageRatingByRecipeId(recipeId);
        Integer totalRatings = ratingRepository.countByRecipeId(recipeId);
        Integer totalComments = commentRepository.countByRecipeId(recipeId);

        return RecipeRatingSummaryDTO.builder()
                .recipeId(recipeId)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalRatings(totalRatings != null ? totalRatings : 0)
                .totalComments(totalComments != null ? totalComments : 0)
                .build();
    }

    private RatingDTO mapToDTO(Rating rating) {
        return RatingDTO.builder()
                .id(rating.getId())
                .stars(rating.getStars())
                .user(UserDTO.builder()
                        .id(rating.getUser().getId())
                        .username(rating.getUser().getUsername())
                        .firstName(rating.getUser().getFirstName())
                        .lastName(rating.getUser().getLastName())
                        .build())
                .recipeId(rating.getRecipe().getId())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }
}
