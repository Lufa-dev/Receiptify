import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {RecipeService} from "../../shared/services/recipe.service";
import {RecipeDTO} from "../../shared/models/recipe.model";
import {AuthService} from "../../shared/services/auth.service";
import {PortionCalculatorService} from "../../shared/services/portion-calculator.service";
import {Subscription} from "rxjs";


@Component({
  selector: 'app-recipe-detail',
  templateUrl: './recipe-detail.component.html',
  styleUrls: ['./recipe-detail.component.scss']
})
export class RecipeDetailComponent implements OnInit, OnDestroy {
  recipe: RecipeDTO | null = null;
  originalRecipe: RecipeDTO | null = null;
  isLoading = true;
  error = '';
  isOwner = false;
  isDeleting = false;
  currentServings: number = 0;
  originalServings: number = 0;
  private subscriptions: Subscription[] = [];


  constructor(
    private recipeService: RecipeService,
    public authService: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private portionCalculatorService: PortionCalculatorService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.loadRecipe(+id);
      } else {
        this.error = 'Recipe ID not found';
        this.isLoading = false;
      }
    });
  }

  loadRecipe(id: number): void {
    this.isLoading = true;

    const username = this.authService.isLoggedIn() ?
      sessionStorage.getItem('profileName') : null;

    const sub = this.recipeService.getRecipeWithSeasonality(id, username || '')
      .subscribe({
        next: (recipe) => {
          this.recipe = JSON.parse(JSON.stringify(recipe)); // Deep copy
          this.originalRecipe = JSON.parse(JSON.stringify(recipe)); // Store original
          this.originalServings = recipe.servings || 1;
          this.currentServings = recipe.servings || 1;
          this.isLoading = false;

          const username = sessionStorage.getItem('profileName');
          this.isOwner = this.authService.isLoggedIn() &&
            username === recipe.user?.username;
        },
        error: (error) => {
          this.error = 'Failed to load recipe details.';
          this.isLoading = false;
        }
      });
    this.subscriptions.push(sub);
  }

  formatIngredientName(type: string): string {
    if (!type) return '';

    // If the type is an enum like TABLESPOON or GRAM, format it
    if (type.includes('_') || /^[A-Z0-9]+$/.test(type)) {
      return type
        .toLowerCase()
        .replace(/_/g, ' ')
        .split(' ')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ');
    }

    // Otherwise, return as is (likely already formatted)
    return type;
  }

  editRecipe(): void {
    if (this.recipe && this.recipe.id) {
      this.router.navigate(['/edit-recipe', this.recipe.id]);
    }
  }

  deleteRecipe(): void {
    if (!this.recipe || !this.recipe.id) {
      return;
    }

    if (confirm('Are you sure you want to delete this recipe?')) {
      this.isDeleting = true;
      this.recipeService.deleteRecipe(this.recipe.id).subscribe({
        next: () => {
          // Navigate to the user's recipes page or home page
          this.router.navigate(['/my-recipes']);
        },
        error: (error) => {
          this.error = 'Failed to delete recipe.';
          this.isDeleting = false;
        }
      });
    }
  }

  /**
   * Calculates the total time for the recipe in minutes
   */
  calculateTotalTime(): number {
    if (!this.recipe) return 0;

    let total = 0;
    if (this.recipe.prepTime) total += this.recipe.prepTime;
    if (this.recipe.cookTime) total += this.recipe.cookTime;
    if (this.recipe.bakingTime) total += this.recipe.bakingTime;

    return total;
  }

  /**
   * Formats time in minutes to a human-readable format
   */
  formatTime(minutes: number): string {
    if (minutes < 60) {
      return `${minutes} min`;
    }

    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;

    if (remainingMinutes === 0) {
      return `${hours} hr`;
    }

    return `${hours} hr ${remainingMinutes} min`;
  }

  /**
   * Opens the browser print dialog for the recipe
   */
  printRecipe(): void {
    window.print();
  }

  updateCommentCount(count: number): void {
    if (this.recipe) {
      this.recipe.totalComments = count;
    }
  }

  /**
   * Updates the serving size and recalculates ingredient amounts
   */
  updateServings(newServings: number | string): void {
    // Convert input to number if needed
    const servingsValue = typeof newServings === 'string' ? parseInt(newServings, 10) : newServings;

    // Validate input
    if (!this.recipe || !this.originalRecipe || isNaN(servingsValue) || servingsValue <= 0) {
      return;
    }

    // If no change in servings, no need to recalculate
    if (servingsValue === this.currentServings) {
      return;
    }

    // Calculate adjusted ingredients
    const result = this.portionCalculatorService.calculateAdjustedIngredients(
      this.originalServings,
      servingsValue,
      this.originalRecipe.ingredients
    );

    // Show notification for non-scalable ingredients
    if (result.nonScalableIngredients.length > 0 && this.currentServings === this.originalServings) {
      const notification = document.createElement('div');
      notification.className = 'scaling-notification';
      notification.innerHTML = `
        <div class="alert alert-warning alert-dismissible fade show" role="alert">
          <strong>Note:</strong> Some ingredients don't have specific amounts and won't be scaled.
          <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
      `;

      // Check if notification already exists before adding
      const existingNotification = document.querySelector('.scaling-notification');
      if (!existingNotification) {
        const container = document.querySelector('.recipe-detail-container');
        if (container) {
          container.insertBefore(notification, container.firstChild);

          // Automatically remove after 5 seconds
          setTimeout(() => {
            if (notification.parentNode) {
              notification.parentNode.removeChild(notification);
            }
          }, 5000);
        }
      }
    }

    // Update current servings and recipe
    this.currentServings = servingsValue;
    this.recipe.servings = servingsValue;
    this.recipe.ingredients = result.adjustedIngredients;
  }

  /**
   * Resets servings to original value
   */
  resetServings(): void {
    if (this.originalRecipe) {
      this.updateServings(this.originalServings);
    }
  }

  onServingsInputChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input && input.value) {
      this.updateServings(Number(input.value));
    }
  }

  getIngredientSeasonality(ingredientName: string): any {
    if (!this.recipe || !this.recipe.seasonalityInfo || !this.recipe.seasonalityInfo.ingredientSeasonality) {
      return null;
    }

    return this.recipe.seasonalityInfo.ingredientSeasonality.find(
      i => i.ingredientName === ingredientName
    ) || null;
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  protected readonly HTMLInputElement = HTMLInputElement;
}


