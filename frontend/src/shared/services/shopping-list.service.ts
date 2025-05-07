import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { RecipeDTO } from '../models/recipe.model';
import { ShoppingListItem } from '../models/shopping-list-item.model';
import { PortionCalculatorService } from './portion-calculator.service';

@Injectable({
  providedIn: 'root'
})
export class ShoppingListService {
  private recipesSubject = new BehaviorSubject<RecipeDTO[]>([]);
  public recipes$ = this.recipesSubject.asObservable();

  constructor(private portionCalculator: PortionCalculatorService) {
    // Restore shopping list from localStorage if available
    this.loadFromLocalStorage();
  }

  private loadFromLocalStorage(): void {
    try {
      const storedRecipes = localStorage.getItem('shopping-list-recipes');
      if (storedRecipes) {
        this.recipesSubject.next(JSON.parse(storedRecipes));
      }
    } catch (error) {
    }
  }

  private saveToLocalStorage(): void {
    try {
      localStorage.setItem('shopping-list-recipes', JSON.stringify(this.recipesSubject.value));
    } catch (error) {
    }
  }

  /**
   * Get the current list of recipes in the shopping list
   */
  getRecipes(): RecipeDTO[] {
    return this.recipesSubject.value;
  }

  /**
   * Add a recipe to the shopping list
   */
  addRecipe(recipe: RecipeDTO): void {
    // Check if recipe is already in the list
    const recipes = this.recipesSubject.value;
    const exists = recipes.some(r => r.id === recipe.id);

    if (!exists) {
      const updatedRecipes = [...recipes, recipe];
      this.recipesSubject.next(updatedRecipes);
      this.saveToLocalStorage();
    }
  }

  /**
   * Remove a recipe from the shopping list
   */
  removeRecipe(recipeId: number): void {
    const recipes = this.recipesSubject.value;
    const updatedRecipes = recipes.filter(r => r.id !== recipeId);
    this.recipesSubject.next(updatedRecipes);
    this.saveToLocalStorage();
  }

  /**
   * Clear the shopping list completely
   */
  clearList(): void {
    this.recipesSubject.next([]);
    localStorage.removeItem('shopping-list-recipes');
  }

  /**
   * Check if a recipe is in the shopping list
   */
  isRecipeInList(recipeId: number): boolean {
    return this.recipesSubject.value.some(r => r.id === recipeId);
  }

  /**
   * Update servings for a recipe in the shopping list
   */
  updateServings(recipeId: number, newServings: number): void {
    const recipes = this.recipesSubject.value;
    const index = recipes.findIndex(r => r.id === recipeId);

    if (index !== -1) {
      const recipe = recipes[index];
      const originalServings = recipe.servings || 1;

      // Create a deep copy to modify
      const updatedRecipe = JSON.parse(JSON.stringify(recipe));

      // Update the servings
      updatedRecipe.servings = newServings;

      // Recalculate ingredient amounts
      if (updatedRecipe.ingredients && updatedRecipe.ingredients.length > 0) {
        const result = this.portionCalculator.calculateAdjustedIngredients(
          originalServings,
          newServings,
          recipe.ingredients
        );
        updatedRecipe.ingredients = result.adjustedIngredients;
      }

      // Update the recipe in the list
      const updatedRecipes = [...recipes];
      updatedRecipes[index] = updatedRecipe;

      this.recipesSubject.next(updatedRecipes);
      this.saveToLocalStorage();
    }
  }

  /**
   * Generate a consolidated shopping list from all recipes
   */
  generateShoppingList(): ShoppingListItem[] {
    const recipes = this.recipesSubject.value;
    const consolidatedList: ShoppingListItem[] = [];

    // Map to track ingredients by name for consolidation
    const ingredientMap = new Map<string, ShoppingListItem>();

    recipes.forEach(recipe => {
      if (recipe.ingredients) {
        recipe.ingredients.forEach(ingredient => {
          const name = ingredient.name || '';
          const amount = ingredient.amount || '';
          const unit = ingredient.unit || '';
          const type = ingredient.type || '';

          // Create a key for ingredient consolidation
          // For ingredients with amounts and units, consolidate by name+unit
          // For ingredients without amounts, just use the name
          const key = amount ? `${name}|${unit}` : name;

          // Try to parse the amount
          const numericAmount = this.portionCalculator.parseAmount(amount);

          if (ingredientMap.has(key)) {
            // If ingredient already exists in the map, update the amount
            const existingItem = ingredientMap.get(key)!;

            if (numericAmount !== null && existingItem.numericAmount !== null) {
              // Both are numeric, add them together
              existingItem.numericAmount += numericAmount;
              existingItem.amount = this.portionCalculator.formatAmount(existingItem.numericAmount);
            } else if (amount) {
              // If we can't add numerically, just append the new amount
              existingItem.amount = `${existingItem.amount}, ${amount}`;
            }

            // Add recipe reference
            existingItem.recipes.push(recipe.title);
          } else {
            // Create a new item if it doesn't exist
            const item: ShoppingListItem = {
              name,
              amount,
              unit,
              type,
              numericAmount: numericAmount,
              checked: false,
              recipes: [recipe.title]
            };

            ingredientMap.set(key, item);
          }
        });
      }
    });

    // Convert map values to array
    return Array.from(ingredientMap.values());
  }
}
