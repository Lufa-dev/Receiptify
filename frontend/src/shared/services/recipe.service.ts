import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {catchError, from, Observable, switchMap} from 'rxjs';
import { Recipe } from '../models/recipe.model';
import {environment} from "../../environments/environment";
import {AuthService} from "./auth.service";
import {user} from "@angular/fire/auth";

@Injectable({
  providedIn: 'root'
})
export class RecipeService {

  private apiUrl = `${environment.apiUrl}/recipes`;

  constructor(private http: HttpClient, private authService:AuthService) {}

  getAllRecipes(): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(this.apiUrl + '/getAll')
  }

  addRecipe(recipe: Recipe): Observable<Recipe> {
    return this.authService.getCurrentUser().pipe(
      switchMap(user => {
        if (user) {
          return from(user.getIdToken()).pipe(
            switchMap(token => {
              const headers = new HttpHeaders({
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
              });
              return this.http.post<Recipe>(`${this.apiUrl}/add`, recipe, { headers });
            }),
            catchError(err => {
              console.error('Error fetching ID token', err);
              throw err;
            })
          );
        } else {
          throw new Error('User not authenticated');
        }
      })
    );
  }

  getRecipesByUserId(userId: string): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(`${this.apiUrl}/user/${userId}`);
  }
}
