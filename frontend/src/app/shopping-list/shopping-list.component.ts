import {Component, OnInit} from '@angular/core';
import {ShoppingListItem} from "../../shared/models/shopping-list-item.model";
import {RecipeDTO} from "../../shared/models/recipe.model";
import {ShoppingListService} from "../../shared/services/shopping-list.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-shopping-list',
  templateUrl: './shopping-list.component.html',
  styleUrl: './shopping-list.component.scss'
})
export class ShoppingListComponent implements OnInit {
  recipes: RecipeDTO[] = [];
  shoppingList: ShoppingListItem[] = [];

  // Organization variables
  organizeByCategory = true; // Default organization method
  categorizedList: Record<string, ShoppingListItem[]> = {};
  categories: string[] = [];

  isEditingServings = false;
  editingRecipeId: number | null = null;
  servingsInput: number = 0;

  constructor(
    private shoppingListService: ShoppingListService,
    private router: Router
  ) { }

  ngOnInit(): void {
    // Subscribe to the shopping list changes
    this.shoppingListService.recipes$.subscribe(recipes => {
      this.recipes = recipes;
      this.generateShoppingList();
    });
  }

  /**
   * Generate the shopping list from the selected recipes
   */
  generateShoppingList(): void {
    this.shoppingList = this.shoppingListService.generateShoppingList();

    if (this.organizeByCategory) {
      this.organizeByCategoryFn();
    }
  }

  /**
   * Organize shopping list items by category
   */
  organizeByCategoryFn(): void {
    this.categorizedList = {};

    // Group items by category
    this.shoppingList.forEach(item => {
      const category = this.getCategoryForItem(item);

      if (!this.categorizedList[category]) {
        this.categorizedList[category] = [];
      }

      this.categorizedList[category].push(item);
    });

    // Get sorted category names
    this.categories = Object.keys(this.categorizedList).sort((a, b) => {
      // Put "Other" at the end
      if (a === 'Other') return 1;
      if (b === 'Other') return -1;
      return a.localeCompare(b);
    });
  }

  // Category mapping to replace long if-else chain
  private readonly CATEGORY_MAPPING: Record<string, string> = {
    'VEGETABLES': 'Vegetables',
    'Vegetables': 'Vegetables',
    'FRUITS': 'Fruits',
    'Fruits': 'Fruits',
    'PROTEINS': 'Proteins',
    'Proteins': 'Proteins',
    'DAIRY': 'Dairy & Eggs',
    'Dairy': 'Dairy & Eggs',
    'GRAINS': 'Grains & Starches',
    'Grains': 'Grains & Starches',
    'HERBS': 'Herbs & Spices',
    'Herbs': 'Herbs & Spices',
    'OILS': 'Oils, Vinegars & Condiments',
    'Oils': 'Oils, Vinegars & Condiments',
    'NUTS': 'Nuts, Seeds & Dried Fruits',
    'Nuts': 'Nuts, Seeds & Dried Fruits',
    'SWEETENERS': 'Sweeteners & Baking',
    'Sweeteners': 'Sweeteners & Baking',
    'BEVERAGES': 'Beverages',
    'Beverages': 'Beverages',
    'CANNED': 'Canned & Jarred Goods',
    'Canned': 'Canned & Jarred Goods',
    'FROZEN': 'Frozen Foods',
    'Frozen': 'Frozen Foods',
    'INTERNATIONAL': 'International',
    'International': 'International'
  };

  /**
   * Get the category for an item, based on its type
   */
  getCategoryForItem(item: ShoppingListItem): string {
    if (!item.type) return 'Other';

    const typeStr = String(item.type);

    // Loop through the mapping and check if the type contains any of the keys
    for (const [key, category] of Object.entries(this.CATEGORY_MAPPING)) {
      if (typeStr.includes(key)) {
        return category;
      }
    }

    return 'Other';
  }

  /**
   * Toggle organization method between category and alphabetical
   */
  toggleOrganization(): void {
    this.organizeByCategory = !this.organizeByCategory;

    if (this.organizeByCategory) {
      this.organizeByCategoryFn();
    }
  }

  /**
   * Remove a recipe from the shopping list
   */
  removeRecipe(recipeId: number): void {
    this.shoppingListService.removeRecipe(recipeId);
  }

  /**
   * Clear all recipes from the shopping list
   */
  clearShoppingList(): void {
    if (confirm('Are you sure you want to clear your shopping list?')) {
      this.shoppingListService.clearList();
    }
  }

  /**
   * Navigate to a recipe detail
   */
  viewRecipe(recipeId: number): void {
    this.router.navigate(['/recipe', recipeId]);
  }

  /**
   * Start editing servings for a recipe
   */
  startEditServings(recipe: RecipeDTO): void {
    this.isEditingServings = true;
    this.editingRecipeId = recipe.id || null;
    this.servingsInput = recipe.servings || 1;
  }

  /**
   * Save updated servings and recalculate ingredients
   */
  saveServings(): void {
    if (this.editingRecipeId && this.servingsInput > 0) {
      this.shoppingListService.updateServings(this.editingRecipeId, this.servingsInput);
    }
    this.cancelEditServings();
  }

  /**
   * Cancel editing servings
   */
  cancelEditServings(): void {
    this.isEditingServings = false;
    this.editingRecipeId = null;
  }

  /**
   * Toggle checked state of a shopping list item
   */
  toggleItemChecked(item: ShoppingListItem): void {
    item.checked = !item.checked;
  }

  /**
   * Print the shopping list
   */
  printShoppingList(): void {
    window.print();
  }

  /**
   * Export shopping list as plain text
   */
  exportShoppingList(): void {
    let content = "Shopping List\n\n";

    if (this.organizeByCategory) {
      // Export by category
      for (const category of this.categories) {
        content += `## ${category}\n`;

        for (const item of this.categorizedList[category]) {
          const amount = item.amount ? `${item.amount} ${item.unit || ''} ` : '';
          content += `- ${amount}${item.name} (${item.recipes.join(', ')})\n`;
        }

        content += '\n';
      }
    } else {
      // Export alphabetically
      for (const item of this.shoppingList.sort((a, b) => a.name.localeCompare(b.name))) {
        const amount = item.amount ? `${item.amount} ${item.unit || ''} ` : '';
        content += `- ${amount}${item.name} (${item.recipes.join(', ')})\n`;
      }
    }

    // Create a downloadable file
    const blob = new Blob([content], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);

    // Create a temporary link element and trigger download
    const a = document.createElement('a');
    a.href = url;
    a.download = 'shopping-list.txt';
    document.body.appendChild(a);
    a.click();

    // Clean up
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }
}



