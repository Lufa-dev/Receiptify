package com.thesis.receiptify.repository;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.thesis.receiptify.model.User;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

@Repository
public class UserRepository {

    public String saveUser(User user) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("users").document(user.getId());
        ApiFuture<WriteResult> result = docRef.set(user);
        return result.get().getUpdateTime().toString();
    }

    // Add methods for fetching and updating user data if needed
}
