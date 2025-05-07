import {Component, Input, OnInit} from '@angular/core';
import {finalize} from "rxjs";
import {RecipeDTO} from "../../shared/models/recipe.model";
import {RecommendationService} from "../../shared/services/recommendation.service";
import {AuthService} from "../../shared/services/auth.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-recommended-recipes',
  templateUrl: './recommended-recipes.component.html',
  styleUrl: './recommended-recipes.component.scss'
})
export class RecommendedRecipesComponent implements OnInit {
  @Input() title: string = 'Recommended for You';
  @Input() limit: number = 12;
  @Input() recommendationType: 'personal' | 'seasonal' = 'personal';

  recipes: RecipeDTO[] = [];
  isLoading = false;
  error = '';

  constructor(
    private recommendationService: RecommendationService,
    public authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadRecommendations();
  }

  loadRecommendations(retryWithPrevious: boolean = false): void {
    // Only load personalized recommendations if the user is logged in
    if (this.recommendationType === 'personal' && !this.authService.isLoggedIn()) {
      return;
    }

    this.isLoading = true;
    this.error = '';

    const recommendationObservable = this.recommendationType === 'personal'
      ? this.recommendationService.getRecommendationsForUser(this.limit, retryWithPrevious)
      : this.recommendationService.getSeasonalRecommendations(this.limit);

    recommendationObservable
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (recipes) => {
          this.recipes = recipes;

          // If we got no recommendations and haven't tried including previous interactions
          if (this.recipes.length === 0 && !retryWithPrevious && this.recommendationType === 'personal') {
            this.loadRecommendations(true);
            return;
          }

          // Check which recipes are already in a collection if user is logged in
          if (this.authService.isLoggedIn()) {
            this.recipes.forEach(recipe => {
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
          this.error = `Failed to load recommendations. Please try again.`;
        }
      });
  }


  navigateToRecipe(recipeId: number): void {
    // Track the recipe view for recommendation analytics
    if (this.authService.isLoggedIn()) {
      this.recommendationService.trackRecipeView(recipeId).subscribe();
    }
    this.router.navigate(['/recipe', recipeId]);
  }
}



