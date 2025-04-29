import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';
import { User } from '../models/user.model';
import { RecipeDTO } from '../models/recipe.model';
import { Comment } from '../models/comment.model';
import { PagedResponse } from '../models/paged-response.model';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = `${environment.API_URL}/api/admin`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getAuthHeaders(): HttpHeaders {
    return this.authService.getAuthHeaders();
  }

  // Check if current user has admin role
  isUserAdmin(): Observable<boolean> {
    return this.http.get<{isAdmin: boolean}>(`${this.apiUrl}/check-admin`, {
      headers: this.getAuthHeaders()
    }).pipe(
      map(response => response.isAdmin),
      catchError(() => of(false))
    );
  }

  // User management
  getAllUsers(page: number = 0, size: number = 10): Observable<PagedResponse<User>> {
    return this.http.get<PagedResponse<User>>(`${this.apiUrl}/users?page=${page}&size=${size}`, {
      headers: this.getAuthHeaders()
    });
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/users/${id}`, {
      headers: this.getAuthHeaders()
    });
  }

  updateUser(id: number, userData: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/users/${id}`, userData, {
      headers: this.getAuthHeaders()
    });
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/${id}`, {
      headers: this.getAuthHeaders()
    });
  }

  // Admin can promote a user to admin or demote an admin to regular user
  toggleAdminRole(id: number, makeAdmin: boolean): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/users/${id}/role`, { role: makeAdmin ? 'ADMIN' : 'USER' }, {
      headers: this.getAuthHeaders()
    });
  }

  // Recipe management
  getAllRecipes(page: number = 0, size: number = 10): Observable<PagedResponse<RecipeDTO>> {
    return this.http.get<PagedResponse<RecipeDTO>>(`${this.apiUrl}/recipes?page=${page}&size=${size}`, {
      headers: this.getAuthHeaders()
    });
  }

  getRecipeById(id: number): Observable<RecipeDTO> {
    return this.http.get<RecipeDTO>(`${this.apiUrl}/recipes/${id}`, {
      headers: this.getAuthHeaders()
    });
  }

  updateRecipe(id: number, recipeData: Partial<RecipeDTO>): Observable<RecipeDTO> {
    return this.http.put<RecipeDTO>(`${this.apiUrl}/recipes/${id}`, recipeData, {
      headers: this.getAuthHeaders()
    });
  }

  deleteRecipe(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/recipes/${id}`, {
      headers: this.getAuthHeaders()
    });
  }

  // Comment management
  getAllComments(page: number = 0, size: number = 10): Observable<PagedResponse<Comment>> {
    return this.http.get<PagedResponse<Comment>>(`${this.apiUrl}/comments?page=${page}&size=${size}`, {
      headers: this.getAuthHeaders()
    });
  }

  getCommentById(id: number): Observable<Comment> {
    return this.http.get<Comment>(`${this.apiUrl}/comments/${id}`, {
      headers: this.getAuthHeaders()
    });
  }

  updateComment(id: number, commentData: Partial<Comment>): Observable<Comment> {
    return this.http.put<Comment>(`${this.apiUrl}/comments/${id}`, commentData, {
      headers: this.getAuthHeaders()
    });
  }

  deleteComment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/comments/${id}`, {
      headers: this.getAuthHeaders()
    });
  }

  // Dashboard statistics
  getDashboardStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/dashboard/stats`, {
      headers: this.getAuthHeaders()
    });
  }

  // Search functionality
  searchUsers(query: string, page: number = 0, size: number = 10): Observable<PagedResponse<User>> {
    return this.http.get<PagedResponse<User>>(`${this.apiUrl}/users/search?query=${query}&page=${page}&size=${size}`, {
      headers: this.getAuthHeaders()
    });
  }

  searchRecipes(query: string, page: number = 0, size: number = 10): Observable<PagedResponse<RecipeDTO>> {
    return this.http.get<PagedResponse<RecipeDTO>>(`${this.apiUrl}/recipes/search?query=${query}&page=${page}&size=${size}`, {
      headers: this.getAuthHeaders()
    });
  }
}
