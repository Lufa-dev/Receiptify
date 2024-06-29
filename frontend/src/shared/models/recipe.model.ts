export interface Recipe {
  id?: string;
  title: string;
  description: string;
  imageUrl: string;
  userId: string;
  ingredients: string[];
  steps: string[];
}
