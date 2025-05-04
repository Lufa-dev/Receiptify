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
    // Build sort parameters for the URL
    let sortParam = '';
    if (criteria.sortBy) {
      // Ensure we're not trying to sort by a field that doesn't exist in the entity
      const validSortFields = ['createdAt', 'title', 'prepTime', 'cookTime', 'bakingTime'];
      if (validSortFields.includes(criteria.sortBy)) {
        sortParam = `&sort=${criteria.sortBy},${criteria.sortDirection || 'desc'}`;
      }
    }

    const url = `${this.apiUrl}/advanced-search?page=${page}&size=${size}${sortParam}`;

    // Remove empty values from criteria
    const cleanCriteria = JSON.parse(JSON.stringify(criteria));
    Object.keys(cleanCriteria).forEach(key => {
      if (cleanCriteria[key] === '' || cleanCriteria[key] === null || cleanCriteria[key] === undefined ||
        (Array.isArray(cleanCriteria[key]) && cleanCriteria[key].length === 0)) {
        delete cleanCriteria[key];
      }
    });

    // Remove sort parameters from the request body (they're in the URL)
    delete cleanCriteria.sortBy;
    delete cleanCriteria.sortDirection;

    console.log('Sending advanced search request:', { url, cleanCriteria });

    return this.http.post(url, cleanCriteria);
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

  getRecipeWithSeasonality(id: number, username?: string): Observable<RecipeDTO> {
    const headers = username ? this.authService.getAuthHeaders() : new HttpHeaders();
    return this.http.get<RecipeDTO>(`${this.apiUrl}/${id}/with-seasonality`, {
      headers
    });
  }

  /**
   * Gets seasonal recipes - updated to work without requiring authentication headers
   */
  getSeasonalRecipes(minSeasonalScore: number = 70, page: number = 0, size: number = 10): Observable<any> {
    return this.http.get<any>(
      `${this.apiUrl}/seasonal?minSeasonalScore=${minSeasonalScore}&page=${page}&size=${size}`
    );
  }

  /**
   * Gets current month for seasonality calculations
   */
  getCurrentMonth(): Observable<string> {
    return this.http.get(`${environment.API_URL}/api/seasonality/current-month`, { responseType: 'text' });
  }

  getUserRecipeStats(): Observable<any> {
    return this.http.get<any>(`${environment.API_URL}/api/profile/recipe-stats`, {
      headers: this.authService.getAuthHeaders()
    });
  }
}



