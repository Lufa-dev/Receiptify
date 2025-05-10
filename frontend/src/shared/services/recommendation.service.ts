import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {catchError, Observable, of} from 'rxjs';
import { environment } from '../../environments/environment';
import { RecipeDTO } from '../models/recipe.model';
import { AuthService } from './auth.service';
import {CollectionService} from "./collection.service";
import {map, tap} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class RecommendationService {
  private apiUrl = `${environment.API_URL}/api/recommendations`;

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private collectionService: CollectionService
  ) { }

  /**
   * Get personalized recommendations for the current user
   */
  getRecommendationsForUser(limit: number = 10, includePreviousInteractions: boolean = false): Observable<RecipeDTO[]> {
    const url = `${this.apiUrl}/for-user?limit=${limit}&includePrevious=${includePreviousInteractions}`;

    return this.http.get<RecipeDTO[]>(url, {
      headers: this.authService.getAuthHeaders()
    }).pipe(
      // Add a fallback to return empty array instead of error
      catchError(error => {
        return of([]);
      })
    );
  }
  /**
   * Get recipes similar to the specified recipe
   */
  getSimilarRecipes(recipeId: number, limit: number = 6): Observable<RecipeDTO[]> {
    return this.http.get<RecipeDTO[]>(`${this.apiUrl}/similar/${recipeId}?limit=${limit}`);
  }

  /**
   * Get seasonal recipe recommendations
   */
  getSeasonalRecommendations(limit: number = 10): Observable<RecipeDTO[]> {
    return this.http.get<RecipeDTO[]>(`${this.apiUrl}/seasonal?limit=${limit}`);
  }

  /**
   * Track that a user viewed a recipe (for recommendation analytics)
   */
  trackRecipeView(recipeId: number): Observable<any> {
    if (!this.authService.isLoggedIn()) {
      return of(null); // Return empty observable if not logged in
    }

    return this.http.post(`${this.apiUrl}/track-view/${recipeId}`, {}, {
      headers: this.authService.getAuthHeaders()
    }).pipe(
      catchError(error => {
        return of(null);
      })
    );
  }


  /**
   * Track that a user saved/unsaved a recipe
   */
  isRecipeInAnyCollection(recipeId: number): Observable<boolean> {
    if (!this.authService.isLoggedIn()) {
      return of(false);
    }

    return this.collectionService.getUserCollections().pipe(
      map(collections => {
        // Check if the recipe is in any collection
        return collections.some(collection =>
          collection.recipeIds && collection.recipeIds.includes(recipeId)
        );
      }),
      catchError(error => {
        return of(false);
      })
    );
  }

  /**
   * Add recipe to favorites collection (shorthand for collection functionality)
   * This is a convenience method for the recommendation system
   */
  addToFavorites(recipeId: number): Observable<any> {
    if (!this.authService.isLoggedIn()) {
      return of(null);
    }

    // First find the Favorites collection
    return this.collectionService.getUserCollections().pipe(
      map(collections => {
        const favoritesCollection = collections.find(c =>
          c.isDefault && c.name === 'Favorites'
        );
        return favoritesCollection?.id;
      }),
      tap(collectionId => {
        if (!collectionId) {
          throw new Error('Favorites collection not found');
        }
      }),
      // Add the recipe to the Favorites collection
      map(collectionId => {
        if (collectionId) {
          this.collectionService.addRecipeToCollection(collectionId, recipeId).subscribe();
          // Also track this action for recommendation system
          this.http.post(`${this.apiUrl}/save/${recipeId}?saved=true`, {}, {
            headers: this.authService.getAuthHeaders()
          }).subscribe();
        }
        return true;
      }),
      catchError(error => {
        return of(false);
      })
    );
  }
}
