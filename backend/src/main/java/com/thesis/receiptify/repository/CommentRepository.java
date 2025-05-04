package com.thesis.receiptify.repository;

import com.thesis.receiptify.model.Comment;
import com.thesis.receiptify.model.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByRecipeOrderByCreatedAtDesc(Recipe recipe, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.recipe.id = ?1")
    Integer countByRecipeId(Long recipeId);
}
