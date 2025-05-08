import {Component, OnDestroy, OnInit} from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { RecipeService } from '../../shared/services/recipe.service';
import { IngredientService } from '../../shared/services/ingredient.service';
import { RecipeSearchCriteria, SearchFilterOptions } from '../../shared/models/recipe-search-criteria.model';
import { RecipeDTO } from '../../shared/models/recipe.model';
import { IngredientType } from '../../shared/models/ingredient-type.model';
import {finalize, Subscription} from 'rxjs';
import {SelectOption} from "../../shared/components/searchable-select/searchable-select.component";

@Component({
  selector: 'app-advanced-search',
  templateUrl: './advanced-search.component.html',
  styleUrls: ['./advanced-search.component.scss']
})
export class AdvancedSearchComponent implements OnInit, OnDestroy {
  searchForm: FormGroup;
  isLoading = false;
  recipes: RecipeDTO[] = [];
  totalRecipes = 0;
  currentPage = 0;
  pageSize = 12;
  private subscriptions: Subscription[] = [];

  ingredientOptions: SelectOption[] = [];
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
    const ingredientSub = this.loadIngredientTypes();
    this.subscriptions.push(ingredientSub);
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
      seasonalOnly: [false],
      minSeasonalScore: [70],
      sortBy: ['createdAt'],
      sortDirection: ['desc']
    });
  }

  private loadIngredientTypes(): Subscription  {
    return this.ingredientService.getIngredientsByCategory().subscribe({
      next: (ingredientsByCategory) => {
        // Convert to select options format
        this.ingredientOptions = [];
        Object.entries(ingredientsByCategory).forEach(([category, ingredients]) => {
          ingredients.forEach(ingredient => {
            this.ingredientOptions.push({
              label: ingredient.displayName,
              value: ingredient.name,
              group: category
            });
          });
        });
      },
      error: (error) => {
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

    // Decide which search method to use based on seasonality filter
    let searchObservable;

    if (formValue.seasonalOnly) {
      searchObservable = this.recipeService.getSeasonalRecipes(
        formValue.minSeasonalScore,
        this.currentPage,
        this.pageSize
      );
    } else {
      searchObservable = this.recipeService.advancedSearchRecipes(
        criteria,
        this.currentPage,
        this.pageSize
      );
    }

    const searchSub = searchObservable.pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (response) => {
          if (append) {
            this.recipes = [...this.recipes, ...response.content];
          } else {
            this.recipes = response.content;
          }
          this.totalRecipes = response.page.totalElements;
        },
        error: (error) => {
        }
      });
    this.subscriptions.push(searchSub);
  }

  addSelectedIngredient(option: SelectOption | null): void {
    if (!option) return;

    const ingredient = this.findIngredientByName(option.value);
    if (ingredient && !this.selectedIngredients.some(i => i.name === ingredient.name)) {
      this.selectedIngredients.push(ingredient);
    }
  }

  removeSelectedIngredient(ingredient: IngredientType): void {
    this.selectedIngredients = this.selectedIngredients.filter(i => i !== ingredient);
  }

  addExcludedIngredient(option: SelectOption | null): void {
    if (!option) return;

    const ingredient = this.findIngredientByName(option.value);
    if (ingredient && !this.excludedIngredients.some(i => i.name === ingredient.name)) {
      this.excludedIngredients.push(ingredient);
    }
  }

  removeExcludedIngredient(ingredient: IngredientType): void {
    this.excludedIngredients = this.excludedIngredients.filter(i => i !== ingredient);
  }

  private findIngredientByName(name: string): IngredientType | undefined {
    for (const option of this.ingredientOptions) {
      if (option.value === name) {
        return {
          name: option.value,
          displayName: option.label,
          category: option.group || ''
        };
      }
    }
    return undefined;
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

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }
}








