package com.thesis.receiptify.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Recipe {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String userId;
    private List<String> steps;
}