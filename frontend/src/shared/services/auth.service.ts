import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpResponse} from '@angular/common/http';
import {BehaviorSubject, catchError, Observable, of, Subject, Subscription, take, timer} from 'rxjs';
import {User} from "../models/user.model";
import {Router} from "@angular/router";
import {environment} from "../../environments/environment";
import {map, tap} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private userSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);
  public user$: Observable<User> = this.userSubject.asObservable();
  private isAdminSubject: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  public isAdmin$: Observable<boolean> = this.isAdminSubject.asObservable();

  private logoutTimerSubscription: Subscription | null = null;
  private readonly INACTIVITY_TIMEOUT = 60 * 60 * 1000;
  private activitySubject = new Subject<void>();


  constructor(
    private http: HttpClient,
    private router: Router,
  ) {
    // Check if user is logged in from sessionStorage
    const token = this.getToken();
    const username = sessionStorage.getItem('profileName');
    if (token && username) {
      this.userSubject.next({ username, token });
      this.checkForAdminRole(token);
    }
  }

  getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  login(username: string, password: string): Observable<User | null> {
    return this.http.post(`${environment.API_URL}/login`,
      { username, password },
      { observe: 'response', responseType: 'text' }
    ).pipe(
      map((response: HttpResponse<string>) => {
        const authHeader = response.headers.get('Authorization');
        if (response.ok && response.body && authHeader) {
          const token = authHeader.startsWith('Bearer ')
            ? authHeader.substring(7) // Remove 'Bearer ' from the start
            : authHeader;

          const userObj = {
            username: response.body,
            token: token
          } as User;
          sessionStorage.setItem('token', token);
          sessionStorage.setItem('profileName', userObj.username);
          this.userSubject.next(userObj);
          this.checkForAdminRole(token);
          this.startLogOutTimer();
          return userObj;
        }
        throw new Error('Login failed');
      }),
      catchError((error) => {
        console.error('Login error:', error);
        return of(null);
      })
    );
  }

  register(registrationRequest: {
    username: string;
    firstName: string;
    lastName: string;
    email: string;
    password: string;
  }): Observable<string | null> {
    return this.http.post(`${environment.API_URL}/register`,
      registrationRequest,
      { observe: 'response', responseType: 'text' }
    ).pipe(
      map((response: HttpResponse<string>) => {
        if (response.ok && response.body) {
          sessionStorage.setItem('profileName', response.body);
          return response.body;
        }
        throw new Error('Registration failed');
      }),
      catchError((error) => {
        console.error('Registration error:', error);
        return of(null);
      })
    );
  }

  signOut(): Observable<boolean> {
    const token = sessionStorage.getItem('token');
    if (!token) {
      console.log('No token found');
      return of(false);
    }

    if (this.logoutTimerSubscription) {
      this.logoutTimerSubscription.unsubscribe();
      this.logoutTimerSubscription = null;
    }

    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    return this.http.post(`${environment.API_URL}/signout`,
      {},
      { headers, observe: 'response', responseType: 'text' }
    ).pipe(
      map((response) => {
        if (response.ok) {
          this.userSubject.next(null);
          this.isAdminSubject.next(false);
          sessionStorage.clear();
          return true;
        }
        throw new Error('Logout failed');
      }),
      catchError((error) => {
        console.error('Logout error:', error);
        return of(false);
      })
    );
  }

  startLogOutTimer(): void {
    // Cancel any existing timer
    this.resetLogoutTimer();

    // Start a new timer
    this.logoutTimerSubscription = timer(this.INACTIVITY_TIMEOUT).pipe(
      take(1)
    ).subscribe(() => {
      console.log('Inactivity timeout reached, logging out');
      this.signOut().subscribe(() => {
        this.router.navigate(['/login']);
      });
    });

    // Subscribe to activity events to reset the timer
    this.activitySubject.subscribe(() => {
      this.resetLogoutTimer();
    });
  }

  // Add this new method
  resetLogoutTimer(): void {
    // Cancel any existing timer
    if (this.logoutTimerSubscription) {
      this.logoutTimerSubscription.unsubscribe();
      this.logoutTimerSubscription = null;
    }

    // Start a new timer
    this.logoutTimerSubscription = timer(this.INACTIVITY_TIMEOUT).pipe(
      take(1)
    ).subscribe(() => {
      console.log('Inactivity timeout reached, logging out');
      this.signOut().subscribe(() => {
        this.router.navigate(['/login']);
      });
    });
  }

  recordActivity(): void {
    this.activitySubject.next();
  }

  getToken(): string | null {
    return sessionStorage.getItem('token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  refreshUserData(): Observable<User> {
    return this.http.get<User>(`${environment.API_URL}/api/profile`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(user => {
        const currentUser = this.userSubject.getValue();
        if (currentUser) {
          this.userSubject.next({
            ...currentUser,
            firstName: user.firstName,
            lastName: user.lastName,
            email: user.email
          });
        }
      })
    );
  }

  // New method to check for admin role in JWT token
  private checkForAdminRole(token: string): void {
    try {
      const tokenParts = token.split('.');
      if (tokenParts.length === 3) {
        const payload = JSON.parse(atob(tokenParts[1]));
        console.log('JWT payload:', payload);

        // Define an interface for authority objects
        interface Authority {
          authority: string;
        }

        let hasAdminRole = false;

        // Check if auth is an array of objects with authority property
        if (Array.isArray(payload.auth)) {
          hasAdminRole = payload.auth.some((auth: any) =>
            (typeof auth === 'string' && auth === 'ROLE_ADMIN') ||
            (auth && auth.authority && auth.authority === 'ROLE_ADMIN')
          );
        }

        // Check other possible formats
        if (!hasAdminRole) {
          hasAdminRole =
            (Array.isArray(payload.authorities) && payload.authorities.some((auth: Authority) => auth.authority === 'ROLE_ADMIN')) ||
            payload.roles === 'ADMIN' ||
            (typeof payload.auth === 'string' && payload.auth.includes('ADMIN'));
        }

        console.log('Has admin role:', hasAdminRole);
        this.isAdminSubject.next(hasAdminRole);
        return;
      }
    } catch (error) {
      console.error('Error parsing JWT token:', error);
    }

    this.isAdminSubject.next(false);
  }

  // Method to check if user is admin - used by other services
  checkAdminRole(): Observable<boolean> {
    console.log('Checking admin role');

    // First check if we already know if user is admin
    const currentValue = this.isAdminSubject.getValue();
    if (currentValue) {
      console.log('Already know user is admin');
      return of(true);
    }

    // If not, make a backend call to check
    console.log('Making backend call to check admin status');
    return this.http.get<{isAdmin: boolean}>(`${environment.API_URL}/api/admin/check-role`, {
      headers: this.getAuthHeaders()
    }).pipe(
      map(response => {
        console.log('Admin check response:', response);
        const isAdmin = response.isAdmin;
        this.isAdminSubject.next(isAdmin);
        return isAdmin;
      }),
      catchError(error => {
        console.error('Admin check error:', error);
        this.isAdminSubject.next(false);
        return of(false);
      })
    );
  }
}

