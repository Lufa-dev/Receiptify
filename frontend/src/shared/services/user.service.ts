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
    // Using real API instead of mock
    return this.http.get<User>(this.apiUrl, {
      headers: this.getAuthHeaders()
    });
  }

  updateProfile(userData: any): Observable<any> {
    // Using real API instead of mock
    return this.http.put(`${this.apiUrl}`, userData, {
      headers: this.getAuthHeaders()
    });
  }
}
