import {Component, HostListener, OnDestroy, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {IngredientService} from "../../shared/services/ingredient.service";
import {IngredientType} from "../../shared/models/ingredient-type.model";
import {finalize, forkJoin, of, Subscription, take} from "rxjs";
import {RecipeDTO} from "../../shared/models/recipe.model";
import {Ingredient} from "../../shared/models/ingredient.model";
import {UnitType} from "../../shared/models/unit-type.model";
import {RecipeStep} from "../../shared/models/recipe-step.model";
import {RecipeService} from "../../shared/services/recipe.service";
import {UnitService} from "../../shared/services/unit.service";
import {SelectOption} from "../../shared/components/searchable-select/searchable-select.component";

@Component({
  selector: 'app-recipe-form',
  templateUrl: './recipe-form.component.html',
  styleUrls: ['./recipe-form.component.scss']
})
export class RecipeFormComponent implements OnInit, OnDestroy {
  recipeForm: FormGroup;
  ingredientsByCategory: Record<string, IngredientType[]> = {};
  unitsByCategory: Record<string, UnitType[]> = {};
  categories: string[] = [];
  unitCategories: string[] = [];
  isLoading = false;
  imagePreview: string | ArrayBuffer | null = null;
  imageFile: File | null = null;
  submitted = false;
  saveError = '';
  validationErrors: string[] = []; // To store validation errors
  isEditMode = false;
  recipeId: number | null = null;
  private subscriptions: Subscription[] = [];
  private pendingSave = false;
  private pendingSaveSubscription: Subscription | null = null;

  // Select options for the searchable dropdown
  ingredientOptions: SelectOption[] = [];
  unitOptions: SelectOption[] = [];

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private recipeService: RecipeService,
    private ingredientService: IngredientService,
    private unitService: UnitService
  ) {
    this.recipeForm = this.createRecipeForm();
  }

  // Add this host listener to prevent navigating away if there's a pending save operation
  @HostListener('window:beforeunload', ['$event'])
  unloadNotification($event: any): void {
    if (this.pendingSave) {
      $event.returnValue = true;
    }
  }

  ngOnInit(): void {
    this.isLoading = true;

    // Check if we're in edit mode
    const routeSub = this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEditMode = true;
        this.recipeId = +id;
      }
    });
    this.subscriptions.push(routeSub);

    // Load ingredients, units, and recipe data if in edit mode
    const observables = {
      categories: this.ingredientService.getCategories(),
      ingredientsByCategory: this.ingredientService.getIngredientsByCategory(),
      unitCategories: this.unitService.getCategories(),
      unitsByCategory: this.unitService.getUnitsByCategory()
    };

    // If editing, add recipe data to observables
    if (this.isEditMode && this.recipeId) {
      const dataLoadSub = forkJoin({
        ...observables,
        recipe: this.recipeService.getRecipeById(this.recipeId)
      }).pipe(
        finalize(() => this.isLoading = false),
        take(1) // Ensure this completes after one emission
      ).subscribe({
        next: (results) => {
          this.setupFormData(results);
          this.populateForm(results.recipe);
        },
        error: (error) => {
          this.saveError = 'Failed to load recipe data. Please try again.';
        }
      });
      this.subscriptions.push(dataLoadSub);
    } else {
      // Just load ingredients and units for a new recipe
      const initDataSub = forkJoin(observables).pipe(
        finalize(() => this.isLoading = false),
        take(1) // Ensure this completes after one emission
      ).subscribe({
        next: (results) => {
          this.setupFormData(results);
        },
        error: (error) => {
          this.saveError = 'Failed to load ingredients. Please refresh and try again.';
        }
      });
      this.subscriptions.push(initDataSub);
    }
  }

  private setupFormData(results: any): void {
    this.categories = results.categories;
    this.ingredientsByCategory = results.ingredientsByCategory;
    this.unitCategories = results.unitCategories;
    this.unitsByCategory = results.unitsByCategory;

    // Convert ingredients to select options
    this.ingredientOptions = [];
    Object.entries(this.ingredientsByCategory).forEach(([category, ingredients]) => {
      ingredients.forEach(ingredient => {
        this.ingredientOptions.push({
          label: ingredient.displayName,
          value: ingredient.name.toUpperCase(),
          group: category
        });
      });
    });

    // Convert units to select options
    this.unitOptions = [];
    Object.entries(this.unitsByCategory).forEach(([category, units]) => {
      units.forEach(unit => {
        this.unitOptions.push({
          label: `${unit.symbol} ${unit.name !== unit.symbol ? '(' + this.formatUnitName(unit.name) + ')' : ''}`,
          value: unit.name,
          group: category
        });
      });
    });
  }

  private populateForm(recipe: RecipeDTO): void {
    if (!recipe) return;

    this.recipeForm.patchValue({
      title: recipe.title,
      description: recipe.description,
      imageUrl: recipe.imageUrl,
      category: recipe.category || '',
      cuisine: recipe.cuisine || '',
      servings: recipe.servings || null,
      difficulty: recipe.difficulty || '',
      costRating: recipe.costRating || '',
      prepTime: recipe.prepTime || null,
      cookTime: recipe.cookTime || null,
      bakingTime: recipe.bakingTime || null,
      bakingTemp: recipe.bakingTemp || null,
      panSize: recipe.panSize || null,
      bakingMethod: recipe.bakingMethod || ''
    });

    // Clear default ingredients and steps
    while (this.ingredients.length > 0) {
      this.ingredients.removeAt(0);
    }

    while (this.steps.length > 0) {
      this.steps.removeAt(0);
    }

    // Add ingredients from recipe
    if (recipe.ingredients && recipe.ingredients.length > 0) {
      recipe.ingredients.forEach(ingredient => {
        this.ingredients.push(this.fb.group({
          type: [ingredient.type, Validators.required],
          amount: [ingredient.amount],
          unit: [ingredient.unit],
          name: [ingredient.name]
        }));
      });
    } else {
      // Add one empty ingredient if none exist
      this.ingredients.push(this.createIngredientForm());
    }

    // Add steps from recipe
    if (recipe.steps && recipe.steps.length > 0) {
      recipe.steps.forEach(step => {
        this.steps.push(this.fb.group({
          stepNumber: [step.stepNumber],
          instruction: [step.instruction, [Validators.required, Validators.maxLength(1000)]]
        }));
      });
    } else {
      // Add one empty step if none exist
      this.steps.push(this.createStepForm(0));
    }

    // If recipe has an image, show preview
    if (recipe.imageUrl) {
      this.imagePreview = recipe.imageUrl;
    }
  }

  private createRecipeForm(): FormGroup {
    return this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', Validators.maxLength(500)],
      imageUrl: [''],
      ingredients: this.fb.array([this.createIngredientForm()]),
      steps: this.fb.array([this.createStepForm(0)]),
      category: [''],
      cuisine: [''],
      servings: [null],
      difficulty: [''],
      costRating: [''],
      prepTime: [null],
      cookTime: [null],
      bakingTime: [null],
      bakingTemp: [null],
      panSize: [null],
      bakingMethod: ['']
    });
  }

  private createIngredientForm(): FormGroup {
    return this.fb.group({
      type: ['', Validators.required],
      amount: [''],
      unit: [''],
      name: ['']
    });
  }

  private createStepForm(stepNumber: number): FormGroup {
    return this.fb.group({
      stepNumber: [stepNumber],
      instruction: ['', [Validators.required, Validators.maxLength(1000)]]
    });
  }

  get ingredients(): FormArray {
    return this.recipeForm.get('ingredients') as FormArray;
  }

  get steps(): FormArray {
    return this.recipeForm.get('steps') as FormArray;
  }

  addIngredient(): void {
    this.ingredients.push(this.createIngredientForm());
  }

  removeIngredient(index: number): void {
    if (this.ingredients.length > 1) {
      this.ingredients.removeAt(index);
    }
  }

  addStep(): void {
    const nextStepNumber = this.steps.length;
    this.steps.push(this.createStepForm(nextStepNumber));
  }

  removeStep(index: number): void {
    if (this.steps.length > 1) {
      this.steps.removeAt(index);

      // Renumber steps
      this.steps.controls.forEach((control, idx) => {
        control.get('stepNumber')?.setValue(idx);
      });
    }
  }

  onImageSelected(event: Event): void {
    const fileInput = event.target as HTMLInputElement;
    if (fileInput.files && fileInput.files[0]) {
      this.imageFile = fileInput.files[0];

      // Preview the image
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result;
      };
      reader.readAsDataURL(this.imageFile);
    }
  }

  // Validate ingredients to check if they have required fields
  validateIngredients(): boolean {
    let isValid = true;
    this.validationErrors = [];

    // Check each ingredient
    this.ingredients.controls.forEach((ingredientControl, index) => {
      const ingredient = ingredientControl.value;

      // Check if type is specified
      if (!ingredient.type) {
        this.validationErrors.push(`Ingredient #${index + 1}: Type is required`);
        isValid = false;
      }

      // Check if both amount and unit are provided
      if (!ingredient.amount || !ingredient.unit) {
        this.validationErrors.push(`Ingredient #${index + 1}: Both amount and unit are required`);
        isValid = false;
      }
    });

    return isValid;
  }


  onSubmit(): void {
    this.submitted = true;
    this.validationErrors = [];

    if (this.recipeForm.invalid) {
      // Collect validation errors
      if (this.recipeForm.get('title')?.invalid) {
        this.validationErrors.push('Title is required and must be less than 100 characters');
      }

      if (this.recipeForm.get('description')?.invalid) {
        this.validationErrors.push('Description must be less than 500 characters');
      }

      // Validate steps
      this.steps.controls.forEach((stepControl, index) => {
        if (stepControl.get('instruction')?.invalid) {
          this.validationErrors.push(`Step #${index + 1}: Instruction is required and must be less than 1000 characters`);
        }
      });

      return;
    }

    // Additional ingredient validation
    if (!this.validateIngredients()) {
      return;
    }

    this.isLoading = true;
    this.saveError = '';
    this.pendingSave = true;

    // First upload the image if there is one
    if (this.imageFile) {
      this.pendingSaveSubscription = this.recipeService.uploadImage(this.imageFile)
        .pipe(
          take(1),
          finalize(() => {
            if (!this.pendingSave) {
              this.isLoading = false;
            }
          })
        )
        .subscribe({
          next: (imageUrl) => {
            this.saveRecipe(imageUrl);
          },
          error: (error) => {
            this.pendingSave = false;
            this.isLoading = false;
            this.saveError = 'Failed to upload image. Please try again.';

            if (error.error && error.error.message) {
              this.saveError = error.error.message;
            }
          }
        });
      this.subscriptions.push(this.pendingSaveSubscription);
    } else {
      this.saveRecipe(this.recipeForm.value.imageUrl || '');
    }
  }

  private saveRecipe(imageUrl: string): void {
    // Prevent sending the entire JSON string as the imageUrl
    if (typeof imageUrl === 'string' && imageUrl.startsWith('{')) {
      try {
        const parsedImage = JSON.parse(imageUrl);
        imageUrl = parsedImage.imageUrl || '';
      } catch (e) {
        // If parsing fails, leave as is
      }
    }

    // Prepare recipe data from form values
    const formValue = this.recipeForm.value;

    // Format ingredients properly
    const formattedIngredients = formValue.ingredients.map((ingredient: Ingredient) => {
      let formattedType = ingredient.type;
      let formattedName = '';

      // Format type if it's a string
      if (typeof ingredient.type === 'string') {
        formattedType = ingredient.type.toUpperCase().replace(/-/g, '_');
        formattedName = ingredient.name || this.formatIngredientName(ingredient.type);
      }

      return {
        type: formattedType,
        amount: ingredient.amount,
        unit: ingredient.unit,
        name: formattedName
      };
    });

    const recipeData: RecipeDTO = {
      id: this.isEditMode && this.recipeId ? this.recipeId : undefined,
      title: formValue.title,
      description: formValue.description,
      imageUrl: imageUrl,
      ingredients: formattedIngredients,
      steps: formValue.steps.map((step: RecipeStep, index: number) => ({
        stepNumber: index, // Ensure steps are numbered correctly
        instruction: step.instruction
      })),
      category: formValue.category,
      cuisine: formValue.cuisine,
      servings: formValue.servings,
      difficulty: formValue.difficulty,
      costRating: formValue.costRating,
      prepTime: formValue.prepTime,
      cookTime: formValue.cookTime,
      bakingTime: formValue.bakingTime,
      bakingTemp: formValue.bakingTemp,
      panSize: formValue.panSize,
      bakingMethod: formValue.bakingMethod
    };

    // Use update or create based on edit mode
    const saveOperation = this.isEditMode && this.recipeId
      ? this.recipeService.updateRecipe(this.recipeId, recipeData)
      : this.recipeService.createRecipe(recipeData);

    const saveOpSub = saveOperation.pipe(
      take(1), // Ensure this completes after one emission
      finalize(() => {
        this.isLoading = false;
        this.pendingSave = false;
      })
    ).subscribe({
      next: () => {
        // Wait for the operation to complete before navigating
        if (!this.pendingSave) return;

        setTimeout(() => {
          this.router.navigate(['/my-recipes']);
        }, 100);
      },
      error: (error) => {
        this.saveError = 'Failed to save recipe. Please try again.';
        this.validationErrors = [];

        // Extract validation errors if available
        if (error.error && error.error.errors) {
          // Handle array of error messages
          if (Array.isArray(error.error.errors)) {
            this.validationErrors = error.error.errors;
          }
          // Handle object with error fields
          else if (typeof error.error.errors === 'object') {
            Object.entries(error.error.errors).forEach(([field, messages]) => {
              if (Array.isArray(messages)) {
                messages.forEach((message: string) => {
                  this.validationErrors.push(`${field}: ${message}`);
                });
              } else {
                this.validationErrors.push(`${field}: ${String(messages)}`);
              }
            });
          }
        }

        // If we got a specific error message
        if (error.error && error.error.message) {
          this.saveError = error.error.message;
        }
      }
    });
    this.pendingSaveSubscription = saveOpSub;
    this.subscriptions.push(saveOpSub);
  }

  // Format ingredient names for display
  formatIngredientName(type: string): string {
    if (!type) return '';

    // For enum values like "BELL_PEPPER" -> "Bell Pepper"
    return type
      .toLowerCase()
      .replace(/_/g, ' ')
      .split(' ')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  // Format unit names
  formatUnitName(unitName: string): string {
    return this.unitService.formatUnitName(unitName);
  }

  ngOnDestroy(): void {
    // Cancel any pending operations
    this.pendingSave = false;

    // Clear specific operation if it's still active
    if (this.pendingSaveSubscription) {
      this.pendingSaveSubscription.unsubscribe();
      this.pendingSaveSubscription = null;
    }

    // Unsubscribe from all subscriptions
    this.subscriptions.forEach(sub => {
      if (sub && !sub.closed) {
        sub.unsubscribe();
      }
    });
  }
}







