package com.thesis.receiptify.service;

import com.thesis.receiptify.model.*;
import com.thesis.receiptify.model.dto.*;
import com.thesis.receiptify.model.enums.IngredientType;
import com.thesis.receiptify.model.enums.Role;
import com.thesis.receiptify.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for administration operations.
 * Provides functionality for user management, content moderation,
 * and system statistics for administrators.
 */
@Service
@RequiredArgsConstructor
public class AdminService {
    private final ProfileRepository profileRepository;
    private final RecipeRepository recipeRepository;
    private final CommentRepository commentRepository;
    private final RatingRepository ratingRepository;
    private final CollectionRepository collectionRepository;
    private final UserInteractionRepository interactionRepository;

    /**
     * Checks if a user has admin role.
     *
     * @param username The username to check
     * @return true if the user has admin role, false otherwise
     * @throws EntityNotFoundException if the user doesn't exist
     */
    @Transactional(readOnly = true)
    public boolean isUserAdmin(String username) {
        Profile profile = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return profile.getRoles() == Role.ADMIN;
    }

    /**
     * Gets dashboard statistics for admin panel.
     * Includes user counts, recipe counts, and recent activity.
     *
     * @return Map of statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // User statistics
        long totalUsers = profileRepository.count();
        stats.put("totalUsers", totalUsers);

        // Calculate new users this month
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long newUsers = profileRepository.findAll().stream()
                .filter(profile -> profile.getCreated() != null && profile.getCreated().isAfter(startOfMonth))
                .count();
        stats.put("newUsers", newUsers);

        // Recipe statistics
        long totalRecipes = recipeRepository.count();
        stats.put("totalRecipes", totalRecipes);

        // Comment statistics
        long totalComments = commentRepository.count();
        stats.put("totalComments", totalComments);

        // Recent activity - get newest recipes first
        // Use a sorted page request to get the newest recipes first
        Pageable pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Recipe> recentRecipes = recipeRepository.findAll(pageRequest).getContent();

        List<Map<String, Object>> recentRecipeData = recentRecipes.stream()
                .map(recipe -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", recipe.getId());
                    data.put("title", recipe.getTitle());
                    data.put("username", recipe.getUser().getUsername());
                    data.put("createdAt", recipe.getCreatedAt());
                    return data;
                })
                .collect(Collectors.toList());
        stats.put("recentActivity", recentRecipeData);

        return stats;
    }

    /**
     * Gets all users with pagination for admin management.
     *
     * @param pageable Pagination information
     * @return Page of user DTOs
     */
    @Transactional(readOnly = true)
    public Page<ProfileDTO> getAllUsers(Pageable pageable) {
        Page<Profile> profiles = profileRepository.findAll(pageable);
        return profiles.map(this::mapToProfileDTO);
    }

    /**
     * Gets a specific user by ID.
     *
     * @param id The user ID
     * @return The user DTO
     * @throws EntityNotFoundException if the user doesn't exist
     */
    @Transactional(readOnly = true)
    public ProfileDTO getUserById(Long id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return mapToProfileDTO(profile);
    }

    /**
     * Updates a user's profile information.
     *
     * @param id The user ID
     * @param userData Map of user data to update
     * @return The updated user DTO
     * @throws EntityNotFoundException if the user doesn't exist
     */
    @Transactional
    public ProfileDTO updateUser(Long id, Map<String, Object> userData) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Update profile fields
        if (userData.containsKey("firstName")) {
            profile.setFirstName((String) userData.get("firstName"));
        }
        if (userData.containsKey("lastName")) {
            profile.setLastName((String) userData.get("lastName"));
        }
        if (userData.containsKey("email")) {
            profile.setEmail((String) userData.get("email"));
        }

        // Update preference fields
        if (userData.containsKey("preferredCategories")) {
            @SuppressWarnings("unchecked")
            List<String> categories = (List<String>) userData.get("preferredCategories");
            profile.setPreferredCategories(new HashSet<>(categories));
        }

        if (userData.containsKey("preferredCuisines")) {
            @SuppressWarnings("unchecked")
            List<String> cuisines = (List<String>) userData.get("preferredCuisines");
            profile.setPreferredCuisines(new HashSet<>(cuisines));
        }

        if (userData.containsKey("favoriteIngredients")) {
            @SuppressWarnings("unchecked")
            List<String> favoriteIngredients = (List<String>) userData.get("favoriteIngredients");
            Set<IngredientType> ingredientTypes = favoriteIngredients.stream()
                    .map(IngredientType::valueOf)
                    .collect(Collectors.toSet());
            profile.setFavoriteIngredients(ingredientTypes);
        }

        if (userData.containsKey("dislikedIngredients")) {
            @SuppressWarnings("unchecked")
            List<String> dislikedIngredients = (List<String>) userData.get("dislikedIngredients");
            Set<IngredientType> ingredientTypes = dislikedIngredients.stream()
                    .map(IngredientType::valueOf)
                    .collect(Collectors.toSet());
            profile.setDislikedIngredients(ingredientTypes);
        }

        if (userData.containsKey("maxPrepTime")) {
            profile.setMaxPrepTime((Integer) userData.get("maxPrepTime"));
        }

        if (userData.containsKey("difficultyPreference")) {
            profile.setDifficultyPreference((String) userData.get("difficultyPreference"));
        }

        if (userData.containsKey("preferSeasonalRecipes")) {
            profile.setPreferSeasonalRecipes((Boolean) userData.get("preferSeasonalRecipes"));
        }

        // Save and return updated profile
        Profile updatedProfile = profileRepository.save(profile);
        return mapToProfileDTO(updatedProfile);
    }

    /**
     * Deletes a user and all associated data.
     *
     * @param id The user ID
     * @throws EntityNotFoundException if the user doesn't exist
     */
    @Transactional
    public void deleteUser(Long id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // First delete all comments made by this user
        commentRepository.findAll().stream()
                .filter(comment -> comment.getUser().getId().equals(profile.getId()))
                .forEach(commentRepository::delete);

        // Delete all ratings given by this user
        ratingRepository.findAll().stream()
                .filter(rating -> rating.getUser().getId().equals(profile.getId()))
                .forEach(ratingRepository::delete);

        // Delete all user's interactions
        interactionRepository.deleteAll(interactionRepository.findByUser(profile));

        // Delete all user's collections
        collectionRepository.deleteAll(collectionRepository.findByUserOrderByNameAsc(profile));

        // Delete user's recipes - this will cascade delete ingredients, steps, ratings, and comments
        recipeRepository.deleteAll(recipeRepository.findByUserOrderByCreatedAtDesc(profile));

        // Finally delete the user
        profileRepository.delete(profile);
    }

    /**
     * Updates a user's role (e.g., from USER to ADMIN).
     *
     * @param id The user ID
     * @param role The new role
     * @return The updated user DTO
     * @throws EntityNotFoundException if the user doesn't exist
     */
    @Transactional
    public ProfileDTO updateUserRole(Long id, Role role) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        profile.setRoles(role);
        Profile updatedProfile = profileRepository.save(profile);

        return mapToProfileDTO(updatedProfile);
    }

    /**
     * Searches for users by username or email.
     *
     * @param query The search query
     * @param pageable Pagination information
     * @return Page of matching user DTOs
     */
    @Transactional(readOnly = true)
    public Page<ProfileDTO> searchUsers(String query, Pageable pageable) {
        // Implement search logic based on your requirements
        // For example, search by username, email, or name
        Page<Profile> profiles = profileRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                query, query, pageable);

        return profiles.map(this::mapToProfileDTO);
    }

    /**
     * Gets all recipes with pagination for admin management.
     *
     * @param pageable Pagination information
     * @return Page of recipe DTOs
     */
    @Transactional(readOnly = true)
    public Page<RecipeDTO> getAllRecipes(Pageable pageable) {
        Page<Recipe> recipes = recipeRepository.findAll(pageable);
        return recipes.map(this::mapToRecipeDTO);
    }

    /**
     * Gets a specific recipe by ID.
     *
     * @param id The recipe ID
     * @return The recipe DTO
     * @throws EntityNotFoundException if the recipe doesn't exist
     */
    @Transactional(readOnly = true)
    public RecipeDTO getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        // Get rating information
        Double averageRating = ratingRepository.getAverageRatingByRecipeId(recipe.getId());
        Integer totalRatings = ratingRepository.countByRecipeId(recipe.getId());
        Integer totalComments = commentRepository.countByRecipeId(recipe.getId());

        // Create DTO with all recipe details
        RecipeDTO recipeDTO = RecipeDTO.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .imageUrl(recipe.getImageUrl())
                .category(recipe.getCategory())
                .cuisine(recipe.getCuisine())
                .servings(recipe.getServings())
                .difficulty(recipe.getDifficulty())
                .costRating(recipe.getCostRating())
                .prepTime(recipe.getPrepTime())
                .cookTime(recipe.getCookTime())
                .bakingTime(recipe.getBakingTime())
                .bakingTemp(recipe.getBakingTemp())
                .panSize(recipe.getPanSize())
                .bakingMethod(recipe.getBakingMethod())
                .user(UserDTO.builder()
                        .id(recipe.getUser().getId())
                        .username(recipe.getUser().getUsername())
                        .firstName(recipe.getUser().getFirstName())
                        .lastName(recipe.getUser().getLastName())
                        .build())
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalRatings(totalRatings != null ? totalRatings : 0)
                .totalComments(totalComments != null ? totalComments : 0)
                .build();

        // Add ingredients
        List<IngredientDTO> ingredientDTOs = recipe.getIngredients().stream()
                .map(this::mapToIngredientDTO)
                .collect(Collectors.toList());
        recipeDTO.setIngredients(ingredientDTOs);

        // Add steps
        List<RecipeStepDTO> stepDTOs = recipe.getSteps().stream()
                .map(this::mapToStepDTO)
                .collect(Collectors.toList());
        recipeDTO.setSteps(stepDTOs);

        return recipeDTO;
    }

    /**
     * Updates a recipe as an administrator.
     * Bypasses owner check that's present in regular recipe service.
     *
     * @param id The recipe ID
     * @param recipeDTO The updated recipe data
     * @return The updated recipe DTO
     * @throws EntityNotFoundException if the recipe doesn't exist
     */
    @Transactional
    public RecipeDTO updateRecipe(Long id, RecipeDTO recipeDTO) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        // Update recipe fields
        recipe.setTitle(recipeDTO.getTitle());
        recipe.setDescription(recipeDTO.getDescription());
        recipe.setImageUrl(recipeDTO.getImageUrl());
        recipe.setCategory(recipeDTO.getCategory());
        recipe.setCuisine(recipeDTO.getCuisine());
        recipe.setServings(recipeDTO.getServings());
        recipe.setDifficulty(recipeDTO.getDifficulty());
        recipe.setCostRating(recipeDTO.getCostRating());
        recipe.setPrepTime(recipeDTO.getPrepTime());
        recipe.setCookTime(recipeDTO.getCookTime());
        recipe.setBakingTime(recipeDTO.getBakingTime());
        recipe.setBakingTemp(recipeDTO.getBakingTemp());
        recipe.setPanSize(recipeDTO.getPanSize());
        recipe.setBakingMethod(recipeDTO.getBakingMethod());
        recipe.setUpdatedAt(LocalDateTime.now());

        // Clear and re-add ingredients
        recipe.getIngredients().clear();
        if (recipeDTO.getIngredients() != null) {
            for (IngredientDTO ingredientDTO : recipeDTO.getIngredients()) {
                Ingredient ingredient = Ingredient.builder()
                        .type(ingredientDTO.getType())
                        .amount(ingredientDTO.getAmount())
                        .unit(ingredientDTO.getUnit())
                        .name(ingredientDTO.getName() != null ? ingredientDTO.getName() : ingredientDTO.getType().getDisplayName())
                        .build();
                recipe.addIngredient(ingredient);
            }
        }

        // Clear and re-add steps
        recipe.getSteps().clear();
        if (recipeDTO.getSteps() != null) {
            for (RecipeStepDTO stepDTO : recipeDTO.getSteps()) {
                RecipeStep step = RecipeStep.builder()
                        .stepNumber(stepDTO.getStepNumber())
                        .instruction(stepDTO.getInstruction())
                        .build();
                recipe.addStep(step);
            }
        }

        Recipe updatedRecipe = recipeRepository.save(recipe);
        return getRecipeById(updatedRecipe.getId()); // Use the improved getRecipeById method
    }

    /**
     * Deletes a recipe as an administrator.
     * Bypasses owner check that's present in regular recipe service.
     *
     * @param id The recipe ID
     * @throws EntityNotFoundException if the recipe doesn't exist
     */
    @Transactional
    public void deleteRecipe(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        // Remove the recipe from all collections that contain it
        collectionRepository.findAllContainingRecipe(recipe).forEach(collection -> {
            collection.removeRecipe(recipe);
            collectionRepository.save(collection);
        });

        // Delete the recipe (which will cascade to ingredients, steps, etc.)
        recipeRepository.delete(recipe);
    }

    /**
     * Searches for recipes by title or description.
     *
     * @param query The search query
     * @param pageable Pagination information
     * @return Page of matching recipe DTOs
     */
    @Transactional(readOnly = true)
    public Page<RecipeDTO> searchRecipes(String query, Pageable pageable) {
        Page<Recipe> recipes = recipeRepository.searchRecipes(query, pageable);
        return recipes.map(this::mapToRecipeDTO);
    }

    /**
     * Gets all comments with pagination for admin management.
     *
     * @param pageable Pagination information
     * @return Page of comment DTOs
     */
    @Transactional(readOnly = true)
    public Page<CommentDTO> getAllComments(Pageable pageable) {
        Page<Comment> comments = commentRepository.findAll(pageable);
        return comments.map(this::mapToCommentDTO);
    }

    /**
     * Gets a specific comment by ID.
     *
     * @param id The comment ID
     * @return The comment DTO
     * @throws EntityNotFoundException if the comment doesn't exist
     */
    @Transactional(readOnly = true)
    public CommentDTO getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        return mapToCommentDTO(comment);
    }

    /**
     * Updates a comment as an administrator.
     * Bypasses owner check that's present in regular comment service.
     *
     * @param id The comment ID
     * @param commentDTO The updated comment data
     * @return The updated comment DTO
     * @throws EntityNotFoundException if the comment doesn't exist
     */
    @Transactional
    public CommentDTO updateComment(Long id, CommentDTO commentDTO) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        comment.setContent(commentDTO.getContent());
        comment.setUpdatedAt(LocalDateTime.now());

        comment.setModerationStatus(commentDTO.getModerationStatus());
        comment.setAdminNotes(commentDTO.getAdminNotes());

        // Additional admin-specific fields could be added

        Comment updatedComment = commentRepository.save(comment);
        return mapToCommentDTO(updatedComment);
    }

    /**
     * Deletes a comment as an administrator.
     * Bypasses owner check that's present in regular comment service.
     *
     * @param id The comment ID
     * @throws EntityNotFoundException if the comment doesn't exist
     */
    @Transactional
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        commentRepository.delete(comment);
    }

    /**
     * Maps a Profile entity to a ProfileDTO.
     *
     * @param profile The Profile entity
     * @return The corresponding ProfileDTO
     */
    private ProfileDTO mapToProfileDTO(Profile profile) {
        return ProfileDTO.builder()
                .id(profile.getId())
                .username(profile.getUsername())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .email(profile.getEmail())
                .roles(profile.getRoles() != null ? profile.getRoles().name() : "USER")
                .preferredCategories(profile.getPreferredCategories())
                .preferredCuisines(profile.getPreferredCuisines())
                .favoriteIngredients(profile.getFavoriteIngredients().stream()
                        .map(IngredientType::name)
                        .collect(Collectors.toSet()))
                .dislikedIngredients(profile.getDislikedIngredients().stream()
                        .map(IngredientType::name)
                        .collect(Collectors.toSet()))
                .maxPrepTime(profile.getMaxPrepTime())
                .difficultyPreference(profile.getDifficultyPreference())
                .preferSeasonalRecipes(profile.getPreferSeasonalRecipes())
                .build();
    }

    /**
     * Maps a Recipe entity to a RecipeDTO.
     *
     * @param recipe The Recipe entity
     * @return The corresponding RecipeDTO
     */
    private RecipeDTO mapToRecipeDTO(Recipe recipe) {
        Double averageRating = ratingRepository.getAverageRatingByRecipeId(recipe.getId());
        Integer totalRatings = ratingRepository.countByRecipeId(recipe.getId());
        Integer totalComments = commentRepository.countByRecipeId(recipe.getId());

        return RecipeDTO.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .imageUrl(recipe.getImageUrl())
                .category(recipe.getCategory())
                .cuisine(recipe.getCuisine())
                .servings(recipe.getServings())
                .difficulty(recipe.getDifficulty())
                .costRating(recipe.getCostRating())
                .prepTime(recipe.getPrepTime())
                .cookTime(recipe.getCookTime())
                .bakingTime(recipe.getBakingTime())
                .bakingTemp(recipe.getBakingTemp())
                .panSize(recipe.getPanSize())
                .bakingMethod(recipe.getBakingMethod())
                .user(UserDTO.builder()
                        .id(recipe.getUser().getId())
                        .username(recipe.getUser().getUsername())
                        .firstName(recipe.getUser().getFirstName())
                        .lastName(recipe.getUser().getLastName())
                        .build())
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalRatings(totalRatings != null ? totalRatings : 0)
                .totalComments(totalComments != null ? totalComments : 0)
                .build();
    }

    /**
     * Maps a Comment entity to a CommentDTO.
     *
     * @param comment The Comment entity
     * @return The corresponding CommentDTO
     */
    private CommentDTO mapToCommentDTO(Comment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(UserDTO.builder()
                        .id(comment.getUser().getId())
                        .username(comment.getUser().getUsername())
                        .firstName(comment.getUser().getFirstName())
                        .lastName(comment.getUser().getLastName())
                        .build())
                .recipeId(comment.getRecipe().getId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .moderationStatus(comment.getModerationStatus())
                .adminNotes(comment.getAdminNotes())
                .build();
    }

    /**
     * Maps an Ingredient entity to an IngredientDTO.
     *
     * @param ingredient The Ingredient entity
     * @return The corresponding IngredientDTO
     */
    private IngredientDTO mapToIngredientDTO(Ingredient ingredient) {
        return IngredientDTO.builder()
                .id(ingredient.getId())
                .type(ingredient.getType())
                .amount(ingredient.getAmount())
                .unit(ingredient.getUnit())
                .name(ingredient.getName())
                .build();
    }

    /**
     * Maps a RecipeStep entity to a RecipeStepDTO.
     *
     * @param step The RecipeStep entity
     * @return The corresponding RecipeStepDTO
     */
    private RecipeStepDTO mapToStepDTO(RecipeStep step) {
        return RecipeStepDTO.builder()
                .id(step.getId())
                .stepNumber(step.getStepNumber())
                .instruction(step.getInstruction())
                .build();
    }

    /**
     * Moderates a comment (approve, reject, mark as pending).
     *
     * @param id The comment ID
     * @param status The moderation status
     * @param adminNotes Notes for administrative purposes
     * @return The updated comment DTO
     * @throws EntityNotFoundException if the comment doesn't exist
     */
    @Transactional
    public CommentDTO moderateComment(Long id, String status, String adminNotes) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        comment.setModerationStatus(status);
        comment.setAdminNotes(adminNotes);
        comment.setUpdatedAt(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);
        return mapToCommentDTO(updatedComment);
    }

    /**
     * Gets statistics for a specific user including recipe, comment, and rating counts.
     *
     * @param userId The user ID
     * @return Map of user statistics
     * @throws EntityNotFoundException if the user doesn't exist
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics(Long userId) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Map<String, Object> stats = new HashMap<>();

        // Count recipes
        List<Recipe> userRecipes = recipeRepository.findByUserOrderByCreatedAtDesc(profile);
        stats.put("recipeCount", userRecipes.size());

        // Get most recent recipes (limit to 5)
        List<Map<String, Object>> recentRecipes = userRecipes.stream()
                .limit(5)
                .map(recipe -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", recipe.getId());
                    data.put("title", recipe.getTitle());
                    data.put("createdAt", recipe.getCreatedAt());
                    return data;
                })
                .collect(Collectors.toList());
        stats.put("recentRecipes", recentRecipes);

        // Count comments
        long commentCount = commentRepository.findAll().stream()
                .filter(comment -> comment.getUser().getId().equals(userId))
                .count();
        stats.put("commentCount", commentCount);

        // Count ratings
        long ratingCount = ratingRepository.findAll().stream()
                .filter(rating -> rating.getUser().getId().equals(userId))
                .count();
        stats.put("ratingCount", ratingCount);

        // Count collections
        long collectionCount = collectionRepository.findByUserOrderByNameAsc(profile).size();
        stats.put("collectionCount", collectionCount);

        // Add account creation date
        stats.put("accountCreated", profile.getCreated());

        return stats;
    }

    /**
     * Gets statistics for a specific recipe including views, ratings, and comments.
     *
     * @param recipeId The recipe ID
     * @return Map of recipe statistics
     * @throws EntityNotFoundException if the recipe doesn't exist
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getRecipeStatistics(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        Map<String, Object> stats = new HashMap<>();

        // Basic recipe info
        stats.put("id", recipe.getId());
        stats.put("title", recipe.getTitle());
        stats.put("author", recipe.getUser().getUsername());
        stats.put("createdAt", recipe.getCreatedAt());

        // Count views (from user interactions)
        long viewCount = interactionRepository.findAll().stream()
                .filter(interaction -> interaction.getRecipe().getId().equals(recipeId))
                .mapToInt(interaction -> interaction.getViewCount())
                .sum();
        stats.put("viewCount", viewCount);

        // Rating statistics
        Double avgRating = ratingRepository.getAverageRatingByRecipeId(recipeId);
        stats.put("avgRating", avgRating != null ? avgRating : 0.0);

        Integer ratingCount = ratingRepository.countByRecipeId(recipeId);
        stats.put("ratingCount", ratingCount != null ? ratingCount : 0);

        // Comment statistics
        Integer commentCount = commentRepository.countByRecipeId(recipeId);
        stats.put("commentCount", commentCount != null ? commentCount : 0);

        // Collection statistics (number of collections this recipe is in)
        long collectionCount = collectionRepository.findAllContainingRecipe(recipe).size();
        stats.put("collectionCount", collectionCount);

        return stats;
    }

    /**
     * Sets a recipe as featured or not featured.
     *
     * @param id The recipe ID
     * @param featured Whether the recipe should be featured
     * @return The updated recipe DTO
     * @throws EntityNotFoundException if the recipe doesn't exist
     */
    @Transactional
    public RecipeDTO setRecipeFeatured(Long id, boolean featured) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        recipe.setFeatured(featured);
        if (featured) {
            recipe.setFeaturedAt(LocalDateTime.now());
        } else {
            recipe.setFeaturedAt(null);
        }

        Recipe updatedRecipe = recipeRepository.save(recipe);
        return mapToRecipeDTO(updatedRecipe);
    }

    /**
     * Gets all featured recipes with pagination.
     *
     * @param pageable Pagination information
     * @return Page of featured recipe DTOs
     */
    @Transactional(readOnly = true)
    public Page<RecipeDTO> getFeaturedRecipes(Pageable pageable) {
        Page<Recipe> recipes = recipeRepository.findByFeaturedTrue(pageable);
        return recipes.map(this::mapToRecipeDTO);
    }
}