import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {map, tap} from "rxjs/operators";

import { UnitType} from "../models/unit-type.model";


@Injectable({
  providedIn: 'root'
})
export class UnitService {
  private apiUrl = `${environment.API_URL}/api/units`;

  constructor(private http: HttpClient) { }

  getAllUnitTypes(): Observable<UnitType[]> {
    return this.http.get<any[]>(this.apiUrl).pipe(
      map(units => units.map(unit => this.parseUnitResponse(unit)))
    );
  }

  getUnitsByCategory(): Observable<Record<string, UnitType[]>> {
    return this.http.get<any>(this.apiUrl + '/by-category').pipe(
      map(categoryMap => {
        const result: Record<string, UnitType[]> = {};

        Object.keys(categoryMap).forEach(category => {
          result[category] = Array.isArray(categoryMap[category])
            ? categoryMap[category].map((unit: any) => this.parseUnitResponse(unit))
            : [];
        });

        return result;
      })
    );
  }

  getCategories(): Observable<string[]> {
    return this.http.get<string[]>(this.apiUrl + '/categories').pipe();
  }

  private parseUnitResponse(unit: any): UnitType {
    if (unit && typeof unit === 'object') {
      // If properly serialized with correct properties
      if (unit.name !== undefined && unit.symbol !== undefined && unit.category !== undefined) {
        return {
          name: unit.name,
          symbol: unit.symbol,
          category: unit.category
        };
      }

      // Some Jackson configurations serialize enums differently
      if ('_name' in unit || 'ordinal' in unit) {
        const enumName = unit._name || unit.name || '';
        return {
          name: enumName,
          symbol: this.getSymbolFromName(enumName),
          category: this.getCategoryFromName(enumName)
        };
      }
    }

    // Case 2: Unit is serialized as string (enum name)
    if (typeof unit === 'string') {
      return {
        name: unit,
        symbol: this.getSymbolFromName(unit),
        category: this.getCategoryFromName(unit)
      };
    }

    // Default case: Couldn't parse unit, create a placeholder
    return {
      name: 'UNKNOWN',
      symbol: 'unknown',
      category: 'Other'
    };
  }

  // Map enum names to symbols based on your Java enum
  getSymbolFromName(enumName: string): string {
    const symbolMap: Record<string, string> = {
      'MILLILITER': 'ml',
      'CENTILITER': 'cl',
      'DECILITER': 'dl',
      'LITER': 'l',
      'MILLIGRAM': 'mg',
      'GRAM': 'g',
      'KILOGRAM': 'kg',
      'MILLIMETER': 'mm',
      'CENTIMETER': 'cm',
      'CELSIUS': '°C',
      'PIECE': 'pc',
      'SLICE': 'slice',
      'PINCH': 'pinch',
      'HANDFUL': 'handful',
      'TEASPOON': 'tsp',
      'TABLESPOON': 'tbsp',
      'CUP': 'cup',
      'TO_TASTE': 'to taste',
      'AS_NEEDED': 'as needed'
    };

    return symbolMap[enumName] || enumName.toLowerCase();
  }

  // Map enum names to categories based on your Java enum
  getCategoryFromName(enumName: string): string {
    const categoryMap: Record<string, string> = {
      'MILLILITER': 'Volume',
      'CENTILITER': 'Volume',
      'DECILITER': 'Volume',
      'LITER': 'Volume',
      'MILLIGRAM': 'Weight',
      'GRAM': 'Weight',
      'KILOGRAM': 'Weight',
      'MILLIMETER': 'Length',
      'CENTIMETER': 'Length',
      'CELSIUS': 'Temperature',
      'PIECE': 'Count',
      'SLICE': 'Count',
      'PINCH': 'Count',
      'HANDFUL': 'Count',
      'TEASPOON': 'Spoon',
      'TABLESPOON': 'Spoon',
      'CUP': 'Cup',
      'TO_TASTE': 'Other',
      'AS_NEEDED': 'Other'
    };

    return categoryMap[enumName] || 'Other';
  }

  // Format enum name for display
  formatUnitName(enumName: string): string {
    if (!enumName) return '';

    return enumName
      .split('_')
      .map(word => word.charAt(0) + word.slice(1).toLowerCase())
      .join(' ');
  }
}

