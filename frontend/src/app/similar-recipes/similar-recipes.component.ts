import {Component, Input, OnInit} from '@angular/core';
import {RecipeDTO} from "../../shared/models/recipe.model";
import {RecommendationService} from "../../shared/services/recommendation.service";
import {Router} from "@angular/router";
import {finalize} from "rxjs";
import {AuthService} from "../../shared/services/auth.service";

@Component({
  selector: 'app-similar-recipes',
  templateUrl: './similar-recipes.component.html',
  styleUrl: './similar-recipes.component.scss'
})
export class SimilarRecipesComponent implements OnInit {
  @Input() recipeId!: number;
  @Input() limit: number = 4;

  similarRecipes: RecipeDTO[] = [];
  isLoading = false;
  error = '';

  constructor(
    private recommendationService: RecommendationService,
    private router: Router,
    public authService: AuthService
  ) { }

  ngOnInit(): void {
    this.loadSimilarRecipes();
  }

  loadSimilarRecipes(): void {
    if (!this.recipeId) {
      return;
    }

    this.isLoading = true;
    this.error = '';

    this.recommendationService.getSimilarRecipes(this.recipeId, this.limit)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (recipes) => {
          this.similarRecipes = recipes;

          // Check which recipes are already in a collection if user is logged in
          if (this.authService.isLoggedIn()) {
            this.similarRecipes.forEach(recipe => {
              if (recipe.id) {
                this.recommendationService.isRecipeInAnyCollection(recipe.id)
                  .subscribe(isInCollection => {
                    recipe.isInCollection = isInCollection;
                  });
              }
            });
          }
        },
        error: (error) => {
          this.error = 'Failed to load similar recipes';
        }
      });
  }

  navigateToRecipe(recipeId: number): void {
    // Track the recipe view for recommendation analytics
    if (this.authService.isLoggedIn()) {
      this.recommendationService.trackRecipeView(recipeId).subscribe();
    }

    // Navigate to the recipe
    this.router.navigate(['/recipe', recipeId]);
  }
}




