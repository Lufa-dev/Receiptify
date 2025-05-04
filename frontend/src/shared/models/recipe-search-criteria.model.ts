export interface RecipeSearchCriteria {
  searchQuery?: string;

  // Ingredient filters
  includeIngredients?: string[];
  excludeIngredients?: string[];

  // Recipe characteristics
  category?: string;
  cuisine?: string;
  difficulty?: string;
  costRating?: string;

  // Numeric ranges
  minServings?: number;
  maxServings?: number;
  maxPrepTime?: number;
  maxCookTime?: number;
  maxTotalTime?: number;

  // Seasonality filters
  seasonalOnly?: boolean;
  minSeasonalScore?: number;

  // Dietary filters
  dietaryTags?: string[];

  // Sorting
  sortBy?: string;
  sortDirection?: string;
}

export interface SearchFilterOptions {
  categories: string[];
  cuisines: string[];
  difficulties: string[];
  costRatings: string[];
}
