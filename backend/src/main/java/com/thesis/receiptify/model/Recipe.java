package com.thesis.receiptify.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(indexes = {
        @Index(name = "idx_recipe_title", columnList = "title"),
        @Index(name = "idx_recipe_category", columnList = "category"),
        @Index(name = "idx_recipe_cuisine", columnList = "cuisine"),
        @Index(name = "idx_recipe_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Profile user;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ingredient> ingredients = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepNumber ASC")
    private List<RecipeStep> steps = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String category;
    private String cuisine;
    private Integer servings;
    private String difficulty;
    private String costRating;

    private Integer prepTime;
    private Integer cookTime;
    private Integer bakingTime;
    private Integer bakingTemp;
    private Integer panSize;
    private String bakingMethod;

    private Boolean featured = false;
    private LocalDateTime featuredAt;
    private String adminNotes;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods to manage relationships
    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
        ingredient.setRecipe(this);
    }

    public void removeIngredient(Ingredient ingredient) {
        ingredients.remove(ingredient);
        ingredient.setRecipe(null);
    }

    public void addStep(RecipeStep step) {
        steps.add(step);
        step.setRecipe(this);
    }

    public void removeStep(RecipeStep step) {
        steps.remove(step);
        step.setRecipe(null);
    }

    public void addRating(Rating rating) {
        ratings.add(rating);
        rating.setRecipe(this);
    }

    public void removeRating(Rating rating) {
        ratings.remove(rating);
        rating.setRecipe(null);
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setRecipe(this);
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setRecipe(null);
    }
}