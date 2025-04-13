export interface Collection {
  id: number;
  name: string;
  description?: string;
  isDefault: boolean;
  user?: {
    id?: number;
    username: string;
    firstName?: string;
    lastName?: string;
  };
  recipeIds: number[];
  recipeCount: number;
}
