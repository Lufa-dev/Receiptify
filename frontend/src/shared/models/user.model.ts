export interface User {
  id?: string;
  firstName: string;
  lastName: string;
  email: string;
  password?: string; // Optional, as it might not be stored directly in Firestore
  profilePicture?: string;
  recipeIds?: string[];
}
