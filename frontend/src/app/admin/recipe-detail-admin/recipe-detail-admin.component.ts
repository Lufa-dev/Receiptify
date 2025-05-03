import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { AdminService } from '../../../shared/services/admin.service';
import { RecipeDTO } from '../../../shared/models/recipe.model';
import { finalize } from 'rxjs/operators';
import { RECIPE_CATEGORIES, RECIPE_CUISINES, RECIPE_DIFFICULTIES, RECIPE_COST_RATINGS } from '../../../shared/constants/recipe-options';
import {SelectOption} from "../../../shared/components/searchable-select/searchable-select.component";
import {IngredientType} from "../../../shared/models/ingredient-type.model";
import {IngredientService} from "../../../shared/services/ingredient.service";
import {UnitService} from "../../../shared/services/unit.service";

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

  // Options for searchable select component
  ingredientTypeOptions: SelectOption[] = [];
  ingredientTypes: IngredientType[] = [];
  unitTypeOptions: SelectOption[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private adminService: AdminService,
    private ingredientService: IngredientService,
    private unitService: UnitService,
    private fb: FormBuilder
  ) {
    this.recipeForm = this.createRecipeForm();
  }

  ngOnInit(): void {
    // Load ingredient types for dropdown
    this.loadIngredientTypes();

    // Load unit types for dropdown
    this.loadUnitTypes();

    this.route.params.subscribe(params => {
      this.recipeId = +params['id'];
      if (this.recipeId) {
        this.loadRecipeDetails();
      } else {
        this.error = 'Invalid recipe ID';
      }
    });
  }

  loadIngredientTypes(): void {
    this.ingredientService.getAllIngredientTypes().subscribe(types => {
      this.ingredientTypes = types;
      this.ingredientTypeOptions = types.map(type => ({
        label: type.displayName,
        value: type.name
      }));
    });
  }

  loadUnitTypes(): void {
    this.unitService.getAllUnitTypes().subscribe(units => {
      this.unitTypeOptions = units.map(unit => ({
        label: unit.symbol,
        value: unit.name
      }));
    });
  }

  createRecipeForm(): FormGroup {
    // All form controls start as disabled since we begin in view mode, not edit mode
    return this.fb.group({
      title: [{value: '', disabled: true}, [Validators.required, Validators.maxLength(100)]],
      description: [{value: '', disabled: true}, Validators.maxLength(500)],
      category: [{value: '', disabled: true}],
      cuisine: [{value: '', disabled: true}],
      servings: [{value: null, disabled: true}, [Validators.min(1)]],
      difficulty: [{value: '', disabled: true}],
      costRating: [{value: '', disabled: true}],
      prepTime: [{value: null, disabled: true}, [Validators.min(0)]],
      cookTime: [{value: null, disabled: true}, [Validators.min(0)]],
      bakingTime: [{value: null, disabled: true}, [Validators.min(0)]],
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
          id: [{value: ingredient.id, disabled: !this.isEditing}],
          type: [{value: ingredient.type, disabled: !this.isEditing}, Validators.required],
          typeSelector: [{value: ingredient.type, disabled: !this.isEditing}],
          name: [{value: ingredient.name, disabled: !this.isEditing}],
          amount: [{value: ingredient.amount, disabled: !this.isEditing}],
          unit: [{value: ingredient.unit, disabled: !this.isEditing}]
        }));
      });
    }

    // Add steps
    if (recipe.steps && recipe.steps.length) {
      recipe.steps.forEach(step => {
        this.stepsArray.push(this.fb.group({
          id: [{value: step.id, disabled: !this.isEditing}],
          stepNumber: [{value: step.stepNumber, disabled: !this.isEditing},
            [Validators.required, Validators.min(0)]],
          instruction: [{value: step.instruction, disabled: !this.isEditing},
            [Validators.required, Validators.maxLength(1000)]]
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
      id: [{value: null, disabled: !this.isEditing}],
      type: [{value: '', disabled: !this.isEditing}, Validators.required],
      typeSelector: [{value: '', disabled: !this.isEditing}],
      name: [{value: '', disabled: !this.isEditing}],
      amount: [{value: '', disabled: !this.isEditing}],
      unit: [{value: '', disabled: !this.isEditing}]
    }));
  }

  removeIngredient(index: number): void {
    this.ingredientsArray.removeAt(index);
  }

  addStep(): void {
    const nextStepNumber = this.stepsArray.length;
    this.stepsArray.push(this.fb.group({
      id: [{value: null, disabled: !this.isEditing}],
      stepNumber: [{value: nextStepNumber, disabled: !this.isEditing},
        [Validators.required, Validators.min(0)]],
      instruction: [{value: '', disabled: !this.isEditing},
        [Validators.required, Validators.maxLength(1000)]]
    }));
  }

  removeStep(index: number): void {
    this.stepsArray.removeAt(index);

    // Update step numbers for remaining steps
    for (let i = index; i < this.stepsArray.length; i++) {
      const control = this.stepsArray.at(i).get('stepNumber');
      if (control) {
        control.setValue(i);
      }
    }
  }

  toggleEditMode(): void {
    this.isEditing = !this.isEditing;

    if (this.isEditing) {
      // Enable all form controls when entering edit mode
      Object.keys(this.recipeForm.controls).forEach(key => {
        const control = this.recipeForm.get(key);
        if (control && !(control instanceof FormArray)) {
          control.enable();
        }
      });

      // Enable controls in form arrays
      for (let i = 0; i < this.ingredientsArray.length; i++) {
        const ingredientGroup = this.ingredientsArray.at(i) as FormGroup;
        Object.keys(ingredientGroup.controls).forEach(key => {
          ingredientGroup.get(key)?.enable();
        });
      }

      for (let i = 0; i < this.stepsArray.length; i++) {
        const stepGroup = this.stepsArray.at(i) as FormGroup;
        Object.keys(stepGroup.controls).forEach(key => {
          stepGroup.get(key)?.enable();
        });
      }
    } else {
      // Disable all form controls when exiting edit mode
      Object.keys(this.recipeForm.controls).forEach(key => {
        const control = this.recipeForm.get(key);
        if (control && !(control instanceof FormArray)) {
          control.disable();
        }
      });

      // Disable controls in form arrays
      for (let i = 0; i < this.ingredientsArray.length; i++) {
        const ingredientGroup = this.ingredientsArray.at(i) as FormGroup;
        Object.keys(ingredientGroup.controls).forEach(key => {
          ingredientGroup.get(key)?.disable();
        });
      }

      for (let i = 0; i < this.stepsArray.length; i++) {
        const stepGroup = this.stepsArray.at(i) as FormGroup;
        Object.keys(stepGroup.controls).forEach(key => {
          stepGroup.get(key)?.disable();
        });
      }

      // Reset form to original values when canceling edit
      if (this.recipe) {
        this.updateFormWithRecipe(this.recipe);
      }
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

    // Get raw values from form since some controls may be disabled
    const formValue = this.recipeForm.getRawValue();

    // Process ingredients to ensure proper mapping
    formValue.ingredients = formValue.ingredients.map((ingredient: any) => {
      return {
        ...ingredient,
        type: ingredient.type,  // Use the type value from the hidden input
        // Remove the typeSelector property as it's not needed on the backend
        typeSelector: undefined
      };
    });

    const updatedRecipe: RecipeDTO = {
      ...formValue,
      id: this.recipeId
    };

    this.adminService.updateRecipe(this.recipeId, updatedRecipe)
      .pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: (recipe) => {
          this.recipe = recipe;
          this.successMessage = 'Recipe updated successfully';
          this.isEditing = false;
          this.toggleEditMode(); // This will disable the form controls
        },
        error: (error) => {
          console.error('Error updating recipe:', error);
          this.error = 'Failed to update recipe. Please try again.';
        }
      });
  }

  // Helper function for the searchable select component
  onIngredientTypeChange(index: number, option: SelectOption | null): void {
    if (option) {
      const ingredientGroup = this.ingredientsArray.at(index) as FormGroup;
      ingredientGroup.get('type')?.setValue(option.value);

      // Also update the name if it's empty or matches the previous type
      const currentName = ingredientGroup.get('name')?.value;
      const type = this.ingredientTypes.find(t => t.name === option.value);
      if (!currentName || this.getIngredientTypeDisplayName(ingredientGroup.get('type')?.value) === currentName) {
        ingredientGroup.get('name')?.setValue(type?.displayName || option.label);
      }
    }
  }

  // Helper function to get display name from ingredient type
  getIngredientTypeDisplayName(typeName: string): string {
    const type = this.ingredientTypes.find(t => t.name === typeName);
    return type?.displayName || typeName;
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

