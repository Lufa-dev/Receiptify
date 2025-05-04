package com.thesis.receiptify.repository;

import com.thesis.receiptify.model.Rating;
import com.thesis.receiptify.model.Profile;
import com.thesis.receiptify.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserAndRecipe(Profile user, Recipe recipe);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.recipe.id = ?1")
    Integer countByRecipeId(Long recipeId);

    @Query("SELECT AVG(r.stars) FROM Rating r WHERE r.recipe.id = ?1")
    Double getAverageRatingByRecipeId(Long recipeId);
}
