import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {  map } from 'rxjs/operators';
import { RecipeDTO } from '../models/recipe.model';
import {environment} from "../../environments/environment";
import {CollectionService} from "./collection.service";
import {AuthService} from "./auth.service";
import {RecipeSearchCriteria, SearchFilterOptions} from "../models/recipe-search-criteria.model";

@Injectable({
  providedIn: 'root'
})
export class RecipeService {
  private apiUrl = `${environment.API_URL}/api/recipes`;

  constructor(
    private http: HttpClient,
    private collectionService: CollectionService,
    private authService: AuthService) { }

  private getAuthHeaders(): HttpHeaders {
    const token = sessionStorage.getItem('token');

    if (!token) {
      console.warn('No token found in sessionStorage');
      return new HttpHeaders();
    }

    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  getAllRecipes(page: number = 0, size: number = 10): Observable<any> {
    return this.http.get(`${this.apiUrl}?page=${page}&size=${size}`);
  }

  getRecipeById(id: number, username?: string): Observable<RecipeDTO> {
    // If username is provided, pass it to the backend
    if (username) {
      const token = this.authService.getToken();
      if (token) {
        const headers = this.authService.getAuthHeaders();
        return this.http.get<RecipeDTO>(`${this.apiUrl}/${id}`, { headers });
      }
    }

    // Otherwise just get the recipe without user rating
    return this.http.get<RecipeDTO>(`${this.apiUrl}/${id}`);
  }

  getUserRecipes(page: number = 0, size: number = 10): Observable<any> {
    return this.http.get(`${this.apiUrl}/user?page=${page}&size=${size}`, {
      headers: this.getAuthHeaders()
    });
  }

  searchRecipes(query: string, page: number = 0, size: number = 10): Observable<any> {
    return this.http.get(`${this.apiUrl}/search?query=${query}&page=${page}&size=${size}`);
  }

  advancedSearchRecipes(criteria: RecipeSearchCriteria, page: number = 0, size: number = 10): Observable<any> {
    return this.http.post(`${this.apiUrl}/advanced-search?page=${page}&size=${size}`, criteria);
  }

  getSearchFilterOptions(): Observable<SearchFilterOptions> {
    return this.http.get<SearchFilterOptions>(`${this.apiUrl}/search-options`);
  }

  createRecipe(recipe: RecipeDTO): Observable<RecipeDTO> {
    const token = sessionStorage.getItem('token');
    console.log('Token being sent:', token); // Debug line

    return this.http.post<RecipeDTO>(this.apiUrl, recipe, {
      headers: this.getAuthHeaders()
    });
  }

  updateRecipe(id: number, recipe: RecipeDTO): Observable<RecipeDTO> {
    return this.http.put<RecipeDTO>(`${this.apiUrl}/${id}`, recipe, {
      headers: this.getAuthHeaders()
    });
  }

  deleteRecipe(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, {
      headers: this.getAuthHeaders()
    });
  }

  uploadImage(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<{imageUrl: string}>(`${this.apiUrl}/upload-image`, formData, {
      headers: this.getAuthHeaders()
    }).pipe(
      map(response => response.imageUrl)
    );
  }

  getUserRecipeStats(): Observable<any> {
    // Return mock data for now
    return of({
      total: 0,
      thisMonth: 0,
      topIngredient: ''
    });
  }
}
