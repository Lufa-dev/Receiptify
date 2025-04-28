package com.thesis.receiptify.model;


import com.thesis.receiptify.model.enums.IngredientType;
import com.thesis.receiptify.model.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    @Column(unique = true)
    private String username;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;

    private String password;

    @CreationTimestamp
    private LocalDateTime created;

    @Enumerated(EnumType.STRING)
    private Role roles;

    @ElementCollection
    private Set<String> preferredCategories = new HashSet<>();

    @ElementCollection
    private Set<String> preferredCuisines = new HashSet<>();

    @ElementCollection
    private Set<IngredientType> favoriteIngredients = new HashSet<>();

    @ElementCollection
    private Set<IngredientType> dislikedIngredients = new HashSet<>();

    private Integer maxPrepTime;

    private String difficultyPreference;

    private Boolean preferSeasonalRecipes = false;
}
