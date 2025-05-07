import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import { RatingService } from '../../shared/services/rating.service';
import { AuthService } from '../../shared/services/auth.service';
import { Rating } from '../../shared/models/rating.model';
import { RatingSummary } from '../../shared/models/rating-summary.model';
import { finalize } from 'rxjs/operators';
import { Router } from '@angular/router';
import {Subscription} from "rxjs";

@Component({
  selector: 'app-recipe-rating',
  templateUrl: './recipe-rating.component.html',
  styleUrls: ['./recipe-rating.component.scss']
})
export class RecipeRatingComponent implements OnInit, OnDestroy {
  @Input() recipeId!: number | undefined;
  @Input() isOwner: boolean = false;

  userRating: number = 0;
  ratingSummary?: RatingSummary;
  isLoading = false;
  isSubmitting = false;
  error = '';
  successMessage = '';
  private subscriptions: Subscription[] = [];

  constructor(
    private ratingService: RatingService,
    public authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadRatingSummary();
    this.loadUserRating();
  }

  loadRatingSummary(): void {
    if (!this.recipeId) return;

    this.isLoading = true;
    const sub = this.ratingService.getRecipeRatingSummary(this.recipeId)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (summary) => {
          this.ratingSummary = summary;
        },
        error: (error) => {
          this.error = 'Failed to load ratings';
        }
      });
    this.subscriptions.push(sub);
  }

  loadUserRating(): void {
    if (!this.recipeId || !this.authService.isLoggedIn()) {
      return;
    }

    this.ratingService.getUserRatingForRecipe(this.recipeId)
      .subscribe({
        next: (rating) => {
          if (rating) {
            this.userRating = rating.stars;
          }
        },
        error: (error) => {
        }
      });
  }

  onRatingChange(stars: number): void {
    if (!this.recipeId) return;

    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login'], {
        queryParams: { returnUrl: `/recipe/${this.recipeId}` }
      });
      return;
    }

    if (this.isOwner) {
      this.error = 'You cannot rate your own recipe';
      return;
    }

    this.isSubmitting = true;
    this.error = '';
    this.successMessage = '';

    const rating: Rating = {
      stars: stars,
      recipeId: this.recipeId
    };

    this.ratingService.rateRecipe(rating)
      .pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: (result) => {
          this.userRating = result.stars;
          this.successMessage = 'Rating submitted successfully!';
          // Refresh rating summary
          this.loadRatingSummary();

          setTimeout(() => {
            this.successMessage = '';
          }, 3000);
        },
        error: (error) => {

          if (error.status === 401) {
            this.router.navigate(['/login'], {
              queryParams: { returnUrl: `/recipe/${this.recipeId}` }
            });
          } else if (error.error?.error) {
            this.error = error.error.error;
          } else {
            this.error = 'Failed to submit rating. Please try again.';
          }
        }
      });
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }
}
