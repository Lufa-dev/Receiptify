export interface Comment {
  id?: number;
  content: string;
  user?: {
    id?: number;
    username: string;
    firstName?: string;
    lastName?: string;
  };
  recipeId: number;
  createdAt?: string;
  updatedAt?: string;
  status?: string;        // Added for admin functionality
  adminNotes?: string;    // Added for admin functionality
}
