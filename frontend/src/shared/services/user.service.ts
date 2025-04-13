import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {Observable, of} from 'rxjs';
import { User } from '../models/user.model';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.API_URL}/api/profile`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  private getAuthHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`
    });
  }

  getUserProfile(): Observable<User> {
    // Temporary mock implementation until backend is ready
    const username = sessionStorage.getItem('profileName') || '';
    return of({
      username: username,
      token: this.authService.getToken() || '',
      email: `${username}@example.com`,
      firstName: 'John',
      lastName: 'Doe'
    } as User);

    // Uncomment this when backend is ready
    // return this.http.get<User>(this.apiUrl, {
    //   headers: this.getAuthHeaders()
    // });
  }

  updateProfile(userData: any): Observable<any> {
    // Temporary mock implementation
    console.log('Profile update requested with data:', userData);
    return of({ success: true });

    // Uncomment this when backend is ready
    // return this.http.put(`${this.apiUrl}`, userData, {
    //   headers: this.getAuthHeaders()
    // });
  }
}
