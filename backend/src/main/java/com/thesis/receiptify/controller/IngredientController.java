package com.thesis.receiptify.controller;

import com.thesis.receiptify.model.enums.IngredientType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {

    @GetMapping
    public ResponseEntity<List<IngredientType>> getAllIngredientTypes() {
        return ResponseEntity.ok(Arrays.asList(IngredientType.values()));
    }

    @GetMapping("/by-category")
    public ResponseEntity<Map<String, List<IngredientType>>> getIngredientsByCategory() {
        Map<String, List<IngredientType>> groupedIngredients = Arrays.stream(IngredientType.values())
                .collect(Collectors.groupingBy(IngredientType::getCategory));

        return ResponseEntity.ok(groupedIngredients);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = Arrays.stream(IngredientType.values())
                .map(IngredientType::getCategory)
                .distinct()
                .collect(Collectors.toList());

        return ResponseEntity.ok(categories);
    }
}
