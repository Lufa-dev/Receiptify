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
  private authSubscription: Subscription;
  private adminSubscription: Subscription;

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

    // Subscribe to admin role changes
    this.adminSubscription = this.authService.isAdmin$.subscribe({
      next: (isAdmin) => {
        this.isAdmin = isAdmin;
        this.cd.markForCheck();
      }
    });
  }

  ngOnInit(): void {
    // If user is logged in, check admin status on init
    if (this.isLoggedIn$) {
      this.authService.checkAdminRole().subscribe();
    }
  }

  ngOnDestroy(): void {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }

    if (this.adminSubscription) {
      this.adminSubscription.unsubscribe();
    }
  }

  logout() {
    this.authService.signOut().subscribe({
      next: (success) => {
        if (success) {
          this.router.navigate(['/login']);
        } else {
        }
        this.cd.markForCheck();
      },
      error: (error) => {
        this.cd.markForCheck();
      }
    });
  }
}


