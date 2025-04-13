package com.thesis.receiptify.controller;

import com.thesis.receiptify.model.enums.UnitType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/units")
public class UnitController {

    @GetMapping
    public ResponseEntity<List<UnitType>> getAllUnitTypes() {
        return ResponseEntity.ok(Arrays.asList(UnitType.values()));
    }

    @GetMapping("/by-category")
    public ResponseEntity<Map<String, List<UnitType>>> getUnitsByCategory() {
        Map<String, List<UnitType>> groupedUnits = Arrays.stream(UnitType.values())
                .collect(Collectors.groupingBy(UnitType::getCategory));

        return ResponseEntity.ok(groupedUnits);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = Arrays.stream(UnitType.values())
                .map(UnitType::getCategory)
                .distinct()
                .collect(Collectors.toList());

        return ResponseEntity.ok(categories);
    }
}