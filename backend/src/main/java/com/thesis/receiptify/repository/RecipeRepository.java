package com.thesis.receiptify.repository;

import com.thesis.receiptify.model.Profile;
import com.thesis.receiptify.model.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long>, JpaSpecificationExecutor<Recipe> {

    List<Recipe> findByUserOrderByCreatedAtDesc(Profile user);

    Page<Recipe> findByUserOrderByCreatedAtDesc(Profile user, Pageable pageable);

    Page<Recipe> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT r FROM Recipe r WHERE " +
            "LOWER(r.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Recipe> searchRecipes(String query, Pageable pageable);

    @Query("SELECT DISTINCT r.category FROM Recipe r WHERE r.category IS NOT NULL")
    List<String> findDistinctCategories();

    @Query("SELECT DISTINCT r.cuisine FROM Recipe r WHERE r.cuisine IS NOT NULL")
    List<String> findDistinctCuisines();
}
