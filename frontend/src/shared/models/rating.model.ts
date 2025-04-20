export interface Rating {
  id?: number;
  stars: number;
  user?: {
    id?: number;
    username: string;
    firstName?: string;
    lastName?: string;
  };
  recipeId: number;
  createdAt?: string;
  updatedAt?: string;
}
