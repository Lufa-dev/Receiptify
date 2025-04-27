import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class NutritionService {
  private apiUrl = `${environment.API_URL}/api/nutrition`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  /**
   * Get nutrition information for a recipe
   */
  getRecipeNutrition(recipeId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/recipe/${recipeId}`);
  }
}
