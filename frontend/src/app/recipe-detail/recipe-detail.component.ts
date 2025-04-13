import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {RecipeService} from "../../shared/services/recipe.service";
import {RecipeDTO} from "../../shared/models/recipe.model";
import {AuthService} from "../../shared/services/auth.service";


@Component({
  selector: 'app-recipe-detail',
  templateUrl: './recipe-detail.component.html',
  styleUrls: ['./recipe-detail.component.scss']
})
export class RecipeDetailComponent implements OnInit {
  recipe: RecipeDTO | null = null;
  isLoading = true;
  error = '';
  isOwner = false;
  isDeleting = false;

  constructor(
    private recipeService: RecipeService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
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
    this.recipeService.getRecipeById(id).subscribe({
      next: (recipe) => {
        this.recipe = recipe;
        this.isLoading = false;

        const username = sessionStorage.getItem('profileName');
        this.isOwner = this.authService.isLoggedIn() &&
          username === recipe.user?.username;

      },
      error: (error) => {
        console.error('Error loading recipe:', error);
        this.error = 'Failed to load recipe details.';
        this.isLoading = false;
      }
    });
  }

  formatIngredientName(type: string): string {
    if (!type) return '';

    return type
      .toLowerCase()
      .replace(/_/g, ' ')
      .split(' ')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  editRecipe(): void {
    if (this.recipe && this.recipe.id) {
      this.router.navigate(['/recipe-form', this.recipe.id]);
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
          console.error('Error deleting recipe:', error);
          this.error = 'Failed to delete recipe.';
          this.isDeleting = false;
        }
      });
    }
  }
}


