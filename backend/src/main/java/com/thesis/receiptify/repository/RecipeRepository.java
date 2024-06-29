package com.thesis.receiptify.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;
import com.thesis.receiptify.model.Recipe;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class RecipeRepository {

    public Recipe saveRecipe(Recipe recipe) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> future = db.collection("recipes").document(recipe.getId()).set(recipe);
        future.get(); // Wait for the write to complete
        return recipe;
    }

    public List<Recipe> getRecipesByUserId(String userId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> querySnapshot = db.collection("recipes").whereEqualTo("userId", userId).get();
        return querySnapshot.get().getDocuments().stream()
                .map(document -> document.toObject(Recipe.class))
                .collect(Collectors.toList());
    }

    public List<Recipe> getAllRecipes() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection("recipes").get();
        List<Recipe> recipes = future.get().getDocuments().stream()
                .map(document -> document.toObject(Recipe.class))
                .collect(Collectors.toList());
        return recipes;
    }
}
