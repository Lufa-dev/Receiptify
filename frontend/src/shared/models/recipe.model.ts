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
}
