import { Component, OnInit } from '@angular/core';
import { RecipeService } from '../../shared/services/recipe.service';
import { Recipe } from '../../shared/models/recipe.model';
import {finalize, forkJoin} from "rxjs";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  recipes: Recipe[] = [];
  seasonalRecipes: Recipe[] = [];
  isLoading = false;
  currentMonth = '';

  constructor(private recipeService: RecipeService) {}

  ngOnInit(): void {
    this.loadAllRecipes();
  }

  loadAllRecipes(): void {
    this.isLoading = true;

    // Get current month for seasonality display
    this.recipeService.getCurrentMonth().subscribe({
      next: (month) => {
        this.currentMonth = this.formatMonth(month);
      },
      error: (error) => {
        console.error('Error getting current month:', error);
        // Default to current browser month as fallback
        const date = new Date();
        this.currentMonth = date.toLocaleString('default', { month: 'long' });
      }
    });

    // Use forkJoin to load both regular and seasonal recipes
    forkJoin({
      regularRecipes: this.recipeService.getAllRecipes(0, 12),
      seasonalRecipes: this.recipeService.getSeasonalRecipes(80, 0, 6) // Get highly seasonal recipes (80%+ score)
    })
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (results) => {
          this.recipes = results.regularRecipes.content;
          this.seasonalRecipes = results.seasonalRecipes.content;

          console.log('Loaded seasonal recipes:', this.seasonalRecipes);
        },
        error: (error) => {
          console.error('Error loading recipes:', error);
          this.isLoading = false;
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
          this.isLoading = false;
        }
      });
  }

  private formatMonth(month: string): string {
    // Convert "JANUARY" to "January"
    return month.charAt(0) + month.slice(1).toLowerCase();
  }
}


