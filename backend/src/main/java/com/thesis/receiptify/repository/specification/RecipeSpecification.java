package com.thesis.receiptify.repository.specification;

import com.thesis.receiptify.model.Ingredient;
import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.model.dto.RecipeSearchCriteriaDTO;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class RecipeSpecification implements Specification<Recipe> {

    private final RecipeSearchCriteriaDTO criteria;

    @Override
    public Predicate toPredicate(Root<Recipe> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // Text search in title and description
        if (criteria.getSearchQuery() != null && !criteria.getSearchQuery().isEmpty()) {
            String searchPattern = "%" + criteria.getSearchQuery().toLowerCase() + "%";
            Predicate titlePredicate = cb.like(cb.lower(root.get("title")), searchPattern);
            Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), searchPattern);
            predicates.add(cb.or(titlePredicate, descriptionPredicate));
        }

        // Category filter
        if (criteria.getCategory() != null && !criteria.getCategory().isEmpty()) {
            predicates.add(cb.equal(root.get("category"), criteria.getCategory()));
        }

        // Cuisine filter
        if (criteria.getCuisine() != null && !criteria.getCuisine().isEmpty()) {
            predicates.add(cb.equal(root.get("cuisine"), criteria.getCuisine()));
        }

        // Difficulty filter
        if (criteria.getDifficulty() != null && !criteria.getDifficulty().isEmpty()) {
            predicates.add(cb.equal(root.get("difficulty"), criteria.getDifficulty()));
        }

        // Cost rating filter
        if (criteria.getCostRating() != null && !criteria.getCostRating().isEmpty()) {
            predicates.add(cb.equal(root.get("costRating"), criteria.getCostRating()));
        }

        // Servings range
        if (criteria.getMinServings() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("servings"), criteria.getMinServings()));
        }
        if (criteria.getMaxServings() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("servings"), criteria.getMaxServings()));
        }

        // Time filters
        if (criteria.getMaxPrepTime() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("prepTime"), criteria.getMaxPrepTime()));
        }
        if (criteria.getMaxCookTime() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("cookTime"), criteria.getMaxCookTime()));
        }

        // Total time calculation
        if (criteria.getMaxTotalTime() != null) {
            // Calculate total time (prepTime + cookTime + bakingTime)
            predicates.add(cb.lessThanOrEqualTo(
                    cb.sum(
                            cb.sum(
                                    cb.coalesce(root.get("prepTime"), 0),
                                    cb.coalesce(root.get("cookTime"), 0)
                            ),
                            cb.coalesce(root.get("bakingTime"), 0)
                    ),
                    criteria.getMaxTotalTime()
            ));
        }

        // Ingredient filters
        if (criteria.getIncludeIngredients() != null && !criteria.getIncludeIngredients().isEmpty()) {
            Join<Recipe, Ingredient> ingredients = root.join("ingredients");
            List<Predicate> ingredientPredicates = new ArrayList<>();

            for (String ingredient : criteria.getIncludeIngredients()) {
                ingredientPredicates.add(cb.equal(ingredients.get("type"), ingredient.toUpperCase()));
            }

            predicates.add(cb.or(ingredientPredicates.toArray(new Predicate[0])));
            query.distinct(true); // Avoid duplicate results
        }

        if (criteria.getExcludeIngredients() != null && !criteria.getExcludeIngredients().isEmpty()) {
            // Create subquery to find recipe IDs that have excluded ingredients
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Recipe> subRoot = subquery.from(Recipe.class);
            Join<Recipe, Ingredient> subIngredients = subRoot.join("ingredients");

            subquery.select(subRoot.get("id"));

            // Create predicates for excluded ingredients
            List<Predicate> excludePredicates = new ArrayList<>();
            for (String ingredient : criteria.getExcludeIngredients()) {
                excludePredicates.add(cb.equal(subIngredients.get("type"), ingredient.toUpperCase()));
            }

            subquery.where(cb.or(excludePredicates.toArray(new Predicate[0])));

            // Exclude recipes that have any of the excluded ingredients
            predicates.add(cb.not(root.get("id").in(subquery)));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
