import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit, OnDestroy {
  isLoggedIn$ = false;
  private authSubscription: Subscription;

  constructor(
    private authService: AuthService,
    private cd: ChangeDetectorRef,
    private router: Router
  ) {
    this.isLoggedIn$ = this.authService.isLoggedIn();

    // Subscribe to authentication state changes
    this.authSubscription = this.authService.user$.subscribe({
      next: (user) => {
        this.isLoggedIn$ = !!user;
        this.cd.markForCheck();
      }
    });
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
  }

  logout() {
    this.authService.signOut().subscribe({
      next: (success) => {
        if (success) {
          this.router.navigate(['/login']);
        } else {
          console.error('Failed to sign out');
        }
        this.cd.markForCheck();
      },
      error: (error) => {
        console.error('Logout error:', error);
        this.cd.markForCheck();
      }
    });
  }
}
