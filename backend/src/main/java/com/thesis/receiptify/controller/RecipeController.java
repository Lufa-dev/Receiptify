package com.thesis.receiptify.controller;

import com.thesis.receiptify.model.dto.RecipeDTO;
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
import java.util.Map;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<RecipeDTO> createRecipe(
            @Valid @RequestBody RecipeDTO recipeDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }

        RecipeDTO createdRecipe = recipeService.createRecipe(recipeDTO, userDetails.getUsername());

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
}