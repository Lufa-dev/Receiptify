import {Component, OnDestroy, OnInit} from '@angular/core';
import { RecipeService } from '../../shared/services/recipe.service';
import {Recipe, RecipeDTO} from '../../shared/models/recipe.model';
import {catchError, finalize, forkJoin, of, Subscription} from "rxjs";
import {AuthService} from "../../shared/services/auth.service";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit, OnDestroy {
  recipes: RecipeDTO[] = [];
  seasonalRecipes: RecipeDTO[] = [];
  isLoading = false;
  isLoadingMore = false;
  currentMonth = '';
  error = '';
  currentPage = 0;
  totalRecipes = 0;
  hasMoreRecipes = false;
  pageSize = 12; // Number of recipes per page
  private subscriptions: Subscription[] = [];

  constructor(
    private recipeService: RecipeService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadAllRecipes();
  }

  loadAllRecipes(): void {
    this.isLoading = true;
    this.error = '';

    // Get current month for seasonality display
    const monthSub = this.recipeService.getCurrentMonth()
      .pipe(catchError(error => {
        // Default to current browser month as fallback
        const date = new Date();
        return of(date.toLocaleString('default', { month: 'long' }).toUpperCase());
      }))
      .subscribe({
        next: (month) => {
          this.currentMonth = this.formatMonth(month);
        }
      });
    this.subscriptions.push(monthSub);

    // Load regular recipes
    const recipesSub = this.recipeService.getAllRecipes(this.currentPage, this.pageSize)
      .pipe(catchError(error => {
        return of({content: [], totalElements: 0});
      }))
      .subscribe({
        next: (response) => {
          this.recipes = response.content;
          this.totalRecipes = response.page.totalElements;
          this.hasMoreRecipes = (this.currentPage + 1) * this.pageSize < this.totalRecipes;
          this.isLoading = false;

        }
      });
    this.subscriptions.push(recipesSub);

    // Load seasonal recipes separately to handle errors independently
    const seasonalSub = this.recipeService.getSeasonalRecipes(80, 0, 8)
      .pipe(
        catchError(error => {
          return of({content: [], totalElements: 0});
        })
      )
      .subscribe({
        next: (response) => {
          this.seasonalRecipes = response.content;
        }
      });
    this.subscriptions.push(seasonalSub);
  }

  loadMoreRecipes(): void {
    if (this.isLoadingMore) return;

    this.isLoadingMore = true;
    this.currentPage++;

    const moreSub = this.recipeService.getAllRecipes(this.currentPage, this.pageSize)
      .pipe(finalize(() => this.isLoadingMore = false))
      .subscribe({
        next: (response) => {
          this.recipes = [...this.recipes, ...response.content];
          this.totalRecipes = response.page.totalElements;
          this.hasMoreRecipes = (this.currentPage + 1) * this.pageSize < this.totalRecipes;
        },
        error: (error) => {
          this.error = 'Failed to load more recipes. Please try again.';
          this.currentPage--; // Revert on error
        }
      });
    this.subscriptions.push(moreSub);
  }

  searchRecipes(query: string): void {
    if (!query.trim()) {
      this.loadAllRecipes();
      return;
    }

    this.isLoading = true;
    this.recipeService.searchRecipes(query, 0, 12)
      .subscribe({
        next: (response) => {
          this.recipes = response.content;
          // Clear seasonal recipes when searching
          this.seasonalRecipes = [];
          this.isLoading = false;
        },
        error: (error) => {
          this.error = 'Failed to search recipes. Please try again.';
          this.isLoading = false;
        }
      });
  }

  isUserLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  private formatMonth(month: string): string {
    return month.charAt(0) + month.slice(1).toLowerCase();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }
}



