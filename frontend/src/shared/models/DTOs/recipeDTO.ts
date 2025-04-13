export interface RecipeDTO {
  id?: number;
  title: string;
  description: string;
  imageUrl?: string;
  ingredients: {
    id?: number;
    type: string;
    amount?: string;
    unit?: string;
    name?: string;
  }[];
  steps: {
    id?: number;
    stepNumber: number;
    instruction: string;
  }[];
}
