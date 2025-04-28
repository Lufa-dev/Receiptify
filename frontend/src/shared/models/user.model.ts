export interface User {
  username: string;
  token: string;
  email?: string;
  firstName?: string;
  lastName?: string;

  preferredCategories?: string[];
  preferredCuisines?: string[];
  favoriteIngredients?: string[];
  dislikedIngredients?: string[];
  maxPrepTime?: number;
  difficultyPreference?: string;
  preferSeasonalRecipes?: boolean;
}
