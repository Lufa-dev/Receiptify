import {Component, OnDestroy} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import { AuthService } from '../../shared/services/auth.service';
import {Subscription} from "rxjs";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnDestroy{
  loginForm: FormGroup;
  error: string = '';
  loading: boolean = false;
  returnUrl: string = '/';
  private subscriptions: Subscription[] = [];

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.loginForm = this.formBuilder.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });

    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  onSubmit() {
    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    this.error = '';

    const { username, password } = this.loginForm.value;

    const loginSub = this.authService.login(username, password).subscribe({
      next: (user) => {
        if (user) {
          this.router.navigate([this.returnUrl]);
        } else {
          this.error = 'Login failed';
          this.loading = false;
        }
      },
      error: (error) => {
        this.error = error.message || 'An error occurred during login';
        this.loading = false;
      }
    });
    this.subscriptions.push(loginSub);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }
}
