import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { AdminService } from '../../../shared/services/admin.service';
import { RecipeDTO } from '../../../shared/models/recipe.model';
import { finalize } from 'rxjs/operators';
import { RECIPE_CATEGORIES, RECIPE_CUISINES, RECIPE_DIFFICULTIES, RECIPE_COST_RATINGS } from '../../../shared/constants/recipe-options';

@Component({
  selector: 'app-recipe-detail-admin',
  templateUrl: './recipe-detail-admin.component.html',
  styleUrls: ['./recipe-detail-admin.component.scss']
})
export class RecipeDetailAdminComponent implements OnInit {
  recipeId: number = 0;
  recipe: RecipeDTO | null = null;
  recipeForm: FormGroup;
  isLoading = false;
  isSubmitting = false;
  error = '';
  successMessage = '';
  isEditing = false;

  // Options for select dropdowns
  categories = RECIPE_CATEGORIES;
  cuisines = RECIPE_CUISINES;
  difficulties = RECIPE_DIFFICULTIES;
  costRatings = RECIPE_COST_RATINGS;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private adminService: AdminService,
    private fb: FormBuilder
  ) {
    this.recipeForm = this.createRecipeForm();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.recipeId = +params['id'];
      if (this.recipeId) {
        this.loadRecipeDetails();
      } else {
        this.error = 'Invalid recipe ID';
      }
    });
  }

  createRecipeForm(): FormGroup {
    return this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', Validators.maxLength(500)],
      category: [''],
      cuisine: [''],
      servings: [null, [Validators.min(1)]],
      difficulty: [''],
      costRating: [''],
      prepTime: [null, [Validators.min(0)]],
      cookTime: [null, [Validators.min(0)]],
      bakingTime: [null, [Validators.min(0)]],
      ingredients: this.fb.array([]),
      steps: this.fb.array([])
    });
  }

  loadRecipeDetails(): void {
    this.isLoading = true;
    this.error = '';

    this.adminService.getRecipeById(this.recipeId)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (recipe) => {
          this.recipe = recipe;
          this.updateFormWithRecipe(recipe);
        },
        error: (error) => {
          console.error('Error loading recipe details:', error);
          this.error = 'Failed to load recipe details. Please try again.';
        }
      });
  }

  updateFormWithRecipe(recipe: RecipeDTO): void {
    // Clear existing arrays
    while (this.ingredientsArray.length) {
      this.ingredientsArray.removeAt(0);
    }

    while (this.stepsArray.length) {
      this.stepsArray.removeAt(0);
    }

    // Add ingredients
    if (recipe.ingredients && recipe.ingredients.length) {
      recipe.ingredients.forEach(ingredient => {
        this.ingredientsArray.push(this.fb.group({
          id: [ingredient.id],
          type: [ingredient.type, Validators.required],
          name: [ingredient.name],
          amount: [ingredient.amount],
          unit: [ingredient.unit]
        }));
      });
    }

    // Add steps
    if (recipe.steps && recipe.steps.length) {
      recipe.steps.forEach(step => {
        this.stepsArray.push(this.fb.group({
          id: [step.id],
          stepNumber: [step.stepNumber, [Validators.required, Validators.min(0)]],
          instruction: [step.instruction, [Validators.required, Validators.maxLength(1000)]]
        }));
      });
    }

    // Update other fields
    this.recipeForm.patchValue({
      title: recipe.title,
      description: recipe.description,
      category: recipe.category,
      cuisine: recipe.cuisine,
      servings: recipe.servings,
      difficulty: recipe.difficulty,
      costRating: recipe.costRating,
      prepTime: recipe.prepTime,
      cookTime: recipe.cookTime,
      bakingTime: recipe.bakingTime
    });
  }

  get ingredientsArray(): FormArray {
    return this.recipeForm.get('ingredients') as FormArray;
  }

  get stepsArray(): FormArray {
    return this.recipeForm.get('steps') as FormArray;
  }

  addIngredient(): void {
    this.ingredientsArray.push(this.fb.group({
      id: [null],
      type: ['', Validators.required],
      name: [''],
      amount: [''],
      unit: ['']
    }));
  }

  removeIngredient(index: number): void {
    this.ingredientsArray.removeAt(index);
  }

  addStep(): void {
    const nextStepNumber = this.stepsArray.length;
    this.stepsArray.push(this.fb.group({
      id: [null],
      stepNumber: [nextStepNumber, [Validators.required, Validators.min(0)]],
      instruction: ['', [Validators.required, Validators.maxLength(1000)]]
    }));
  }

  removeStep(index: number): void {
    this.stepsArray.removeAt(index);

    // Update step numbers for remaining steps
    for (let i = index; i < this.stepsArray.length; i++) {
      this.stepsArray.at(i).get('stepNumber')?.setValue(i);
    }
  }

  toggleEditMode(): void {
    this.isEditing = !this.isEditing;

    if (!this.isEditing && this.recipe) {
      // Reset form to original values when canceling edit
      this.updateFormWithRecipe(this.recipe);
    }
  }

  onSubmit(): void {
    if (this.recipeForm.invalid) {
      // Mark all fields as touched to trigger validation messages
      this.markFormGroupTouched(this.recipeForm);
      return;
    }

    this.isSubmitting = true;
    this.error = '';
    this.successMessage = '';

    const updatedRecipe: RecipeDTO = {
      ...this.recipeForm.value,
      id: this.recipeId
    };

    this.adminService.updateRecipe(this.recipeId, updatedRecipe)
      .pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: (recipe) => {
          this.recipe = recipe;
          this.successMessage = 'Recipe updated successfully';
          this.isEditing = false;
          this.updateFormWithRecipe(recipe);
        },
        error: (error) => {
          console.error('Error updating recipe:', error);
          this.error = 'Failed to update recipe. Please try again.';
        }
      });
  }

  // Helper function to mark all fields as touched
  markFormGroupTouched(formGroup: FormGroup): void {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();

      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      } else if (control instanceof FormArray) {
        control.controls.forEach(arrayControl => {
          if (arrayControl instanceof FormGroup) {
            this.markFormGroupTouched(arrayControl);
          } else {
            arrayControl.markAsTouched();
          }
        });
      }
    });
  }

  deleteRecipe(): void {
    if (confirm('Are you sure you want to delete this recipe? This action cannot be undone.')) {
      this.isLoading = true;
      this.error = '';
      this.successMessage = '';

      this.adminService.deleteRecipe(this.recipeId)
        .pipe(finalize(() => this.isLoading = false))
        .subscribe({
          next: () => {
            this.successMessage = 'Recipe deleted successfully';
            // Navigate back to recipe list after short delay
            setTimeout(() => {
              this.router.navigate(['/admin/recipes']);
            }, 1500);
          },
          error: (error) => {
            console.error('Error deleting recipe:', error);
            this.error = 'Failed to delete recipe. Please try again.';
          }
        });
    }
  }

  viewOnSite(): void {
    this.router.navigate(['/recipe', this.recipeId]);
  }

  goBack(): void {
    this.router.navigate(['/admin/recipes']);
  }
}
