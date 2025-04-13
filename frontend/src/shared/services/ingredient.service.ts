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
        name: item.name,
        displayName: item.displayName,
        category: item.category
      })))
    );
  }

  getIngredientsByCategory(): Observable<Record<string, IngredientType[]>> {
    return this.http.get<Record<string, string[]>>(`${this.apiUrl}/by-category`).pipe(
      map(categories => {
        const result: Record<string, IngredientType[]> = {};

        Object.entries(categories).forEach(([category, ingredientNames]) => {
          result[category] = ingredientNames.map(enumName => ({
            name: enumName.toLowerCase(),
            displayName: this.formatEnumName(enumName),
            category: category
          }));
        });

        return result;
      })
    );
  }

// Helper method to format enum names
  private formatEnumName(enumName: string): string {
    return enumName
      .split('_')
      .map(word => word.charAt(0) + word.slice(1).toLowerCase())
      .join(' ');
  }

  getCategories(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/categories`);
  }
}

