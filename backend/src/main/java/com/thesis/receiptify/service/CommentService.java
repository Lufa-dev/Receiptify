package com.thesis.receiptify.service;

import com.thesis.receiptify.model.Comment;
import com.thesis.receiptify.model.Profile;
import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.model.dto.CommentDTO;
import com.thesis.receiptify.model.dto.UserDTO;
import com.thesis.receiptify.repository.CommentRepository;
import com.thesis.receiptify.repository.ProfileRepository;
import com.thesis.receiptify.repository.RecipeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service responsible for managing recipe comments.
 * Handles adding, updating, retrieving, and deleting comments.
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final RecipeRepository recipeRepository;
    private final ProfileRepository profileRepository;

    /**
     * Adds a new comment to a recipe.
     *
     * @param commentDTO The comment data
     * @param username The username of the commenter
     * @return The created comment DTO
     * @throws EntityNotFoundException if the user or recipe doesn't exist
     */
    @Transactional
    public CommentDTO addComment(CommentDTO commentDTO, String username) {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Recipe recipe = recipeRepository.findById(commentDTO.getRecipeId())
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        Comment comment = Comment.builder()
                .content(commentDTO.getContent())
                .user(user)
                .recipe(recipe)
                .createdAt(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);
        return mapToDTO(savedComment);
    }

    /**
     * Updates an existing comment.
     *
     * @param id The comment ID
     * @param commentDTO The updated comment data
     * @param username The username of the requesting user
     * @return The updated comment DTO
     * @throws EntityNotFoundException if the comment doesn't exist
     * @throws SecurityException if the user doesn't have permission to update the comment
     */
    @Transactional
    public CommentDTO updateComment(Long id, CommentDTO commentDTO, String username) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        // Check if user is the owner of the comment
        if (!comment.getUser().getUsername().equals(username)) {
            throw new SecurityException("You don't have permission to update this comment");
        }

        comment.setContent(commentDTO.getContent());
        comment.setUpdatedAt(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);
        return mapToDTO(updatedComment);
    }

    /**
     * Deletes a comment.
     *
     * @param id The comment ID
     * @param username The username of the requesting user
     * @throws EntityNotFoundException if the comment doesn't exist
     * @throws SecurityException if the user doesn't have permission to delete the comment
     */
    @Transactional
    public void deleteComment(Long id, String username) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        // Check if user is the owner of the comment or the recipe
        if (!comment.getUser().getUsername().equals(username) &&
                !comment.getRecipe().getUser().getUsername().equals(username)) {
            throw new SecurityException("You don't have permission to delete this comment");
        }

        commentRepository.delete(comment);
    }

    /**
     * Retrieves comments for a recipe with pagination.
     *
     * @param recipeId The recipe ID
     * @param pageable Pagination information
     * @return A page of comment DTOs
     * @throws EntityNotFoundException if the recipe doesn't exist
     */
    @Transactional(readOnly = true)
    public Page<CommentDTO> getRecipeComments(Long recipeId, Pageable pageable) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        return commentRepository.findByRecipeOrderByCreatedAtDesc(recipe, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Maps a Comment entity to a CommentDTO.
     *
     * @param comment The Comment entity
     * @return The corresponding CommentDTO
     */
    private CommentDTO mapToDTO(Comment comment) {
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
                .build();
    }
}
