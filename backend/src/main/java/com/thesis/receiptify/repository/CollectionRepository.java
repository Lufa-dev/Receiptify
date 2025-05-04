package com.thesis.receiptify.repository;

import com.thesis.receiptify.model.Collection;
import com.thesis.receiptify.model.Profile;
import com.thesis.receiptify.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findByUserOrderByNameAsc(Profile user);

    @Query("SELECT c FROM Collection c WHERE c.user = ?1 AND c.isDefault = true AND c.name = ?2")
    Optional<Collection> findDefaultCollectionByUserAndName(Profile user, String name);

    Optional<Collection> findByIdAndUser(Long id, Profile user);

    boolean existsByNameAndUser(String name, Profile user);

    @Query("SELECT c FROM Collection c JOIN c.recipes r WHERE r = ?1")
    List<Collection> findAllContainingRecipe(Recipe recipe);
}
