import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';
import { Recipe } from '../models/recipe.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private apiUrl = 'http://localhost:8080/users'; // Adjust the URL as needed

  constructor(private http: HttpClient) {}

  getUserById(userId: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${userId}`);
  }

  getUserRecipes(userId: string): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(`${this.apiUrl}/${userId}/recipes`);
  }
}
