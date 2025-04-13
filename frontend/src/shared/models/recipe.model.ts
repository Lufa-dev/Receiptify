import {Ingredient, IngredientDTO} from './ingredient.model';
import { RecipeStep } from './recipe-step.model';

export interface Recipe {
  id: number;
  title: string;
  description?: string;
  imageUrl?: string;
  ingredients: Ingredient[];
  steps: RecipeStep[];
  createdAt?: string;
  updatedAt?: string;
}

export interface RecipeDTO {
  id?: number;
  title: string;
  description: string;
  imageUrl?: string;
  ingredients: IngredientDTO[];
  steps: RecipeStep[];
  user?: {
    id?: number;
    username: string;
    firstName?: string;
    lastName?: string;
  };

  category?: string;       // e.g. "aprósütemény", "befőttek", etc.
  cuisine?: string;        // e.g. "afrikai", "amerikai", etc.
  servings?: number;       // e.g. 1-12 főre
  difficulty?: string;     // e.g. "könnyű", "közepes", "nehéz"
  costRating?: string;     // e.g. "olcsó", "megfizethető", "költséges"

  // Additional values
  prepTime?: number;       // in minutes
  cookTime?: number;       // in minutes
  bakingTime?: number;     // in minutes
  bakingTemp?: number;     // in Celsius
  panSize?: number;        // in cm
  bakingMethod?: string;
}
