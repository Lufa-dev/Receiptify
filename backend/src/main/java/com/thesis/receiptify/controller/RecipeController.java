package com.thesis.receiptify.controller;

import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @PostMapping
    public String createRecipe(@RequestBody Recipe recipe) throws ExecutionException, InterruptedException {
        return recipeService.saveRecipe(recipe);
    }

    @GetMapping("/user/{userId}")
    public List<Recipe> getRecipesByUserId(@PathVariable String userId) throws ExecutionException, InterruptedException {
        return recipeService.getRecipesByUserId(userId);
    }
}
