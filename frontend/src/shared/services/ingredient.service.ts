import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { IngredientType } from "../models/ingredient-type.model";
import {map} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class IngredientService {
  private apiUrl = `${environment.API_URL}/api/ingredients`;

  constructor(private http: HttpClient) { }

  getAllIngredientTypes(): Observable<IngredientType[]> {
    return this.http.get<any[]>(this.apiUrl).pipe(
      map(data => data.map(item => ({
        name: item.name || item,
        displayName: item.displayName || this.formatEnumName(item.name || item),
        category: item.category
      })))
    );
  }

  getIngredientsByCategory(): Observable<Record<string, IngredientType[]>> {
    return this.http.get<Record<string, any[]>>(`${this.apiUrl}/by-category`).pipe(
      map(categories => {
        const result: Record<string, IngredientType[]> = {};

        Object.entries(categories).forEach(([category, ingredients]) => {
          result[category] = ingredients.map(item => ({
            name: typeof item === 'string' ? item : item.name,
            displayName: typeof item === 'string' ? this.formatEnumName(item) : item.displayName,
            category: category
          }));
        });

        return result;
      })
    );
  }

  // Helper method to format enum names
  private formatEnumName(enumName: string): string {
    if (!enumName) return '';

    return enumName
      .split('_')
      .map(word => word.charAt(0) + word.slice(1).toLowerCase())
      .join(' ');
  }

  getCategories(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/categories`);
  }
}



