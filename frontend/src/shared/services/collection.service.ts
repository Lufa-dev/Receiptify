import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Collection } from '../models/collection.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class CollectionService {
  private apiUrl = `${environment.API_URL}/api/collections`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  private getAuthHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`
    });
  }

  getUserCollections(): Observable<Collection[]> {
    return this.http.get<Collection[]>(this.apiUrl, {
      headers: this.getAuthHeaders()
    });
  }

  getCollectionById(id: number): Observable<Collection> {
    return this.http.get<Collection>(`${this.apiUrl}/${id}`, {
      headers: this.getAuthHeaders()
    });
  }

  createCollection(collection: Partial<Collection>): Observable<Collection> {
    return this.http.post<Collection>(this.apiUrl, collection, {
      headers: this.getAuthHeaders()
    });
  }

  updateCollection(id: number, collection: Partial<Collection>): Observable<Collection> {
    return this.http.put<Collection>(`${this.apiUrl}/${id}`, collection, {
      headers: this.getAuthHeaders()
    });
  }

  deleteCollection(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, {
      headers: this.getAuthHeaders()
    });
  }

  addRecipeToCollection(collectionId: number, recipeId: number): Observable<Collection> {
    return this.http.post<Collection>(`${this.apiUrl}/${collectionId}/recipes/${recipeId}`, {}, {
      headers: this.getAuthHeaders()
    });
  }

  removeRecipeFromCollection(collectionId: number, recipeId: number): Observable<Collection> {
    return this.http.delete<Collection>(`${this.apiUrl}/${collectionId}/recipes/${recipeId}`, {
      headers: this.getAuthHeaders()
    });
  }

  getUserCollectionsFiltered(filterMyRecipes: boolean = false): Observable<Collection[]> {
    const url = `${this.apiUrl}?filterMyRecipes=${filterMyRecipes}`;
    return this.http.get<Collection[]>(url, {
      headers: this.getAuthHeaders()
    });
  }
}
