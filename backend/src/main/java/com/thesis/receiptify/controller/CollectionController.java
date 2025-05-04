package com.thesis.receiptify.controller;

import com.thesis.receiptify.model.dto.CollectionDTO;
import com.thesis.receiptify.service.CollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @GetMapping
    public ResponseEntity<List<CollectionDTO>> getUserCollections(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Boolean filterMyRecipes)  {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<CollectionDTO> collections = collectionService.getUserCollections(userDetails.getUsername());

        if (Boolean.TRUE.equals(filterMyRecipes)) {
            collections = collections.stream()
                    .filter(c -> !("My Recipes".equalsIgnoreCase(c.getName())))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(collections);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionDTO> getCollectionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            CollectionDTO collection = collectionService.getCollectionById(id, userDetails.getUsername());
            return ResponseEntity.ok(collection);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createCollection(
            @Valid @RequestBody CollectionDTO collectionDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            CollectionDTO createdCollection = collectionService.createCollection(collectionDTO, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCollection);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create collection: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCollection(
            @PathVariable Long id,
            @Valid @RequestBody CollectionDTO collectionDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            CollectionDTO updatedCollection = collectionService.updateCollection(id, collectionDTO, userDetails.getUsername());
            return ResponseEntity.ok(updatedCollection);
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update collection: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCollection(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            collectionService.deleteCollection(id, userDetails.getUsername());
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete collection: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{collectionId}/recipes/{recipeId}")
    public ResponseEntity<?> addRecipeToCollection(
            @PathVariable Long collectionId,
            @PathVariable Long recipeId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            CollectionDTO updatedCollection = collectionService.addRecipeToCollection(
                    collectionId, recipeId, userDetails.getUsername());
            return ResponseEntity.ok(updatedCollection);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to add recipe to collection: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{collectionId}/recipes/{recipeId}")
    public ResponseEntity<?> removeRecipeFromCollection(
            @PathVariable Long collectionId,
            @PathVariable Long recipeId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            CollectionDTO updatedCollection = collectionService.removeRecipeFromCollection(
                    collectionId, recipeId, userDetails.getUsername());
            return ResponseEntity.ok(updatedCollection);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to remove recipe from collection: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

