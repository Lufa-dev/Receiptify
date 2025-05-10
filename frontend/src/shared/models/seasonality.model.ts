export interface RecipeSeasonality {
  recipeId: number;
  seasonalScore: number;
  inSeasonCount: number;
  outOfSeasonCount: number;
  yearRoundCount: number;
  trulySeasonalCount: number;
  ingredientSeasonality: IngredientSeasonality[];
}

export interface IngredientSeasonality {
  ingredientId: number;
  ingredientName: string;
  seasonality: string;
  status: string;
  isInSeason: boolean;
  isComingSoon: boolean;
}
