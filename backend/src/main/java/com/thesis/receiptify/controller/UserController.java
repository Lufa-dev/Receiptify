// src/main/java/com/example/receiptify/controller/UserController.java
package com.thesis.receiptify.controller;


import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.model.User;
import com.thesis.receiptify.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String registerUser(@RequestBody User user) throws ExecutionException, InterruptedException {
        return userService.saveUser(user);
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable String userId) throws ExecutionException, InterruptedException {
        return userService.getUserById(userId);
    }

    @GetMapping("/{userId}/recipes")
    public List<Recipe> getUserRecipes(@PathVariable String userId) throws ExecutionException, InterruptedException {
        return userService.getUserRecipes(userId);
    }
}
