package com.thesis.receiptify.controller;

import com.thesis.receiptify.model.dto.RecipeDTO;
import com.thesis.receiptify.service.InteractionService;
import com.thesis.receiptify.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService recommendationService;
    private final InteractionService interactionService;

    @GetMapping("/for-user")
    public ResponseEntity<List<RecipeDTO>> getRecommendationsForUser(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<RecipeDTO> recommendations =
                recommendationService.getRecommendationsForUser(userDetails.getUsername(), limit);

        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/similar/{recipeId}")
    public ResponseEntity<List<RecipeDTO>> getSimilarRecipes(
            @PathVariable Long recipeId,
            @RequestParam(defaultValue = "6") int limit) {

        List<RecipeDTO> similarRecipes = recommendationService.getSimilarRecipes(recipeId, limit);
        return ResponseEntity.ok(similarRecipes);
    }

    @GetMapping("/seasonal")
    public ResponseEntity<List<RecipeDTO>> getSeasonalRecommendations(
            @RequestParam(defaultValue = "10") int limit) {

        List<RecipeDTO> seasonalRecipes = recommendationService.getSeasonalRecommendations(limit);
        return ResponseEntity.ok(seasonalRecipes);
    }

    @PostMapping("/track-view/{recipeId}")
    public ResponseEntity<?> trackRecipeView(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        interactionService.trackRecipeView(recipeId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/save/{recipeId}")
    public ResponseEntity<?> saveRecipe(
            @PathVariable Long recipeId,
            @RequestParam boolean saved,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        interactionService.saveRecipe(recipeId, userDetails.getUsername(), saved);
        return ResponseEntity.ok().build();
    }
}