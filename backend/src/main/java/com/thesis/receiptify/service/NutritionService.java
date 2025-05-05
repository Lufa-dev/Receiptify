package com.thesis.receiptify.service;

import com.thesis.receiptify.model.Ingredient;
import com.thesis.receiptify.model.Recipe;
import com.thesis.receiptify.model.dto.NutritionDTO;
import com.thesis.receiptify.model.enums.IngredientType;
import com.thesis.receiptify.model.enums.UnitType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NutritionService {

    /**
     * Calculates nutrition information for a recipe based on its ingredients
     * @param recipe The recipe to analyze
     * @return NutritionDTO containing the calculated nutritional values
     */
    public NutritionDTO calculateNutrition(Recipe recipe) {
        if (recipe == null || recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            return createEmptyNutritionDTO();
        }

        double totalCalories = 0;
        double totalProtein = 0;
        double totalFat = 0;
        double totalCarbs = 0;
        double totalFiber = 0;
        double totalSugar = 0;
        double totalSodium = 0;

        // Process each ingredient
        for (Ingredient ingredient : recipe.getIngredients()) {
            IngredientType type = ingredient.getType();
            if (type == null) continue;

            // Extract amount and convert to grams
            double grams = extractGramsFromAmount(ingredient.getAmount(), ingredient.getUnit());

            // If we couldn't determine the weight, use a default portion
            if (grams <= 0) {
                grams = getDefaultPortionSize(type);
            }

            // Scale nutrients by the weight in grams (nutritional data is per 100g)
            double scaleFactor = grams / 100.0;

            totalCalories += type.getCalories() * scaleFactor;
            totalProtein += type.getProtein() * scaleFactor;
            totalFat += type.getFat() * scaleFactor;
            totalCarbs += type.getCarbs() * scaleFactor;
            totalFiber += type.getFiber() * scaleFactor;
            totalSugar += type.getSugar() * scaleFactor;
            totalSodium += type.getSodium() * scaleFactor;
        }

        // Calculate nutrition per serving if servings are specified
        int servings = Optional.ofNullable(recipe.getServings()).orElse(1);

        return NutritionDTO.builder()
                .calories((int) Math.round(totalCalories / servings))
                .protein(Math.round(totalProtein * 10.0 / servings) / 10.0)
                .fat(Math.round(totalFat * 10.0 / servings) / 10.0)
                .carbs(Math.round(totalCarbs * 10.0 / servings) / 10.0)
                .fiber(Math.round(totalFiber * 10.0 / servings) / 10.0)
                .sugar(Math.round(totalSugar * 10.0 / servings) / 10.0)
                .sodium((int) Math.round(totalSodium / servings))
                .servings(servings)
                .build();
    }

    /**
     * Calculate the normalized macronutrient distribution ensuring total is exactly 100%
     * @param nutrition The nutrition information
     * @return Map with protein, fat, and carbs percentages that sum to 100%
     */
    public Map<String, Integer> calculateNormalizedMacroDistribution(NutritionDTO nutrition) {
        if (nutrition == null) {
            return Map.of("protein", 0, "fat", 0, "carbs", 0);
        }

        double proteinCal = nutrition.getCaloriesFromProtein();
        double fatCal = nutrition.getCaloriesFromFat();
        double carbsCal = nutrition.getCaloriesFromCarbs();
        double totalCal = proteinCal + fatCal + carbsCal;

        if (totalCal == 0) {
            return Map.of("protein", 0, "fat", 0, "carbs", 0);
        }

        // Calculate exact percentages
        int proteinPct = (int) Math.round((proteinCal / totalCal) * 100);
        int fatPct = (int) Math.round((fatCal / totalCal) * 100);
        int carbsPct = (int) Math.round((carbsCal / totalCal) * 100);

        // Adjust to ensure sum is 100%
        int sum = proteinPct + fatPct + carbsPct;
        if (sum != 100) {
            // Determine which value to adjust based on which has the largest fractional part
            double proteinFrac = (proteinCal / totalCal) * 100 - Math.floor((proteinCal / totalCal) * 100);
            double fatFrac = (fatCal / totalCal) * 100 - Math.floor((fatCal / totalCal) * 100);
            double carbsFrac = (carbsCal / totalCal) * 100 - Math.floor((carbsCal / totalCal) * 100);

            if (sum > 100) {
                // Need to subtract
                if (proteinFrac <= fatFrac && proteinFrac <= carbsFrac) {
                    proteinPct -= (sum - 100);
                } else if (fatFrac <= proteinFrac && fatFrac <= carbsFrac) {
                    fatPct -= (sum - 100);
                } else {
                    carbsPct -= (sum - 100);
                }
            } else {
                // Need to add
                if (proteinFrac >= fatFrac && proteinFrac >= carbsFrac) {
                    proteinPct += (100 - sum);
                } else if (fatFrac >= proteinFrac && fatFrac >= carbsFrac) {
                    fatPct += (100 - sum);
                } else {
                    carbsPct += (100 - sum);
                }
            }
        }

        return Map.of("protein", proteinPct, "fat", fatPct, "carbs", carbsPct);
    }

    /**
     * Creates an empty nutrition DTO with zeros for all values
     */
    private NutritionDTO createEmptyNutritionDTO() {
        return NutritionDTO.builder()
                .calories(0)
                .protein(0.0)
                .fat(0.0)
                .carbs(0.0)
                .fiber(0.0)
                .sugar(0.0)
                .sodium(0)
                .servings(1)
                .build();
    }

    /**
     * Extracts the weight in grams from the amount string and unit
     */
    private double extractGramsFromAmount(String amount, UnitType unitType) {
        if (amount == null || amount.isEmpty()) {
            return 0;
        }

        try {
            // Handle fractions like "1/2" or mixed numbers like "1 1/2"
            double numericAmount = parseAmount(amount);

            String unit = unitType != null ? unitType.getSymbol() : null;

            // If no unit or already in grams, return the amount
            if (unit == null || unit.isEmpty() || unit.equalsIgnoreCase("g") || unit.equalsIgnoreCase("gram") || unit.equalsIgnoreCase("grams")) {
                return numericAmount;
            }

            // Convert common units to grams
            Map<String, Double> unitConversions = getUnitConversions();
            String normalizedUnit = unit.toLowerCase().trim();

            if (unitConversions.containsKey(normalizedUnit)) {
                return numericAmount * unitConversions.get(normalizedUnit);
            }

            // For volume-based measurements, use density approximations
            Map<String, Double> volumeToDensity = getVolumeToDensityMap();

            for (Map.Entry<String, Double> entry : volumeToDensity.entrySet()) {
                if (normalizedUnit.contains(entry.getKey())) {
                    return numericAmount * entry.getValue();
                }
            }

            // If we can't convert, return 0
            return 0;

        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Parses an amount string into a numeric value, handling fractions
     */
    private double parseAmount(String amount) {
        // Clean up the input string
        amount = amount.trim();

        // Pattern for mixed numbers like "1 1/2"
        Pattern mixedPattern = Pattern.compile("(\\d+)\\s+(\\d+)/(\\d+)");
        Matcher mixedMatcher = mixedPattern.matcher(amount);

        if (mixedMatcher.matches()) {
            int whole = Integer.parseInt(mixedMatcher.group(1));
            int numerator = Integer.parseInt(mixedMatcher.group(2));
            int denominator = Integer.parseInt(mixedMatcher.group(3));
            return whole + (double) numerator / denominator;
        }

        // Pattern for fractions like "1/2"
        Pattern fractionPattern = Pattern.compile("(\\d+)/(\\d+)");
        Matcher fractionMatcher = fractionPattern.matcher(amount);

        if (fractionMatcher.matches()) {
            int numerator = Integer.parseInt(fractionMatcher.group(1));
            int denominator = Integer.parseInt(fractionMatcher.group(2));
            return (double) numerator / denominator;
        }

        // Try to parse as a simple number
        try {
            return Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            // Handle any remaining non-numeric characters
            amount = amount.replaceAll("[^\\d.]", "");
            if (!amount.isEmpty()) {
                return Double.parseDouble(amount);
            }
            return 0;
        }
    }

    /**
     * Returns a map of unit conversions to grams
     */
    private Map<String, Double> getUnitConversions() {
        Map<String, Double> conversions = new HashMap<>();

        // Weight conversions
        conversions.put("kg", 1000.0);
        conversions.put("kilogram", 1000.0);
        conversions.put("kilograms", 1000.0);
        conversions.put("g", 1.0);
        conversions.put("gram", 1.0);
        conversions.put("grams", 1.0);
        conversions.put("mg", 0.001);
        conversions.put("milligram", 0.001);
        conversions.put("milligrams", 0.001);
        conversions.put("oz", 28.35);
        conversions.put("ounce", 28.35);
        conversions.put("ounces", 28.35);
        conversions.put("lb", 453.592);
        conversions.put("pound", 453.592);
        conversions.put("pounds", 453.592);

        // Volume to weight conversions for common ingredients
        conversions.put("tsp", 5.0);
        conversions.put("teaspoon", 5.0);
        conversions.put("teaspoons", 5.0);
        conversions.put("tbsp", 15.0);
        conversions.put("tablespoon", 15.0);
        conversions.put("tablespoons", 15.0);
        conversions.put("cup", 240.0);
        conversions.put("cups", 240.0);
        conversions.put("ml", 1.0);
        conversions.put("milliliter", 1.0);
        conversions.put("milliliters", 1.0);
        conversions.put("l", 1000.0);
        conversions.put("liter", 1000.0);
        conversions.put("liters", 1000.0);
        conversions.put("pt", 473.176);
        conversions.put("pint", 473.176);
        conversions.put("pints", 473.176);
        conversions.put("qt", 946.353);
        conversions.put("quart", 946.353);
        conversions.put("quarts", 946.353);
        conversions.put("gal", 3785.41);
        conversions.put("gallon", 3785.41);
        conversions.put("gallons", 3785.41);

        // Count-based conversions (approximations)
        conversions.put("slice", 30.0);
        conversions.put("slices", 30.0);
        conversions.put("piece", 30.0);
        conversions.put("pieces", 30.0);
        conversions.put("pinch", 0.5);
        conversions.put("pinches", 0.5);
        conversions.put("dash", 0.5);
        conversions.put("dashes", 0.5);
        conversions.put("clove", 5.0);  // for garlic
        conversions.put("cloves", 5.0);

        conversions.put("to taste", 0.5); // Minimal amount
        conversions.put("as needed", 0.5); // Minimal amount

        return conversions;
    }

    /**
     * Returns a map of volume terms to density multipliers
     * These are rough approximations for converting volume to weight
     */
    private Map<String, Double> getVolumeToDensityMap() {
        Map<String, Double> densities = new HashMap<>();

        // Density multipliers for converting volume measurements to grams
        // These are approximate and vary by ingredient
        densities.put("cup", 240.0);
        densities.put("tbsp", 15.0);
        densities.put("tsp", 5.0);
        densities.put("ml", 1.0);
        densities.put("l", 1000.0);

        return densities;
    }

    /**
     * Returns default portion sizes for different ingredient types
     */
    private double getDefaultPortionSize(IngredientType type) {
        String category = type.getCategory();

        // Default portion sizes in grams by category
        if (category.equals("Vegetables")) return 100.0;
        if (category.equals("Fruits")) return 100.0;
        if (category.equals("Proteins")) return 85.0;
        if (category.equals("Dairy & Eggs")) return 30.0;
        if (category.equals("Grains & Starches")) return 50.0;
        if (category.equals("Herbs & Spices")) return 5.0;
        if (category.equals("Oils, Vinegars & Condiments")) return 15.0;
        if (category.equals("Nuts, Seeds & Dried Fruits")) return 30.0;
        if (category.equals("Sweeteners & Baking")) return 10.0;
        if (category.equals("Beverages & Alcoholic Ingredients")) return 100.0;
        if (category.equals("Canned & Jarred Goods")) return 100.0;
        if (category.equals("Frozen Foods")) return 100.0;
        if (category.equals("International Ingredients")) return 15.0;
        if (category.equals("Miscellaneous")) return 10.0;

        // Default if category is unknown
        return 30.0;
    }

    /**
     * Calculates the percentage of daily values for key nutrients
     * based on a 2000 calorie diet
     */
    public Map<String, Integer> calculateDailyValues(NutritionDTO nutrition) {
        Map<String, Integer> dailyValues = new HashMap<>();

        // Daily recommended values based on a 2000 calorie diet
        double dailyCalories = 2000.0;
        double dailyProtein = 50.0;
        double dailyFat = 70.0;
        double dailyCarbs = 300.0;
        double dailyFiber = 28.0;
        double dailySugar = 50.0;
        double dailySodium = 2300.0;

        // Calculate percentages
        dailyValues.put("calories", (int) Math.round((nutrition.getCalories() / dailyCalories) * 100));
        dailyValues.put("protein", (int) Math.round((nutrition.getProtein() / dailyProtein) * 100));
        dailyValues.put("fat", (int) Math.round((nutrition.getFat() / dailyFat) * 100));
        dailyValues.put("carbs", (int) Math.round((nutrition.getCarbs() / dailyCarbs) * 100));
        dailyValues.put("fiber", (int) Math.round((nutrition.getFiber() / dailyFiber) * 100));
        dailyValues.put("sugar", (int) Math.round((nutrition.getSugar() / dailySugar) * 100));
        dailyValues.put("sodium", (int) Math.round((nutrition.getSodium() / dailySodium) * 100));

        return dailyValues;
    }
}
