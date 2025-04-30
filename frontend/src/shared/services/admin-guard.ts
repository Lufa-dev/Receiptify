import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from './auth.service';
import { Observable, of } from 'rxjs';
import {catchError, map, tap} from 'rxjs/operators';
import { AdminService } from './admin.service';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    // First check if user is logged in
    if (!this.authService.isLoggedIn()) {
      console.log('Admin guard: User not logged in');
      this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
      return of(false);
    }

    // Then check if user is an admin using the AuthService's checkAdminRole method
    return this.authService.checkAdminRole().pipe(
      tap(isAdmin => {
        if (!isAdmin) {
          console.log('Admin guard: User is not admin');
          this.router.navigate(['/']);
        }
      }),
      catchError(error => {
        console.error('Admin guard error:', error);
        this.router.navigate(['/']);
        return of(false);
      })
    );
  }
}


