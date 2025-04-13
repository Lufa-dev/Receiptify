import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {RecipeService} from "../../shared/services/recipe.service";
import {RecipeDTO} from "../../shared/models/DTOs/recipeDTO";


@Component({
  selector: 'app-recipe-detail',
  templateUrl: './recipe-detail.component.html',
  styleUrls: ['./recipe-detail.component.scss']
})
export class RecipeDetailComponent implements OnInit {
  recipe: RecipeDTO | null = null;
  isLoading = true;
  error = '';

  constructor(
    private recipeService: RecipeService,
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
      },
      error: (error) => {
        console.error('Error loading recipe:', error);
        this.error = 'Failed to load recipe details.';
        this.isLoading = false;
      }
    });
  }
}


