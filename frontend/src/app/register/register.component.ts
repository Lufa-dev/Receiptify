import { Component } from '@angular/core';
import { AuthService } from '../../shared/services/auth.service';
import { User } from '../../shared/models/user.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  firstName: string = '';
  lastName: string = '';
  email: string = '';
  password: string = '';
  confirmPassword: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  async onSubmit() {
    if (this.password !== this.confirmPassword) {
      alert('Passwords do not match.');
      return;
    }

    const user: User = {
      firstName: this.firstName,
      lastName: this.lastName,
      email: this.email,
      password: this.password
    };

    try {
      await this.authService.register(user);
      this.router.navigate(['/']);
    } catch (error) {
      console.error('Registration error:', error);
      alert('Registration failed. Please try again.');
    }
  }
}
