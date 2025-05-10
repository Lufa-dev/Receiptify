import {Component, OnDestroy} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators} from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../shared/services/auth.service';
import {Subscription} from "rxjs";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnDestroy{
  registerForm: FormGroup;
  error: string = '';
  loading: boolean = false;
  private subscriptions: Subscription[] = [];

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.formBuilder.group({
      username: ['', [Validators.required]],
      firstName: [''],
      lastName: [''],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  // Custom validator to check if password and confirm password match
  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');

    if (password?.value !== confirmPassword?.value) {
      confirmPassword?.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    } else {
      // If there's an error, manually clear it
      // This ensures that if user corrects the mismatch, the error is removed
      if (confirmPassword?.errors) {
        const errors = { ...confirmPassword.errors };
        delete errors['passwordMismatch'];
        confirmPassword.setErrors(Object.keys(errors).length ? errors : null);
      }
      return null;
    }
  }

  onSubmit() {
    if (this.registerForm.invalid) {
      // Mark all fields as touched to trigger validation display
      Object.keys(this.registerForm.controls).forEach(key => {
        const control = this.registerForm.get(key);
        control?.markAsTouched();
      });
      return;
    }

    this.loading = true;
    this.error = '';

    // Extract form values without the confirmPassword field
    const { confirmPassword, ...registrationData } = this.registerForm.value;

    const registerSub = this.authService.register(registrationData).subscribe({
      next: (username) => {
        if (username) {
          this.router.navigate(['/login']);
        } else {
          this.error = 'Registration failed';
          this.loading = false;
        }
      },
      error: (error) => {
        this.error = error.message || 'An error occurred during registration';
        this.loading = false;
      }
    });
    this.subscriptions.push(registerSub);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }
}
