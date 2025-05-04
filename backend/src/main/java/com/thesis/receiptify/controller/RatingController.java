package com.thesis.receiptify.controller;

import com.thesis.receiptify.model.dto.RatingDTO;
import com.thesis.receiptify.model.dto.RecipeRatingSummaryDTO;
import com.thesis.receiptify.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<?> rateRecipe(
            @Valid @RequestBody RatingDTO ratingDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            RatingDTO savedRating = ratingService.rateRecipe(ratingDTO, userDetails.getUsername());
            return ResponseEntity.ok(savedRating);
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to rate recipe: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<RecipeRatingSummaryDTO> getRecipeRatingSummary(@PathVariable Long recipeId) {
        try {
            RecipeRatingSummaryDTO summary = ratingService.getRecipeRatingSummary(recipeId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recipe/{recipeId}/user")
    public ResponseEntity<?> getUserRatingForRecipe(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            RatingDTO userRating = ratingService.getUserRatingForRecipe(recipeId, userDetails.getUsername());
            return ResponseEntity.ok(userRating);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get user rating: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
