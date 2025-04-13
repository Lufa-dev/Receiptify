package com.thesis.receiptify.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thesis.receiptify.model.enums.IngredientType;
import com.thesis.receiptify.model.enums.UnitType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IngredientType type;

    private String amount;

    @Enumerated(EnumType.STRING)
    private UnitType unit;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    @JsonIgnore
    private Recipe recipe;

    public void setType(IngredientType type) {
        this.type = type;
        if (type != null) {
            this.name = type.getDisplayName();
        }
    }

    public String getUnitSymbol() {
        return unit != null ? unit.getSymbol() : null;
    }
}
