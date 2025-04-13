import { Component, OnInit } from '@angular/core';
import { RecipeService } from '../../shared/services/recipe.service';
import { Recipe } from '../../shared/models/recipe.model';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  recipes: Recipe[] = [];
  isLoading = false;

  constructor(private recipeService: RecipeService) {}

  ngOnInit(): void {
    this.loadRecipes();
  }

  loadRecipes(): void {
    this.isLoading = true;
    this.recipeService.getAllRecipes(0, 12)
      .subscribe({
        next: (response) => {
          this.recipes = response.content;
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading recipes:', error);
          this.isLoading = false;
        }
      });
  }

  searchRecipes(query: string): void {
    if (!query.trim()) {
      this.loadRecipes();
      return;
    }

    this.isLoading = true;
    this.recipeService.searchRecipes(query, 0, 12)
      .subscribe({
        next: (response) => {
          this.recipes = response.content;
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error searching recipes:', error);
          this.isLoading = false;
        }
      });
  }
}

