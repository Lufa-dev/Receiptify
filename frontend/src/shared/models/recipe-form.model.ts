import {Ingredient} from "./ingredient.model";
import {RecipeStep} from "./recipe-step.model";

export interface RecipeFormModel {
  title: string;
  description: string;
  imageUrl: string;
  ingredients: Ingredient[];
  steps: RecipeStep[];
}
