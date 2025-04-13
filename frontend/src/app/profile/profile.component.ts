import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { UserService } from '../../shared/services/user.service';
import { AuthService } from '../../shared/services/auth.service';
import { RecipeService } from '../../shared/services/recipe.service';
import {finalize, Observable, of} from 'rxjs';

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
  isLoading = false;
  isSubmitting = false;
  submitted = false;
  passwordChangeAttempted = false;
  successMessage = '';
  errorMessage = '';

  recipeStats = {
    total: 0,
    thisMonth: 0,
    topIngredient: ''
  };

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private authService: AuthService,
    private recipeService: RecipeService
  ) {
    this.profileForm = this.createForm();
  }

  ngOnInit(): void {
    this.loadUserProfile();
    this.loadRecipeStats();
  }

  // Getter for easy access to form fields
  get f() {
    return this.profileForm.controls;
  }

  createForm(): FormGroup {
    const form = this.fb.group({
      username: ['', { disabled: true }],
      email: ['', [Validators.required, Validators.email]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      currentPassword: [''],
      newPassword: ['', Validators.minLength(6)],
      confirmPassword: ['']
    }, { validators: passwordMatchValidator });

    return form;
  }

  loadUserProfile(): void {
    this.isLoading = true;
    this.userService.getUserProfile()
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (user) => {
          this.profileForm.patchValue({
            username: user.username,
            email: user.email,
            firstName: user.firstName,
            lastName: user.lastName
          });
        },
        error: (error) => {
          console.error('Error loading profile:', error);
          this.errorMessage = 'Failed to load profile information. Please try again.';
        }
      });
  }

  loadRecipeStats(): void {
    this.recipeService.getUserRecipeStats()
      .subscribe({
        next: (stats) => {
          this.recipeStats = stats;
        },
        error: (error) => {
          console.error('Error loading recipe stats:', error);
        }
      });
  }

  onSubmit(): void {
    this.submitted = true;

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

    const formData = {
      email: this.profileForm.get('email')?.value,
      firstName: this.profileForm.get('firstName')?.value,
      lastName: this.profileForm.get('lastName')?.value,
      // Only include password fields if attempting password change
      ...(this.passwordChangeAttempted && {
        currentPassword: this.profileForm.get('currentPassword')?.value,
        newPassword: this.profileForm.get('newPassword')?.value
      })
    };

    this.userService.updateProfile(formData)
      .pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: () => {
          this.successMessage = 'Profile updated successfully!';
          this.errorMessage = '';
          this.submitted = false;
          this.passwordChangeAttempted = false;

          // Reset password fields
          this.profileForm.patchValue({
            currentPassword: '',
            newPassword: '',
            confirmPassword: ''
          });

          // Refresh user data in auth service if needed
          this.authService.refreshUserData();
        },
        error: (error) => {
          console.error('Error updating profile:', error);
          this.errorMessage = error?.error?.message || 'Failed to update profile. Please try again.';
          this.successMessage = '';
        }
      });
  }
}
