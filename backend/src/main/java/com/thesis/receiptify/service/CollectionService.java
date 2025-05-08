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

/**
 * Service responsible for managing recipe collections.
 * Handles creating, updating, retrieving, and deleting collections,
 * as well as managing recipes within collections.
 */
@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final ProfileRepository profileRepository;
    private final RecipeRepository recipeRepository;

    /**
     * Initializes default collections for a new user.
     * Creates "Favorites" and "My Recipes" collections automatically.
     *
     * @param user The user profile to create collections for
     */
    @Transactional
    public void initializeDefaultCollections(Profile user) {
        // Create "Favorites" collection
        createDefaultCollection(user, "Favorites", "Your favorite recipes");

        // Create "My Recipes" collection (automatically populated when user creates recipes)
        createDefaultCollection(user, "My Recipes", "Recipes created by you");
    }

    /**
     * Creates a default collection with the specified name and description.
     * If the collection already exists, returns the existing one.
     *
     * @param user The owner of the collection
     * @param name The collection name
     * @param description The collection description
     * @return The created or existing collection
     */
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

    /**
     * Retrieves all collections for a specific user.
     *
     * @param username The username of the collection owner
     * @return List of collection DTOs
     * @throws EntityNotFoundException if the user doesn't exist
     */
    @Transactional(readOnly = true)
    public List<CollectionDTO> getUserCollections(String username) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return collectionRepository.findByUserOrderByNameAsc(user).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific collection by ID for a user.
     *
     * @param id The collection ID
     * @param username The username of the requesting user
     * @return The collection DTO
     * @throws EntityNotFoundException if the collection or user doesn't exist
     */
    @Transactional(readOnly = true)
    public CollectionDTO getCollectionById(Long id, String username) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Collection collection = collectionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Collection not found"));

        return mapToDTO(collection);
    }

    /**
     * Creates a new collection for a user.
     *
     * @param collectionDTO The collection data
     * @param username The username of the collection owner
     * @return The created collection DTO
     * @throws EntityNotFoundException if the user doesn't exist
     * @throws IllegalArgumentException if collection with the same name already exists
     */
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

    /**
     * Updates an existing collection.
     *
     * @param id The collection ID
     * @param collectionDTO The updated collection data
     * @param username The username of the requesting user
     * @return The updated collection DTO
     * @throws EntityNotFoundException if the collection or user doesn't exist
     * @throws IllegalStateException if user tries to modify a default collection
     */
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

    /**
     * Deletes a collection.
     *
     * @param id The collection ID
     * @param username The username of the requesting user
     * @throws EntityNotFoundException if the collection or user doesn't exist
     * @throws IllegalStateException if user tries to delete a default collection
     */
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

    /**
     * Adds a recipe to a collection.
     *
     * @param collectionId The collection ID
     * @param recipeId The recipe ID
     * @param username The username of the requesting user
     * @return The updated collection DTO
     * @throws EntityNotFoundException if the collection, recipe, or user doesn't exist
     * @throws IllegalStateException if user tries to manually add to "My Recipes" collection
     */
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

    /**
     * Removes a recipe from a collection.
     *
     * @param collectionId The collection ID
     * @param recipeId The recipe ID
     * @param username The username of the requesting user
     * @return The updated collection DTO
     * @throws EntityNotFoundException if the collection, recipe, or user doesn't exist
     * @throws IllegalStateException if user tries to remove from "My Recipes" collection
     */
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

    /**
     * Handles adding a new recipe to the user's "My Recipes" collection.
     * Automatically called when a user creates a new recipe.
     *
     * @param recipe The newly created recipe
     * @param username The username of the recipe creator
     * @throws EntityNotFoundException if the user doesn't exist
     */
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

    /**
     * Maps a Collection entity to a CollectionDTO.
     *
     * @param collection The Collection entity
     * @return The corresponding CollectionDTO
     */
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
