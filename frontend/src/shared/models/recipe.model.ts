import {Ingredient, IngredientDTO} from './ingredient.model';
import { RecipeStep } from './recipe-step.model';
import {RecipeSeasonality} from "./seasonality.model";

export interface Recipe {
  id: number;
  title: string;
  description?: string;
  imageUrl?: string;
  ingredients: Ingredient[];
  steps: RecipeStep[];
  createdAt?: string;
  updatedAt?: string;
  seasonalityInfo?: RecipeSeasonality;
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

  category?: string;
  cuisine?: string;
  servings?: number;
  difficulty?: string;
  costRating?: string;

  prepTime?: number;
  cookTime?: number;
  bakingTime?: number;
  bakingTemp?: number;
  panSize?: number;
  bakingMethod?: string;

  averageRating?: number;
  totalRatings?: number;
  totalComments?: number;
  userRating?: number;
  viewCount?: number;

  seasonalityInfo?: RecipeSeasonality;

  dietaryTags?: string[];

  isInCollection?: boolean;
}
