package com.thesis.receiptify.service;

import com.thesis.receiptify.model.Collection;
import com.thesis.receiptify.model.Profile;
import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.model.dto.CollectionDTO;
import com.thesis.receiptify.model.dto.UserDTO;
import com.thesis.receiptify.repository.CollectionRepository;
import com.thesis.receiptify.repository.ProfileRepository;
import com.thesis.receiptify.repository.RecipeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final ProfileRepository profileRepository;
    private final RecipeRepository recipeRepository;

    @Transactional
    public void initializeDefaultCollections(Profile user) {
        // Create "Favorites" collection
        createDefaultCollection(user, "Favorites", "Your favorite recipes");

        // Create "My Recipes" collection (automatically populated when user creates recipes)
        createDefaultCollection(user, "My Recipes", "Recipes created by you");
    }

    private Collection createDefaultCollection(Profile user, String name, String description) {
        // Check if collection already exists
        return collectionRepository.findDefaultCollectionByUserAndName(user, name)
                .orElseGet(() -> {
                    Collection collection = Collection.builder()
                            .name(name)
                            .description(description)
                            .user(user)
                            .isDefault(true)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return collectionRepository.save(collection);
                });
    }

    @Transactional(readOnly = true)
    public List<CollectionDTO> getUserCollections(String username) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return collectionRepository.findByUserOrderByNameAsc(user).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CollectionDTO getCollectionById(Long id, String username) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Collection collection = collectionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Collection not found"));

        return mapToDTO(collection);
    }

    @Transactional
    public CollectionDTO createCollection(CollectionDTO collectionDTO, String username) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Check if collection with same name already exists
        if (collectionRepository.existsByNameAndUser(collectionDTO.getName(), user)) {
            throw new IllegalArgumentException("Collection with this name already exists");
        }

        Collection collection = Collection.builder()
                .name(collectionDTO.getName())
                .description(collectionDTO.getDescription())
                .user(user)
                .isDefault(false)  // User-created collections are not default
                .recipes(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .build();

        Collection savedCollection = collectionRepository.save(collection);
        return mapToDTO(savedCollection);
    }

    @Transactional
    public CollectionDTO updateCollection(Long id, CollectionDTO collectionDTO, String username) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Collection collection = collectionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Collection not found"));

        // Don't allow modifying default collections
        if (collection.isDefault()) {
            throw new IllegalStateException("Default collections cannot be modified");
        }

        collection.setName(collectionDTO.getName());
        collection.setDescription(collectionDTO.getDescription());
        collection.setUpdatedAt(LocalDateTime.now());

        Collection updatedCollection = collectionRepository.save(collection);
        return mapToDTO(updatedCollection);
    }

    @Transactional
    public void deleteCollection(Long id, String username) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Collection collection = collectionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Collection not found"));

        // Don't allow deleting default collections
        if (collection.isDefault()) {
            throw new IllegalStateException("Default collections cannot be deleted");
        }

        collectionRepository.delete(collection);
    }

    @Transactional
    public CollectionDTO addRecipeToCollection(Long collectionId, Long recipeId, String username) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Collection collection = collectionRepository.findByIdAndUser(collectionId, user)
                .orElseThrow(() -> new EntityNotFoundException("Collection not found"));

        if (collection.isDefault() && "My Recipes".equalsIgnoreCase(collection.getName())) {
            throw new IllegalStateException("Cannot manually add recipes to the 'My Recipes' collection");
        }

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        collection.addRecipe(recipe);
        Collection updatedCollection = collectionRepository.save(collection);
        return mapToDTO(updatedCollection);
    }


    @Transactional
    public CollectionDTO removeRecipeFromCollection(Long collectionId, Long recipeId, String username) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Collection collection = collectionRepository.findByIdAndUser(collectionId, user)
                .orElseThrow(() -> new EntityNotFoundException("Collection not found"));

        if (collection.isDefault() && "My Recipes".equals(collection.getName())) {
            throw new IllegalStateException("Recipes cannot be removed from the default 'My Recipes' collection");
        }

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        collection.removeRecipe(recipe);
        Collection updatedCollection = collectionRepository.save(collection);
        return mapToDTO(updatedCollection);
    }

    @Transactional
    public void handleNewRecipe(Recipe recipe, String username) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Find "My Recipes" collection
        Collection myRecipesCollection = collectionRepository.findDefaultCollectionByUserAndName(user, "My Recipes")
                .orElseGet(() -> createDefaultCollection(user, "My Recipes", "Recipes created by you"));

        // Add the recipe to "My Recipes" collection
        myRecipesCollection.addRecipe(recipe);
        collectionRepository.save(myRecipesCollection);
    }

    // Helper method to map entity to DTO
    private CollectionDTO mapToDTO(Collection collection) {
        if (collection == null) {
            return null;
        }

        Set<Long> recipeIds = new HashSet<>();
        if (collection.getRecipes() != null) {
            recipeIds = collection.getRecipes().stream()
                    .map(Recipe::getId)
                    .collect(Collectors.toSet());
        }

        return CollectionDTO.builder()
                .id(collection.getId())
                .name(collection.getName())
                .description(collection.getDescription())
                .isDefault(collection.isDefault())
                .user(UserDTO.builder()
                        .id(collection.getUser().getId())
                        .username(collection.getUser().getUsername())
                        .firstName(collection.getUser().getFirstName())
                        .lastName(collection.getUser().getLastName())
                        .build())
                .recipeIds(recipeIds)
                .recipeCount(recipeIds.size())
                .build();
    }
}
