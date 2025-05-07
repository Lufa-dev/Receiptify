import { Injectable } from '@angular/core';
import {Ingredient} from "../models/ingredient.model";

@Injectable({
  providedIn: 'root'
})
export class DietaryDetectionService {
  // Ingredient types that are animal-based (non-vegan)
  private readonly NON_VEGAN_INGREDIENTS: string[] = [
    // Meats
    'BEEF', 'CHICKEN', 'PORK', 'LAMB', 'BACON', 'HAM', 'TURKEY', 'VEAL', 'DUCK',
    'BEEF_BRISKET', 'BEEF_CHUCK', 'BEEF_GROUND', 'BEEF_RIBEYE', 'BEEF_SIRLOIN',
    'BEEF_TENDERLOIN', 'CHICKEN_BREAST', 'CHICKEN_DRUMSTICKS', 'CHICKEN_GROUND',
    'CHICKEN_THIGHS', 'CHICKEN_WHOLE', 'CHORIZO', 'GROUND_TURKEY', 'HAM',
    'LAMB_CHOPS', 'LAMB_GROUND', 'LIVER', 'PORK_BELLY', 'PORK_CHOPS',
    'PORK_GROUND', 'PORK_LOIN', 'PORK_SHOULDER', 'PROSCIUTTO', 'SALAMI',
    'SAUSAGE', 'TURKEY_BREAST', 'VEAL',

    // Seafood
    'FISH', 'SALMON', 'TUNA', 'SHRIMP', 'CRAB', 'LOBSTER', 'MUSSELS', 'OYSTERS',
    'ANCHOVIES', 'COD', 'SARDINES', 'SCALLOPS', 'TILAPIA', 'TROUT', 'TUNA_CANNED',

    // Dairy
    'MILK', 'CHEESE', 'BUTTER', 'CREAM', 'YOGURT', 'ICE_CREAM',
    'AMERICAN_CHEESE', 'BLUE_CHEESE', 'BRIE_CHEESE', 'CHEDDAR_CHEESE',
    'COTTAGE_CHEESE', 'CREAM_CHEESE', 'FETA_CHEESE', 'GOAT_CHEESE',
    'GOUDA_CHEESE', 'GREEK_YOGURT', 'HALF_AND_HALF', 'HEAVY_CREAM',
    'MOZZARELLA', 'PARMESAN_CHEESE', 'PROVOLONE_CHEESE', 'RICOTTA_CHEESE',
    'SOUR_CREAM', 'SWISS_CHEESE', 'WHIPPED_CREAM', 'YOGURT',

    // Eggs
    'EGGS',

    // Other animal products
    'GELATIN', 'HONEY'
  ];

  // Ingredients that contain gluten
  private readonly GLUTEN_INGREDIENTS: string[] = [
    'WHEAT', 'BARLEY', 'RYE', 'FLOUR', 'BREAD', 'PASTA', 'COUSCOUS',
    'CRACKERS', 'CROISSANT', 'NOODLES', 'WHEAT_GERM', 'BULGUR',
    'BEER', 'SOY_SAUCE' // Traditional soy sauce contains wheat
  ];

  // Ingredients that contain dairy
  private readonly DAIRY_INGREDIENTS: string[] = [
    'MILK', 'CHEESE', 'BUTTER', 'CREAM', 'YOGURT', 'ICE_CREAM',
    'AMERICAN_CHEESE', 'BLUE_CHEESE', 'BRIE_CHEESE', 'CHEDDAR_CHEESE',
    'COTTAGE_CHEESE', 'CREAM_CHEESE', 'FETA_CHEESE', 'GOAT_CHEESE',
    'GOUDA_CHEESE', 'GREEK_YOGURT', 'HALF_AND_HALF', 'HEAVY_CREAM',
    'MOZZARELLA', 'PARMESAN_CHEESE', 'PROVOLONE_CHEESE', 'RICOTTA_CHEESE',
    'SOUR_CREAM', 'SWISS_CHEESE', 'WHIPPED_CREAM', 'YOGURT'
  ];

  // Ingredients that are nuts or contain nuts
  private readonly NUT_INGREDIENTS: string[] = [
    'ALMONDS', 'CASHEWS', 'HAZELNUTS', 'MACADAMIA_NUTS', 'PEANUTS',
    'PECANS', 'PINE_NUTS', 'PISTACHIOS', 'WALNUTS',
    'PEANUT_OIL', 'ALMOND_MILK' // Derived products
  ];

  // High carb ingredients that would not be keto-friendly
  private readonly HIGH_CARB_INGREDIENTS: string[] = [
    'SUGAR', 'FLOUR', 'BREAD', 'PASTA', 'RICE', 'POTATOES',
    'CORN', 'OATS', 'QUINOA', 'HONEY', 'MAPLE_SYRUP',
    'BROWN_SUGAR', 'CONFECTIONERS_SUGAR', 'AGAVE_NECTAR'
  ];

  constructor() { }

  /**
   * Detect dietary tags based on recipe ingredients
   * @param ingredients List of ingredients in a recipe
   * @returns List of detected dietary tags
   */
  detectDietaryTags(ingredients: Ingredient[]): string[] {
    const dietaryTags: string[] = [];

    if (!ingredients || ingredients.length === 0) {
      return dietaryTags;
    }

    // Check if vegan (no animal products at all)
    if (this.isVegan(ingredients)) {
      dietaryTags.push('vegan');
    }

    // Check if vegetarian (may include dairy, eggs, but no meat)
    if (this.isVegetarian(ingredients)) {
      dietaryTags.push('vegetarian');
    }

    // Check if gluten-free
    if (this.isGlutenFree(ingredients)) {
      dietaryTags.push('gluten-free');
    }

    // Check if dairy-free
    if (this.isDairyFree(ingredients)) {
      dietaryTags.push('dairy-free');
    }

    // Check if nut-free
    if (this.isNutFree(ingredients)) {
      dietaryTags.push('nut-free');
    }

    // Check if keto-friendly (low carb, high fat)
    if (this.isKetoFriendly(ingredients)) {
      dietaryTags.push('keto');
    }

    return dietaryTags;
  }

  /**
   * Check if a recipe is vegan (no animal products)
   */
  private isVegan(ingredients: Ingredient[]): boolean {
    return !ingredients.some(ingredient => {
      const type = ingredient.type;
      return type && this.NON_VEGAN_INGREDIENTS.includes(type);
    });
  }

  /**
   * Check if a recipe is vegetarian (no meat but may include dairy, eggs)
   */
  private isVegetarian(ingredients: Ingredient[]): boolean {
    // A recipe is vegetarian if it doesn't contain meat or seafood
    const nonVegetarianIngredients = this.NON_VEGAN_INGREDIENTS.filter(
      item => !this.DAIRY_INGREDIENTS.includes(item) && item !== 'EGGS' && item !== 'HONEY'
    );

    return !ingredients.some(ingredient => {
      const type = ingredient.type;
      return type && nonVegetarianIngredients.includes(type);
    });
  }

  /**
   * Check if a recipe is gluten-free
   */
  private isGlutenFree(ingredients: Ingredient[]): boolean {
    return !ingredients.some(ingredient => {
      const type = ingredient.type;
      return type && this.GLUTEN_INGREDIENTS.includes(type);
    });
  }

  /**
   * Check if a recipe is dairy-free
   */
  private isDairyFree(ingredients: Ingredient[]): boolean {
    return !ingredients.some(ingredient => {
      const type = ingredient.type;
      return type && this.DAIRY_INGREDIENTS.includes(type);
    });
  }

  /**
   * Check if a recipe is nut-free
   */
  private isNutFree(ingredients: Ingredient[]): boolean {
    return !ingredients.some(ingredient => {
      const type = ingredient.type;
      return type && this.NUT_INGREDIENTS.includes(type);
    });
  }

  /**
   * Check if a recipe is keto-friendly (low carb, high fat)
   */
  private isKetoFriendly(ingredients: Ingredient[]): boolean {
    // Count high-carb ingredients
    const highCarbCount = ingredients.filter(ingredient => {
      const type = ingredient.type;
      return type && this.HIGH_CARB_INGREDIENTS.includes(type);
    }).length;

    // A recipe is likely keto-friendly if it has very few high-carb ingredients
    return highCarbCount <= 1;
  }
}


