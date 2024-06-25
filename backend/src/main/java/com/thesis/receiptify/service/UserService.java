package com.thesis.receiptify.service;


import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.model.User;
import com.thesis.receiptify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public String saveUser(User user) throws ExecutionException, InterruptedException {
        user.setId(UUID.randomUUID().toString());  // Generate a unique ID for the user
        return userRepository.saveUser(user);
    }

    public User getUserById(String userId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        return db.collection("users").document(userId).get().get().toObject(User.class);
    }

    public List<Recipe> getUserRecipes(String userId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        User user = getUserById(userId);
        return user.getRecipeIds().stream()
                .map(recipeId -> {
                    try {
                        return db.collection("recipes").document(recipeId).get().get().toObject(Recipe.class);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }
}
