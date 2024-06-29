package com.thesis.receiptify.controller;

import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/recipes")
@CrossOrigin(origins = "http://localhost:4200")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @PostMapping("/add")
    public ResponseEntity<Recipe> addRecipe(@RequestBody Recipe recipe, @AuthenticationPrincipal UserDetails userDetails) throws ExecutionException, InterruptedException {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();  // Unauthorized
        }

        Recipe savedRecipe = recipeService.saveRecipe(recipe);
        return ResponseEntity.ok(savedRecipe);
    }

    @GetMapping("/user/{userId}")
    public List<Recipe> getRecipesByUserId(@PathVariable String userId) throws ExecutionException, InterruptedException {
        return recipeService.getRecipesByUserId(userId);
    }

    @GetMapping("/getAll")
    public List<Recipe> getAllRecipes() throws ExecutionException, InterruptedException {
        return recipeService.getAllRecipes();
    }
}
