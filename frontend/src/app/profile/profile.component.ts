import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { UserService } from '../../shared/services/user.service';
import { AuthService } from '../../shared/services/auth.service';
import { RecipeService } from '../../shared/services/recipe.service';
import {finalize, Observable, of} from 'rxjs';
import {Router} from "@angular/router";
import {SelectOption} from "../../shared/components/searchable-select/searchable-select.component";
import {RECIPE_CATEGORIES, RECIPE_CUISINES} from "../../shared/constants/recipe-options";
import {IngredientService} from "../../shared/services/ingredient.service";

// Password match validator
function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('newPassword');
  const confirmPassword = control.get('confirmPassword');

  if (password?.value && confirmPassword?.value && password.value !== confirmPassword.value) {
    confirmPassword?.setErrors({ matching: true });
    return { matching: true };
  }

  return null;
}

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  profileForm: FormGroup;
  preferencesForm: FormGroup;
  isLoading = false;
  isSubmitting = false;
  submitted = false;
  passwordChangeAttempted = false;
  successMessage = '';
  errorMessage = '';

  // Options for select dropdowns
  categoryOptions: SelectOption[] = [];
  cuisineOptions: SelectOption[] = [];
  ingredientOptions: SelectOption[] = [];

  // Selected items arrays
  selectedCategories: SelectOption[] = [];
  selectedCuisines: SelectOption[] = [];
  selectedFavoriteIngredients: SelectOption[] = [];
  selectedDislikedIngredients: SelectOption[] = [];

  // For the slider display
  prepTimeValue: number = 30;

  recipeStats = {
    total: 0,
    thisMonth: 0,
    topIngredient: ''
  };

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private authService: AuthService,
    private recipeService: RecipeService,
    private ingredientService: IngredientService,
    private router: Router
  ) {
    this.profileForm = this.createProfileForm();
    this.preferencesForm = this.createPreferencesForm();
  }

  ngOnInit(): void {
    // Check if user is logged in
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadUserProfile();
    this.loadRecipeStats();
    this.initializeSelectOptions();
  }

  // Getter for easy access to form fields
  get f() {
    return this.profileForm.controls;
  }

  createProfileForm(): FormGroup {
    return this.fb.group({
      username: [{ value: '', disabled: true }],
      email: ['', [Validators.required, Validators.email]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      currentPassword: [''],
      newPassword: ['', Validators.minLength(6)],
      confirmPassword: ['']
    }, { validators: passwordMatchValidator });
  }

  createPreferencesForm(): FormGroup {
    return this.fb.group({
      preferredCategories: [[]],
      preferredCuisines: [[]],
      favoriteIngredients: [[]],
      dislikedIngredients: [[]],
      maxPrepTime: [30], // Default value
      difficultyPreference: [''],
      preferSeasonalRecipes: [false]
    });
  }

  initializeSelectOptions() {
    // Initialize category options
    this.categoryOptions = RECIPE_CATEGORIES.map(category => ({
      label: category.label,
      value: category.value
    }));

    // Initialize cuisine options
    this.cuisineOptions = RECIPE_CUISINES.map(cuisine => ({
      label: cuisine.label,
      value: cuisine.value
    }));

    // Initialize ingredient options from service
    this.ingredientService.getIngredientsByCategory().subscribe({
      next: (ingredientsByCategory) => {
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

        // Sort ingredient options alphabetically within each group
        this.ingredientOptions.sort((a, b) => {
          // Get groups, defaulting to empty string if undefined
          const groupA = a.group || '';
          const groupB = b.group || '';

          if (groupA === groupB) {
            return a.label.localeCompare(b.label);
          }
          return groupA.localeCompare(groupB);
        });
      },
      error: (error) => {
        console.error('Error loading ingredients:', error);
      }
    });
  }

  loadUserProfile(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.userService.getUserProfile()
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (user) => {
          console.log('User profile loaded:', user);

          // Patch form with user data
          this.profileForm.patchValue({
            username: user.username || '',
            email: user.email || '',
            firstName: user.firstName || '',
            lastName: user.lastName || ''
          });

          // Load user preferences
          this.userService.getUserPreferences().subscribe({
            next: (preferences) => {
              // Populate preference form
              this.preferencesForm.patchValue({
                preferredCategories: preferences.preferredCategories || [],
                preferredCuisines: preferences.preferredCuisines || [],
                favoriteIngredients: preferences.favoriteIngredients || [],
                dislikedIngredients: preferences.dislikedIngredients || [],
                maxPrepTime: preferences.maxPrepTime || 30,
                difficultyPreference: preferences.difficultyPreference || '',
                preferSeasonalRecipes: preferences.preferSeasonalRecipes || false
              });

              // Update slider display value
              this.prepTimeValue = preferences.maxPrepTime || 30;

              // Update the selected items for visual display
              this.populateSelectedPreferences(preferences);
            },
            error: (error) => {
              console.error('Error loading user preferences:', error);
            }
          });
        },
        error: (error) => {
          console.error('Error loading profile:', error);
          this.errorMessage = 'Failed to load profile information. Please try again.';

          // If 401 unauthorized, redirect to login
          if (error.status === 401) {
            setTimeout(() => this.router.navigate(['/login']), 1500);
          }
        }
      });
  }

  populateSelectedPreferences(preferences: any): void {
    // Populate selected categories
    if (preferences.preferredCategories && preferences.preferredCategories.length) {
      this.selectedCategories = preferences.preferredCategories.map((categoryValue: string) => {
        const option = this.categoryOptions.find(opt => opt.value === categoryValue);
        return option || { label: categoryValue, value: categoryValue };
      });
    }

    // Populate selected cuisines
    if (preferences.preferredCuisines && preferences.preferredCuisines.length) {
      this.selectedCuisines = preferences.preferredCuisines.map((cuisineValue: string) => {
        const option = this.cuisineOptions.find(opt => opt.value === cuisineValue);
        return option || { label: cuisineValue, value: cuisineValue };
      });
    }

    // Populate favorite ingredients
    if (preferences.favoriteIngredients && preferences.favoriteIngredients.length) {
      this.selectedFavoriteIngredients = preferences.favoriteIngredients.map((ingredientValue: string) => {
        const option = this.ingredientOptions.find(opt => opt.value === ingredientValue);
        // If not found, create a temporary option with the value as both label and value
        return option || {
          label: this.formatEnumName(ingredientValue),
          value: ingredientValue,
          group: 'Other'
        };
      });
    }

    // Populate disliked ingredients
    if (preferences.dislikedIngredients && preferences.dislikedIngredients.length) {
      this.selectedDislikedIngredients = preferences.dislikedIngredients.map((ingredientValue: string) => {
        const option = this.ingredientOptions.find(opt => opt.value === ingredientValue);
        // If not found, create a temporary option with the value as both label and value
        return option || {
          label: this.formatEnumName(ingredientValue),
          value: ingredientValue,
          group: 'Other'
        };
      });
    }
  }

  formatEnumName(enumName: string): string {
    if (!enumName) return '';

    return enumName
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
      .join(' ');
  }

  loadRecipeStats(): void {
    this.recipeService.getUserRecipeStats()
      .subscribe({
        next: (stats) => {
          this.recipeStats = stats;
          console.log('Recipe stats loaded:', stats);
        },
        error: (error) => {
          console.error('Error loading recipe stats:', error);
          // Set default values in case of error
          this.recipeStats = {
            total: 0,
            thisMonth: 0,
            topIngredient: ''
          };
        }
      });
  }

  onSubmit(): void {
    this.submitted = true;
    this.successMessage = '';
    this.errorMessage = '';

    // Check if password fields have values
    const currentPassword = this.profileForm.get('currentPassword')?.value;
    const newPassword = this.profileForm.get('newPassword')?.value;

    if (currentPassword || newPassword) {
      this.passwordChangeAttempted = true;

      // If attempting to change password, validate current password
      if (!currentPassword) {
        this.profileForm.get('currentPassword')?.setErrors({ required: true });
        return;
      }

      // Only validate confirm password if new password is provided
      if (newPassword && this.profileForm.hasError('matching')) {
        return;
      }
    }

    if (this.profileForm.invalid) {
      return;
    }

    this.isSubmitting = true;

    // Create form data
    const formData: any = {
      email: this.profileForm.get('email')?.value,
      firstName: this.profileForm.get('firstName')?.value,
      lastName: this.profileForm.get('lastName')?.value
    };

    // Only include password fields if attempting password change
    if (this.passwordChangeAttempted) {
      formData.currentPassword = this.profileForm.get('currentPassword')?.value;
      formData.newPassword = this.profileForm.get('newPassword')?.value;
    }

    console.log('Updating profile with data:', formData);

    this.userService.updateProfile(formData)
      .pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: (response) => {
          console.log('Profile updated successfully:', response);
          this.successMessage = 'Profile updated successfully!';
          this.submitted = false;
          this.passwordChangeAttempted = false;

          // Reset password fields
          this.profileForm.patchValue({
            currentPassword: '',
            newPassword: '',
            confirmPassword: ''
          });

          // Refresh user data in auth service
          this.authService.refreshUserData().subscribe({
            next: (updatedUser) => console.log('User data refreshed:', updatedUser),
            error: (err) => console.error('Error refreshing user data:', err)
          });
        },
        error: (error) => {
          console.error('Error updating profile:', error);

          if (error.status === 401) {
            this.errorMessage = 'Your session has expired. Please log in again.';
            setTimeout(() => this.router.navigate(['/login']), 1500);
          } else if (error.status === 400 && error.error?.message?.includes('password')) {
            this.errorMessage = 'Current password is incorrect.';
          } else {
            this.errorMessage = error?.error?.message || 'Failed to update profile. Please try again.';
          }
        }
      });
  }

  savePreferences(): void {
    if (this.preferencesForm.invalid) {
      return;
    }

    this.isSubmitting = true;

    // Get values from the form
    const preferences = {
      ...this.preferencesForm.value,
      // Override with selected items values (for badges UI)
      preferredCategories: this.selectedCategories.map(item => item.value),
      preferredCuisines: this.selectedCuisines.map(item => item.value),
      favoriteIngredients: this.selectedFavoriteIngredients.map(item => item.value),
      dislikedIngredients: this.selectedDislikedIngredients.map(item => item.value)
    };

    this.userService.updatePreferences(preferences)
      .pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: () => {
          this.successMessage = 'Preferences updated successfully!';

          // Scroll to the top to see the message
          window.scrollTo({ top: 0, behavior: 'smooth' });

          // Clear message after a delay
          setTimeout(() => {
            this.successMessage = '';
          }, 3000);
        },
        error: (error) => {
          console.error('Error updating preferences:', error);
          this.errorMessage = 'Failed to update preferences. Please try again.';
        }
      });
  }

  // Update the displayed prep time value when slider changes
  updatePrepTimeLabel(): void {
    this.prepTimeValue = this.preferencesForm.get('maxPrepTime')?.value || 30;
  }

  // Reset preferences to default values
  resetPreferences(): void {
    this.preferencesForm.patchValue({
      preferredCategories: [],
      preferredCuisines: [],
      favoriteIngredients: [],
      dislikedIngredients: [],
      maxPrepTime: 30,
      difficultyPreference: '',
      preferSeasonalRecipes: false
    });

    // Clear selected items arrays
    this.selectedCategories = [];
    this.selectedCuisines = [];
    this.selectedFavoriteIngredients = [];
    this.selectedDislikedIngredients = [];

    // Update slider display value
    this.prepTimeValue = 30;
  }

  // Methods for handling selection changes
  addSelectedCategory(option: SelectOption | null): void {
    if (!option) return;

    if (!this.selectedCategories.some(item => item.value === option.value)) {
      this.selectedCategories.push(option);

      // Update the form control - ensure we're working with an array
      let currentValues = this.preferencesForm.get('preferredCategories')?.value;
      if (!Array.isArray(currentValues)) {
        currentValues = [];
      }

      this.preferencesForm.get('preferredCategories')?.setValue([
        ...currentValues,
        option.value
      ]);
    }
  }

  removeSelectedCategory(option: SelectOption): void {
    this.selectedCategories = this.selectedCategories.filter(item => item.value !== option.value);

    // Update the form control - ensure we're working with an array
    let currentValues = this.preferencesForm.get('preferredCategories')?.value;
    if (!Array.isArray(currentValues)) {
      currentValues = [];
    }

    this.preferencesForm.get('preferredCategories')?.setValue(
      currentValues.filter((value: string) => value !== option.value)
    );
  }

  addSelectedCuisine(option: SelectOption | null): void {
    if (!option) return;

    if (!this.selectedCuisines.some(item => item.value === option.value)) {
      this.selectedCuisines.push(option);

      // Update the form control
      const currentValues = this.preferencesForm.get('preferredCuisines')?.value || [];
      this.preferencesForm.get('preferredCuisines')?.setValue([
        ...currentValues,
        option.value
      ]);
    }
  }

  removeSelectedCuisine(option: SelectOption): void {
    this.selectedCuisines = this.selectedCuisines.filter(item => item.value !== option.value);

    // Update the form control
    const currentValues = this.preferencesForm.get('preferredCuisines')?.value || [];
    this.preferencesForm.get('preferredCuisines')?.setValue(
      currentValues.filter((value: string) => value !== option.value)
    );
  }

  addSelectedFavoriteIngredient(option: SelectOption | null): void {
    if (!option) return;

    if (!this.selectedFavoriteIngredients.some(item => item.value === option.value)) {
      this.selectedFavoriteIngredients.push(option);

      // Update the form control
      const currentValues = this.preferencesForm.get('favoriteIngredients')?.value || [];
      this.preferencesForm.get('favoriteIngredients')?.setValue([
        ...currentValues,
        option.value
      ]);
    }
  }

  removeSelectedFavoriteIngredient(option: SelectOption): void {
    this.selectedFavoriteIngredients = this.selectedFavoriteIngredients.filter(item => item.value !== option.value);

    // Update the form control
    const currentValues = this.preferencesForm.get('favoriteIngredients')?.value || [];
    this.preferencesForm.get('favoriteIngredients')?.setValue(
      currentValues.filter((value: string) => value !== option.value)
    );
  }

  addSelectedDislikedIngredient(option: SelectOption | null): void {
    if (!option) return;

    if (!this.selectedDislikedIngredients.some(item => item.value === option.value)) {
      this.selectedDislikedIngredients.push(option);

      // Update the form control
      const currentValues = this.preferencesForm.get('dislikedIngredients')?.value || [];
      this.preferencesForm.get('dislikedIngredients')?.setValue([
        ...currentValues,
        option.value
      ]);
    }
  }

  removeSelectedDislikedIngredient(option: SelectOption): void {
    this.selectedDislikedIngredients = this.selectedDislikedIngredients.filter(item => item.value !== option.value);

    // Update the form control
    const currentValues = this.preferencesForm.get('dislikedIngredients')?.value || [];
    this.preferencesForm.get('dislikedIngredients')?.setValue(
      currentValues.filter((value: string) => value !== option.value)
    );
  }
}







