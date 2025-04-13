import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { UserService } from '../../shared/services/user.service';
import { AuthService } from '../../shared/services/auth.service';
import { RecipeService } from '../../shared/services/recipe.service';
import {finalize, Observable, of} from 'rxjs';
import {Router} from "@angular/router";

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
    private recipeService: RecipeService,
    private router: Router
  ) {
    this.profileForm = this.createForm();
  }

  ngOnInit(): void {
    // Check if user is logged in
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadUserProfile();
    this.loadRecipeStats();
  }

  // Getter for easy access to form fields
  get f() {
    return this.profileForm.controls;
  }

  createForm(): FormGroup {
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

  loadRecipeStats(): void {
    this.recipeService.getUserRecipeStats()
      .subscribe({
        next: (stats) => {
          this.recipeStats = stats;
        },
        error: (error) => {
          console.error('Error loading recipe stats:', error);
          // Don't show error to user since this is secondary information
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
}




