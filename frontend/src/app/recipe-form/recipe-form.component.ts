import {Component, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {IngredientService} from "../../shared/services/ingredient.service";
import {IngredientType} from "../../shared/models/ingredient-type.model";
import {finalize, forkJoin, of} from "rxjs";
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
export class RecipeFormComponent implements OnInit {
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
  isEditMode = false;
  recipeId: number | null = null;

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

  ngOnInit(): void {
    this.isLoading = true;

    // Check if we're in edit mode
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEditMode = true;
        this.recipeId = +id;
      }
    });

    // Load ingredients, units, and recipe data if in edit mode
    const observables = {
      categories: this.ingredientService.getCategories(),
      ingredientsByCategory: this.ingredientService.getIngredientsByCategory(),
      unitCategories: this.unitService.getCategories(),
      unitsByCategory: this.unitService.getUnitsByCategory()
    };

    // If editing, add recipe data to observables
    if (this.isEditMode && this.recipeId) {
      forkJoin({
        ...observables,
        recipe: this.recipeService.getRecipeById(this.recipeId)
      }).pipe(
        finalize(() => this.isLoading = false)
      ).subscribe({
        next: (results) => {
          this.setupFormData(results);
          this.populateForm(results.recipe);
        },
        error: (error) => {
          console.error('Failed to load data', error);
          this.saveError = 'Failed to load recipe data. Please try again.';
        }
      });
    } else {
      // Just load ingredients and units for a new recipe
      forkJoin(observables).pipe(
        finalize(() => this.isLoading = false)
      ).subscribe({
        next: (results) => {
          this.setupFormData(results);
        },
        error: (error) => {
          console.error('Failed to load ingredient data', error);
          this.saveError = 'Failed to load ingredients. Please refresh and try again.';
        }
      });
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

  onSubmit(): void {
    this.submitted = true;

    if (this.recipeForm.invalid) {
      return;
    }

    this.isLoading = true;
    this.saveError = '';

    // First upload the image if there is one
    if (this.imageFile) {
      this.recipeService.uploadImage(this.imageFile).subscribe({
        next: (imageUrl) => {
          this.saveRecipe(imageUrl);
        },
        error: (error) => {
          console.error('Failed to upload image', error);
          this.saveError = 'Failed to upload image. Please try again.';
          this.isLoading = false;
        }
      });
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
        console.error('Error parsing image URL', e);
      }
    }

    // Prepare recipe data from form values
    const formValue = this.recipeForm.value;

    const recipeData: RecipeDTO = {
      id: this.isEditMode && this.recipeId ? this.recipeId : undefined,
      title: formValue.title,
      description: formValue.description,
      imageUrl: imageUrl,
      ingredients: formValue.ingredients.map((ingredient: Ingredient) => {
        const formattedType = ingredient.type.toUpperCase().replace(/-/g, '_');
        const formattedName = ingredient.type.toLowerCase()
          .replace(/_/g, ' ')
          .split(' ')
          .map(word => word.charAt(0).toUpperCase() + word.slice(1))
          .join(' ');

        return {
          type: formattedType,
          amount: ingredient.amount,
          unit: ingredient.unit,
          name: formattedName
        };
      }),
      steps: formValue.steps.map((step: RecipeStep) => ({
        stepNumber: step.stepNumber,
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

    saveOperation.pipe(
      finalize(() => this.isLoading = false)
    ).subscribe({
      next: () => {
        this.router.navigate(['']);
      },
      error: (error) => {
        console.error('Failed to save recipe', error);
        this.saveError = 'Failed to save recipe. Please try again.';
      }
    });
  }

  formatUnitName(unitName: string): string {
    return this.unitService.formatUnitName(unitName);
  }
}






