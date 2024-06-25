package com.thesis.receiptify.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePicture;
    private List<String> recipeIds;
}