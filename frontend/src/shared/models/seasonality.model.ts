export interface IngredientSeasonality {
  ingredientId: number;
  ingredientName: string;
  seasonality: string;
  status: string;
  isInSeason: boolean;
  isComingSoon: boolean;
}

export interface RecipeSeasonality {
  recipeId: number;
  seasonalScore: number;
  inSeasonCount: number;
  outOfSeasonCount: number;
  ingredientSeasonality: IngredientSeasonality[];
}
