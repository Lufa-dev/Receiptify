import { Injectable } from '@angular/core';
import { Ingredient } from '../models/ingredient.model';

@Injectable({
  providedIn: 'root'
})
export class PortionCalculatorService {
  /**
   * Calculate adjusted ingredients based on serving changes
   */
  calculateAdjustedIngredients(
    originalServings: number,
    newServings: number,
    ingredients: Ingredient[]
  ): {
    adjustedIngredients: Ingredient[];
    nonScalableIngredients: string[];
  } {
    // Identify ingredients without specific quantities
    const nonScalableIngredients = ingredients
      .filter(ingredient => !ingredient.amount)
      .map(ingredient => ingredient.name || ingredient.type);

    // Recalculate amounts for ingredients
    const adjustedIngredients = ingredients.map(ingredient => {
      const newIngredient = { ...ingredient };

      // If the ingredient has an amount, recalculate it
      if (newIngredient.amount && originalServings > 0 && ingredient.amount) {
        // Parse the original amount

        const originalAmount = this.parseAmount(ingredient.amount);

        if (originalAmount !== null) {
          // Calculate the new amount
          const newAmount = originalAmount * (newServings / originalServings);
          // Format the new amount
          newIngredient.amount = this.formatAmount(newAmount);
        }
      }

      return newIngredient;
    });

    return {
      adjustedIngredients,
      nonScalableIngredients
    };
  }

  /**
   * Parse an amount string to a number
   */
  parseAmount(amount: string): number | null {
    // Ensure we have a string to work with
    if (!amount || typeof amount !== 'string') {
      return null;
    }

    // Standardize the input by removing any extra spaces
    const cleanAmount = amount.trim();

    // Handle mixed numbers like "1 1/2"
    if (cleanAmount.includes(' ') && cleanAmount.includes('/')) {
      const parts = cleanAmount.split(' ');
      if (parts.length === 2) {
        const whole = parseFloat(parts[0].trim());
        const fractionParts = parts[1].split('/');
        if (fractionParts.length === 2) {
          const numerator = parseFloat(fractionParts[0].trim());
          const denominator = parseFloat(fractionParts[1].trim());
          if (!isNaN(whole) && !isNaN(numerator) && !isNaN(denominator) && denominator !== 0) {
            return whole + (numerator / denominator);
          }
        }
      }
    }

    // Handle fractional amounts like "1/2"
    if (cleanAmount.includes('/')) {
      const parts = cleanAmount.split('/');
      if (parts.length === 2) {
        const numerator = parseFloat(parts[0].trim());
        const denominator = parseFloat(parts[1].trim());
        if (!isNaN(numerator) && !isNaN(denominator) && denominator !== 0) {
          return numerator / denominator;
        }
      }
    }

    // Handle simple numbers
    const num = parseFloat(cleanAmount);
    return !isNaN(num) ? num : null;
  }

  /**
   * Format a number to a user-friendly string
   */
  formatAmount(amount: number): string {
    // If it's a whole number, display as integer
    if (Math.abs(amount - Math.round(amount)) < 0.01) {
      return Math.round(amount).toString();
    }

    // For fractions that are close to common values
    const tolerance = 0.01;

    // Common fractions to check for
    const fractions = [
      { value: 0.25, str: '1/4' },
      { value: 0.5, str: '1/2' },
      { value: 0.75, str: '3/4' },
      { value: 0.33, str: '1/3' },
      { value: 0.67, str: '2/3' },
      { value: 0.2, str: '1/5' },
      { value: 0.4, str: '2/5' },
      { value: 0.6, str: '3/5' },
      { value: 0.8, str: '4/5' },
      { value: 0.125, str: '1/8' },
      { value: 0.375, str: '3/8' },
      { value: 0.625, str: '5/8' },
      { value: 0.875, str: '7/8' },
    ];

    // Check for whole number + fraction
    const wholePart = Math.floor(amount);
    const decimalPart = amount - wholePart;

    // If there's a whole part and a decimal part
    if (wholePart > 0 && decimalPart > 0) {
      // Check for common fractions
      for (const fraction of fractions) {
        if (Math.abs(decimalPart - fraction.value) < tolerance) {
          return `${wholePart} ${fraction.str}`;
        }
      }

      // Otherwise, return with up to 1 decimal place
      return amount.toFixed(1);
    }

    // If it's just a decimal (less than 1)
    for (const fraction of fractions) {
      if (Math.abs(amount - fraction.value) < tolerance) {
        return fraction.str;
      }
    }

    // Default: return with 1 decimal place
    return amount.toFixed(1);
  }
}
