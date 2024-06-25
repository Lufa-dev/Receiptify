package com.thesis.receiptify.service;

import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    public String saveRecipe(Recipe recipe) throws ExecutionException, InterruptedException {
        recipe.setId(UUID.randomUUID().toString()); // Generate a unique ID for the recipe
        return recipeRepository.saveRecipe(recipe);
    }

    public List<Recipe> getRecipesByUserId(String userId) throws ExecutionException, InterruptedException {
        return recipeRepository.getRecipesByUserId(userId);
    }
}