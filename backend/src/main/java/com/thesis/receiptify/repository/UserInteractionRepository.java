package com.thesis.receiptify.repository;

import com.thesis.receiptify.model.Profile;
import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.model.UserInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {
    List<UserInteraction> findByUser(Profile user);
    List<UserInteraction> findByUserOrderByViewCountDesc(Profile user);
    Optional<UserInteraction> findByUserAndRecipe(Profile user, Recipe recipe);
}
