package com.thesis.receiptify.service;

import com.thesis.receiptify.model.Comment;
import com.thesis.receiptify.model.Profile;
import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.model.dto.CommentDTO;
import com.thesis.receiptify.model.dto.ProfileDTO;
import com.thesis.receiptify.model.dto.RecipeDTO;
import com.thesis.receiptify.model.dto.UserDTO;
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
     * Check if a user has admin role
     */
    @Transactional(readOnly = true)
    public boolean isUserAdmin(String username) {
        Profile profile = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return profile.getRoles() == Role.ADMIN;
    }

    /**
     * Get dashboard statistics for admin panel
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
     * Get all users for admin management
     */
    @Transactional(readOnly = true)
    public Page<ProfileDTO> getAllUsers(Pageable pageable) {
        Page<Profile> profiles = profileRepository.findAll(pageable);
        return profiles.map(this::mapToProfileDTO);
    }

    /**
     * Get a specific user by ID
     */
    @Transactional(readOnly = true)
    public ProfileDTO getUserById(Long id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return mapToProfileDTO(profile);
    }

    /**
     * Update a user's profile information
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
     * Delete a user
     */
    @Transactional
    public void deleteUser(Long id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

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
     * Update a user's role
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
     * Search for users
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
     * Get all recipes for admin management
     */
    @Transactional(readOnly = true)
    public Page<RecipeDTO> getAllRecipes(Pageable pageable) {
        Page<Recipe> recipes = recipeRepository.findAll(pageable);
        return recipes.map(this::mapToRecipeDTO);
    }

    /**
     * Get a specific recipe by ID
     */
    @Transactional(readOnly = true)
    public RecipeDTO getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        return mapToRecipeDTO(recipe);
    }

    /**
     * Update a recipe
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

        // Ingredients and steps would be handled separately or use a service method

        Recipe updatedRecipe = recipeRepository.save(recipe);
        return mapToRecipeDTO(updatedRecipe);
    }

    /**
     * Delete a recipe
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
     * Search for recipes
     */
    @Transactional(readOnly = true)
    public Page<RecipeDTO> searchRecipes(String query, Pageable pageable) {
        // You can implement more sophisticated search logic
        Page<Recipe> recipes = recipeRepository.searchRecipes(query, pageable);
        return recipes.map(this::mapToRecipeDTO);
    }

    /**
     * Get all comments for admin management
     */
    @Transactional(readOnly = true)
    public Page<CommentDTO> getAllComments(Pageable pageable) {
        Page<Comment> comments = commentRepository.findAll(pageable);
        return comments.map(this::mapToCommentDTO);
    }

    /**
     * Get a specific comment by ID
     */
    @Transactional(readOnly = true)
    public CommentDTO getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        return mapToCommentDTO(comment);
    }

    /**
     * Update a comment
     */
    @Transactional
    public CommentDTO updateComment(Long id, CommentDTO commentDTO) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        comment.setContent(commentDTO.getContent());
        comment.setUpdatedAt(LocalDateTime.now());

        // Additional admin-specific fields could be added

        Comment updatedComment = commentRepository.save(comment);
        return mapToCommentDTO(updatedComment);
    }

    /**
     * Delete a comment
     */
    @Transactional
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        commentRepository.delete(comment);
    }

    // Helper methods for mapping entities to DTOs
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

    private RecipeDTO mapToRecipeDTO(Recipe recipe) {
        // This could be more complex with ingredients and steps
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
                .build();
    }

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
     * Moderate a comment (approve, reject, mark as pending)
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
     * Get user statistics including count of recipes, comments, ratings
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
     * Get detailed statistics for a specific recipe
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
     * Set a recipe as featured or not featured
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
     * Get all featured recipes
     */
    @Transactional(readOnly = true)
    public Page<RecipeDTO> getFeaturedRecipes(Pageable pageable) {
        Page<Recipe> recipes = recipeRepository.findByFeaturedTrue(pageable);
        return recipes.map(this::mapToRecipeDTO);
    }
}