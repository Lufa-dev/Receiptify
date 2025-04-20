import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Rating } from '../models/rating.model';
import { RatingSummary } from '../models/rating-summary.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class RatingService {
  private apiUrl = `${environment.API_URL}/api/ratings`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  rateRecipe(rating: Rating): Observable<Rating> {
    return this.http.post<Rating>(this.apiUrl, rating, {
      headers: this.authService.getAuthHeaders()
    });
  }

  getRecipeRatingSummary(recipeId: number): Observable<RatingSummary> {
    return this.http.get<RatingSummary>(`${this.apiUrl}/recipe/${recipeId}`);
  }

  getUserRatingForRecipe(recipeId: number): Observable<Rating | null> {
    return this.http.get<Rating | null>(`${this.apiUrl}/recipe/${recipeId}/user`, {
      headers: this.authService.getAuthHeaders()
    });
  }
}
