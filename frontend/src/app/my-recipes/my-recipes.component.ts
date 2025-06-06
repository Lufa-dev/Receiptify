import {Component, OnDestroy, OnInit} from '@angular/core';
import {Recipe} from "../../shared/models/recipe.model";
import {RecipeService} from "../../shared/services/recipe.service";
import {Router} from "@angular/router";
import {finalize, Subscription} from "rxjs";

@Component({
  selector: 'app-my-recipes',
  templateUrl: './my-recipes.component.html',
  styleUrl: './my-recipes.component.scss'
})
export class MyRecipesComponent implements OnInit, OnDestroy {
  recipes: Recipe[] = [];
  isLoading = false;
  error = '';
  successMessage = '';
  isDeletingRecipe = false;
  private subscriptions: Subscription[] = [];

  constructor(
    private recipeService: RecipeService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUserRecipes();
  }

  loadUserRecipes(): void {
    this.isLoading = true;
    const recipesSub = this.recipeService.getUserRecipes()
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (response) => {
          this.recipes = response.content;
        },
        error: (error) => {
          this.error = 'Failed to load your recipes. Please try again.';

          if (error.status === 401) {
            setTimeout(() => this.router.navigate(['/login']), 1500);
          }
        }
      });
    this.subscriptions.push(recipesSub);
  }

  editRecipe(id: number, event: Event): void {
    event.stopPropagation(); // Prevent the card click from triggering navigation
    this.router.navigate(['/edit-recipe', id]);
  }

  deleteRecipe(id: number, event: Event): void {
    event.stopPropagation(); // Prevent the card click from triggering navigation

    if (this.isDeletingRecipe) {
      return; // Prevent multiple delete operations
    }

    if (confirm('Are you sure you want to delete this recipe? This will remove it from all your collections.')) {
      this.isDeletingRecipe = true;
      this.error = '';
      this.successMessage = '';

      const deleteSub = this.recipeService.deleteRecipe(id)
        .pipe(finalize(() => this.isDeletingRecipe = false))
        .subscribe({
          next: () => {
            this.successMessage = 'Recipe deleted successfully';
            // Filter out the deleted recipe from the local array
            this.recipes = this.recipes.filter(recipe => recipe.id !== id);
          },
          error: (error) => {

            if (error.status === 401) {
              this.error = 'Your session has expired. Please log in again.';
              setTimeout(() => this.router.navigate(['/login']), 1500);
            } else if (error.status === 403) {
              this.error = 'You do not have permission to delete this recipe.';
            } else {
              this.error = 'Failed to delete recipe. Please try again.';
            }
          }
        });
      this.subscriptions.push(deleteSub);
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }
}


