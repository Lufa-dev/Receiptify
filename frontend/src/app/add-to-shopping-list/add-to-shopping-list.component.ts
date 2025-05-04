import {Component, Input, OnInit} from '@angular/core';
import {RecipeDTO} from "../../shared/models/recipe.model";
import {ShoppingListService} from "../../shared/services/shopping-list.service";

@Component({
  selector: 'app-add-to-shopping-list',
  templateUrl: './add-to-shopping-list.component.html',
  styleUrl: './add-to-shopping-list.component.scss'
})
export class AddToShoppingListComponent implements OnInit {
  @Input() recipe!: RecipeDTO;

  isInShoppingList = false;
  successMessage = '';

  constructor(private shoppingListService: ShoppingListService) { }

  ngOnInit(): void {
    this.checkIfInShoppingList();

    // Subscribe to changes in the shopping list
    this.shoppingListService.recipes$.subscribe(() => {
      this.checkIfInShoppingList();
    });
  }

  checkIfInShoppingList(): void {
    if (this.recipe && this.recipe.id !== undefined) {
      this.isInShoppingList = this.shoppingListService.isRecipeInList(this.recipe.id);
    }
  }

  toggleShoppingList(): void {
    if (!this.recipe || !this.recipe.id) return;

    if (this.isInShoppingList) {
      this.shoppingListService.removeRecipe(this.recipe.id);
      this.successMessage = 'Removed from shopping list';
    } else {
      this.shoppingListService.addRecipe(this.recipe);
      this.successMessage = 'Added to shopping list';
    }

    // Clear message after 3 seconds
    setTimeout(() => {
      this.successMessage = '';
    }, 3000);
  }
}

