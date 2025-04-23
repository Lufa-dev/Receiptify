package com.thesis.receiptify.controller;

import com.thesis.receiptify.model.dto.RecipeDTO;
import com.thesis.receiptify.model.dto.RecipeSearchCriteriaDTO;
import com.thesis.receiptify.service.CollectionService;
import com.thesis.receiptify.service.FileStorageService;
import com.thesis.receiptify.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final FileStorageService fileStorageService;
    private final CollectionService collectionService;

    @PostMapping
    public ResponseEntity<RecipeDTO> createRecipe(
            @Valid @RequestBody RecipeDTO recipeDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }

        RecipeDTO createdRecipe = recipeService.createRecipe(recipeDTO, userDetails.getUsername());

        try {
            collectionService.handleNewRecipe(recipeService.getRecipeEntityById(createdRecipe.getId()), userDetails.getUsername());
        } catch (Exception e) {
            System.err.println("Failed to add recipe to collection: " + e.getMessage());
        }

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdRecipe.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdRecipe);
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String fileName = fileStorageService.storeFile(file);
            String fileUrl = fileStorageService.getDirectFileUrl(fileName);

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", fileUrl);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Could not upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDTO> getRecipeById(@PathVariable Long id) {
        RecipeDTO recipeDTO = recipeService.getRecipeById(id);
        return ResponseEntity.ok(recipeDTO);
    }

    @GetMapping
    public ResponseEntity<Page<RecipeDTO>> getAllRecipes(Pageable pageable) {
        Page<RecipeDTO> recipes = recipeService.getAllRecipes(pageable);
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/user")
    public ResponseEntity<Page<RecipeDTO>> getUserRecipes(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Page<RecipeDTO> recipes = recipeService.getUserRecipes(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<RecipeDTO>> searchRecipes(
            @RequestParam String query,
            Pageable pageable) {
        Page<RecipeDTO> recipes = recipeService.searchRecipes(query, pageable);
        return ResponseEntity.ok(recipes);
    }

    @PostMapping("/advanced-search")
    public ResponseEntity<Page<RecipeDTO>> advancedSearchRecipes(
            @RequestBody RecipeSearchCriteriaDTO criteria,
            Pageable pageable) {
        Page<RecipeDTO> recipes = recipeService.advancedSearchRecipes(criteria, pageable);
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/search-options")
    public ResponseEntity<Map<String, List<String>>> getSearchOptions() {
        Map<String, List<String>> options = recipeService.getSearchFilterOptions();
        return ResponseEntity.ok(options);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeDTO> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody RecipeDTO recipeDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        RecipeDTO updatedRecipe = recipeService.updateRecipe(id, recipeDTO, userDetails.getUsername());
        return ResponseEntity.ok(updatedRecipe);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        recipeService.deleteRecipe(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/with-seasonality")
    public ResponseEntity<RecipeDTO> getRecipeWithSeasonality(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails != null ? userDetails.getUsername() : null;

        try {
            RecipeDTO recipeDTO = recipeService.getRecipeWithSeasonality(id, username);
            return ResponseEntity.ok(recipeDTO);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get recipe: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Search for recipes by season
     */
    @GetMapping("/seasonal")
    public ResponseEntity<Page<RecipeDTO>> getSeasonalRecipes(
            @RequestParam(required = false, defaultValue = "70") int minSeasonalScore,
            Pageable pageable) {

        try {
            Page<RecipeDTO> seasonalRecipes = recipeService.findSeasonalRecipes(minSeasonalScore, pageable);
            return ResponseEntity.ok(seasonalRecipes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}