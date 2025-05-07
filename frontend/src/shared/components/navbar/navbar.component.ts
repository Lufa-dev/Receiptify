import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import {AdminService} from "../../services/admin.service";

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit, OnDestroy {
  isLoggedIn$ = false;
  isAdmin = false;
  private subscriptions: Subscription[] = [];

  constructor(
    private authService: AuthService,
    private cd: ChangeDetectorRef,
    private router: Router
  ) {
    this.isLoggedIn$ = this.authService.isLoggedIn();

    // Subscribe to authentication state changes
    const authSub = this.authService.user$.subscribe({
      next: (user) => {
        this.isLoggedIn$ = !!user;
        this.cd.markForCheck();
      }
    });
    this.subscriptions.push(authSub);

    // Subscribe to admin role changes
    const adminSub = this.authService.isAdmin$.subscribe({
      next: (isAdmin) => {
        this.isAdmin = isAdmin;
        this.cd.markForCheck();
      }
    });
    this.subscriptions.push(adminSub);
  }

  ngOnInit(): void {
    // If user is logged in, check admin status on init
    if (this.isLoggedIn$) {
      const adminCheckSub = this.authService.checkAdminRole().subscribe();
      this.subscriptions.push(adminCheckSub);
    }
  }

  ngOnDestroy(): void {
    // Clean up all subscriptions
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  logout() {
    const logoutSub = this.authService.signOut().subscribe({
      next: (success) => {
        if (success) {
          this.router.navigate(['/login']);
        }
        this.cd.markForCheck();
      },
      error: (error) => {
        this.cd.markForCheck();
      }
    });
    this.subscriptions.push(logoutSub);
  }
}



