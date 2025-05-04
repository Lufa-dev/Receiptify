export interface ShoppingListItem {
  name: string;
  amount: string;
  unit?: string;
  type?: string;
  numericAmount: number | null;
  checked: boolean;
  recipes: string[]; // List of recipe names this item is used in
}
