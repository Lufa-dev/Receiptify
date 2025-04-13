import {Component, OnInit} from '@angular/core';
import {Recipe} from "../../shared/models/recipe.model";
import {RecipeService} from "../../shared/services/recipe.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-my-recipes',
  templateUrl: './my-recipes.component.html',
  styleUrl: './my-recipes.component.scss'
})
export class MyRecipesComponent implements OnInit {
  recipes: Recipe[] = [];
  isLoading = false;
  error = '';

  constructor(
    private recipeService: RecipeService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUserRecipes();
  }

  loadUserRecipes(): void {
    this.isLoading = true;
    this.recipeService.getUserRecipes()
      .subscribe({
        next: (response) => {
          this.recipes = response.content;
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading user recipes:', error);
          this.error = 'Failed to load your recipes. Please try again.';
          this.isLoading = false;
        }
      });
  }

  editRecipe(id: number, event: Event): void {
    event.stopPropagation(); // Prevent the card click from triggering navigation
    this.router.navigate(['/edit-recipe', id]);
  }

  deleteRecipe(id: number, event: Event): void {
    event.stopPropagation(); // Prevent the card click from triggering navigation
    if (confirm('Are you sure you want to delete this recipe?')) {
      this.recipeService.deleteRecipe(id).subscribe({
        next: () => {
          this.recipes = this.recipes.filter(recipe => recipe.id !== id);
        },
        error: (error) => {
          console.error('Error deleting recipe:', error);
          alert('Failed to delete recipe. Please try again.');
        }
      });
    }
  }
}

