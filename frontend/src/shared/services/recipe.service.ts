import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Recipe } from '../models/recipe.model';

@Injectable({
  providedIn: 'root'
})
export class RecipeService {

  private apiUrl = 'http://localhost:8080/recipes';

  constructor(private http: HttpClient) {}

  createRecipe(recipe: Recipe): Observable<string> {
    return this.http.post<string>(this.apiUrl, recipe);
  }

  getRecipesByUserId(userId: string): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(`${this.apiUrl}/user/${userId}`);
  }
}
