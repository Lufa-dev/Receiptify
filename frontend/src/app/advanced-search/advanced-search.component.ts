import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { RecipeService } from '../../shared/services/recipe.service';
import { IngredientService } from '../../shared/services/ingredient.service';
import { RecipeSearchCriteria, SearchFilterOptions } from '../../shared/models/recipe-search-criteria.model';
import { RecipeDTO } from '../../shared/models/recipe.model';
import { IngredientType } from '../../shared/models/ingredient-type.model';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-advanced-search',
  templateUrl: './advanced-search.component.html',
  styleUrls: ['./advanced-search.component.scss']
})
export class AdvancedSearchComponent implements OnInit {
  searchForm: FormGroup;
  isLoading = false;
  recipes: RecipeDTO[] = [];
  totalRecipes = 0;
  currentPage = 0;
  pageSize = 12;

  // Filter options - hardcoded to match recipe-form.component
  filterOptions: SearchFilterOptions = {
    categories: [
      'appetizer',
      'breakfast',
      'main',
      'soup',
      'salad',
      'side',
      'dessert',
      'snack',
      'bread',
      'cake',
      'cookie',
      'pastry',
      'beverage',
      'sauce',
      'preserve'
    ],
    cuisines: [
      'american',
      'italian',
      'french',
      'chinese',
      'japanese',
      'indian',
      'mexican',
      'thai',
      'mediterranean',
      'greek',
      'spanish',
      'middle-eastern',
      'korean',
      'vietnamese',
      'caribbean',
      'african',
      'german',
      'british',
      'fusion'
    ],
    difficulties: ['easy', 'medium', 'hard'],
    costRatings: ['budget', 'moderate', 'expensive']
  };

  ingredientTypes: IngredientType[] = [];
  selectedIngredients: IngredientType[] = [];
  excludedIngredients: IngredientType[] = [];

  // Sort options
  sortOptions = [
    { value: 'createdAt', label: 'Most Recent' },
    { value: 'title', label: 'Title (A-Z)' },
    { value: 'prepTime', label: 'Quickest to Prepare' },
    { value: 'cookTime', label: 'Quickest to Cook' },
    { value: 'bakingTime', label: 'Quickest to Bake' }
  ];

  constructor(
    private fb: FormBuilder,
    private recipeService: RecipeService,
    private ingredientService: IngredientService,
    private router: Router
  ) {
    this.searchForm = this.createSearchForm();
  }

  ngOnInit(): void {
    this.loadIngredientTypes();
  }

  private createSearchForm(): FormGroup {
    return this.fb.group({
      searchQuery: [''],
      category: [''],
      cuisine: [''],
      difficulty: [''],
      costRating: [''],
      minServings: [null],
      maxServings: [null],
      maxPrepTime: [null],
      maxCookTime: [null],
      maxTotalTime: [null],
      sortBy: ['createdAt'],
      sortDirection: ['desc']
    });
  }

  private loadIngredientTypes(): void {
    this.ingredientService.getAllIngredientTypes().subscribe({
      next: (ingredients) => {
        this.ingredientTypes = ingredients;
        console.log('Ingredients loaded:', this.ingredientTypes);
      },
      error: (error) => {
        console.error('Error loading ingredient types:', error);
      }
    });
  }

  onSearch(): void {
    this.currentPage = 0;
    this.performSearch();
  }

  onLoadMore(): void {
    this.currentPage++;
    this.performSearch(true);
  }

  private performSearch(append: boolean = false): void {
    this.isLoading = true;

    // Clean up the form values to remove empty strings and nulls
    const formValue = this.searchForm.value;
    const cleanedFormValue: any = {};

    Object.keys(formValue).forEach(key => {
      if (formValue[key] !== '' && formValue[key] !== null) {
        cleanedFormValue[key] = formValue[key];
      }
    });

    const criteria: RecipeSearchCriteria = {
      ...cleanedFormValue,
      includeIngredients: this.selectedIngredients.map(i => i.name),
      excludeIngredients: this.excludedIngredients.map(i => i.name)
    };

    console.log('Sending search criteria:', criteria); // Debug log

    this.recipeService.advancedSearchRecipes(criteria, this.currentPage, this.pageSize)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (response) => {
          console.log('Search response:', response); // Debug log
          if (append) {
            this.recipes = [...this.recipes, ...response.content];
          } else {
            this.recipes = response.content;
          }
          this.totalRecipes = response.totalElements;
        },
        error: (error) => {
          console.error('Error searching recipes:', error);
        }
      });
  }

  addSelectedIngredient(ingredientValue: string): void {
    // Find the ingredient object from the ingredientTypes array
    const ingredient = this.ingredientTypes.find(i => i.name === ingredientValue);
    if (ingredient && !this.selectedIngredients.includes(ingredient)) {
      this.selectedIngredients.push(ingredient);
    }
  }

  removeSelectedIngredient(ingredient: IngredientType): void {
    this.selectedIngredients = this.selectedIngredients.filter(i => i !== ingredient);
  }

  addExcludedIngredient(ingredientValue: string): void {
    // Find the ingredient object from the ingredientTypes array
    const ingredient = this.ingredientTypes.find(i => i.name === ingredientValue);
    if (ingredient && !this.excludedIngredients.includes(ingredient)) {
      this.excludedIngredients.push(ingredient);
    }
  }

  removeExcludedIngredient(ingredient: IngredientType): void {
    this.excludedIngredients = this.excludedIngredients.filter(i => i !== ingredient);
  }

  clearFilters(): void {
    this.searchForm.reset({
      sortBy: 'createdAt',
      sortDirection: 'desc'
    });
    this.selectedIngredients = [];
    this.excludedIngredients = [];
    this.onSearch();
  }

  hasMoreRecipes(): boolean {
    return (this.currentPage + 1) * this.pageSize < this.totalRecipes;
  }

  navigateToRecipe(recipeId: number | undefined): void {
    if (recipeId) {
      this.router.navigate(['/recipe', recipeId]);
    }
  }
}







