package com.thesis.receiptify.model.enums;

import lombok.Getter;

@Getter
public enum UnitType {
    // Volume units (metric)
    MILLILITER("ml", "Volume"),
    CENTILITER("cl", "Volume"),
    DECILITER("dl", "Volume"),
    LITER("l", "Volume"),

    // Weight units (metric)
    MILLIGRAM("mg", "Weight"),
    GRAM("g", "Weight"),
    KILOGRAM("kg", "Weight"),

    // Length units (metric)
    MILLIMETER("mm", "Length"),
    CENTIMETER("cm", "Length"),

    // Temperature units
    CELSIUS("°C", "Temperature"),

    // Count/Quantity
    PIECE("pc", "Count"),
    SLICE("slice", "Count"),
    PINCH("pinch", "Count"),
    HANDFUL("handful", "Count"),

    // Spoon measurements
    TEASPOON("tsp", "Spoon"),
    TABLESPOON("tbsp", "Spoon"),

    // Cup measurements
    CUP("cup", "Cup"),

    // Other
    TO_TASTE("to taste", "Other"),
    AS_NEEDED("as needed", "Other");

    private final String symbol;
    private final String category;

    UnitType(String symbol, String category) {
        this.symbol = symbol;
        this.category = category;
    }

    public static UnitType fromSymbol(String symbol) {
        for (UnitType type : values()) {
            if (type.getSymbol().equalsIgnoreCase(symbol)) {
                return type;
            }
        }
        return null;
    }
}