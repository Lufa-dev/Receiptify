import { Component, OnInit } from '@angular/core';
import { RecipeService } from '../../shared/services/recipe.service';
import {Recipe, RecipeDTO} from '../../shared/models/recipe.model';
import {catchError, finalize, forkJoin, of} from "rxjs";
import {AuthService} from "../../shared/services/auth.service";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  recipes: RecipeDTO[] = [];
  seasonalRecipes: RecipeDTO[] = [];
  isLoading = false;
  currentMonth = '';
  error = '';

  constructor(private recipeService: RecipeService,
              private authService: AuthService
              ) {
  }

  ngOnInit(): void {
    this.loadAllRecipes();
  }

  loadAllRecipes(): void {
    this.isLoading = true;
    this.error = '';

    // Get current month for seasonality display
    this.recipeService.getCurrentMonth()
      .pipe(catchError(error => {
        console.error('Error getting current month:', error);
        // Default to current browser month as fallback
        const date = new Date();
        return of(date.toLocaleString('default', { month: 'long' }).toUpperCase());
      }))
      .subscribe({
        next: (month) => {
          this.currentMonth = this.formatMonth(month);
        }
      });

    // Load regular recipes
    this.recipeService.getAllRecipes(0, 12)
      .pipe(catchError(error => {
        console.error('Error loading regular recipes:', error);
        return of({content: [], totalElements: 0});
      }))
      .subscribe({
        next: (response) => {
          this.recipes = response.content;
          this.isLoading = false;
        }
      });

    // Load seasonal recipes separately to handle errors independently
    this.recipeService.getSeasonalRecipes(80, 0, 6)
      .pipe(
        catchError(error => {
          console.error('Error loading seasonal recipes:', error);
          return of({content: [], totalElements: 0});
        })
      )
      .subscribe({
        next: (response) => {
          this.seasonalRecipes = response.content;
          console.log('Loaded seasonal recipes:', this.seasonalRecipes);
        }
      });
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
          console.error('Error searching recipes:', error);
          this.error = 'Failed to search recipes. Please try again.';
          this.isLoading = false;
        }
      });
  }

  isUserLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  private formatMonth(month: string): string {
    // Convert "JANUARY" to "January"
    return month.charAt(0) + month.slice(1).toLowerCase();
  }
}


