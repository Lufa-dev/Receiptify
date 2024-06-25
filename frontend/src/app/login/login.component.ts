import { Component } from '@angular/core';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  email: string = '';
  password: string = '';

  constructor(private authService: AuthService) {}

  onSubmit() {
    if (this.email && this.password) {
      this.authService.login(this.email, this.password).catch((error: any) => {
        console.error('Login error:', error);
        alert('Login failed. Please try again.');
      });
    } else {
      alert('Please enter email and password.');
    }
  }
}
