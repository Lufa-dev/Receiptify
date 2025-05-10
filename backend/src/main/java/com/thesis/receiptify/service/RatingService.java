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

/**
 * Service responsible for managing recipe ratings.
 * Handles adding, retrieving, and summarizing ratings.
 */
@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final RecipeRepository recipeRepository;
    private final ProfileRepository profileRepository;
    private final CommentRepository commentRepository;

    /**
     * Rates a recipe or updates an existing rating.
     *
     * @param ratingDTO The rating data
     * @param username The username of the rater
     * @return The created or updated rating DTO
     * @throws EntityNotFoundException if the user or recipe doesn't exist
     * @throws IllegalStateException if the user tries to rate their own recipe
     */
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

    /**
     * Retrieves a user's rating for a specific recipe.
     *
     * @param recipeId The recipe ID
     * @param username The username of the user
     * @return The user's rating DTO, or null if the user hasn't rated the recipe
     * @throws EntityNotFoundException if the user or recipe doesn't exist
     */
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

    /**
     * Retrieves rating and comment summary for a recipe.
     *
     * @param recipeId The recipe ID
     * @return Summary DTO with average rating, total ratings, and total comments
     * @throws EntityNotFoundException if the recipe doesn't exist
     */
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

    /**
     * Maps a Rating entity to a RatingDTO.
     *
     * @param rating The Rating entity
     * @return The corresponding RatingDTO
     */
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
