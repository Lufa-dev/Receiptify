export interface User {
  id?: number;            // Added for admin functionality
  username: string;
  token: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  roles?: string;         // Added for admin functionality

  preferredCategories?: string[];
  preferredCuisines?: string[];
  favoriteIngredients?: string[];
  dislikedIngredients?: string[];
  maxPrepTime?: number;
  difficultyPreference?: string;
  preferSeasonalRecipes?: boolean;

  // Admin-specific properties
  recipeCount?: number;
  commentCount?: number;
  ratingCount?: number;
  recentRecipes?: any[];
}
