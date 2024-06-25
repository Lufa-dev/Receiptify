import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../shared/services/auth.service';
import { UserService } from '../../shared/services/user.service';
import { RecipeService } from '../../shared/services/recipe.service';
import { User } from '../../shared/models/user.model';
import { Recipe } from '../../shared/models/recipe.model';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  user!: User;
  userRecipes: Recipe[] = [];
  userProfilePicture!: string;
  userRecipesCount!: number;

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private recipeService: RecipeService
  ) {}

  ngOnInit(): void {
    this.loadUserProfile();
  }

  loadUserProfile(): void {
    this.authService.getCurrentUserIdObservable().subscribe(userId => {
      if (userId) {
        this.userService.getUserById(userId).subscribe(user => {
          this.user = user;
          this.userProfilePicture = user.profilePicture || 'assets/default-profile.png';
          this.loadUserRecipes(userId);
        });
      }
    });
  }

  loadUserRecipes(userId: string): void {
    this.userService.getUserRecipes(userId).subscribe(recipes => {
      this.userRecipes = recipes;
      this.userRecipesCount = recipes.length;
    });
  }

  changeData(): void {
    // Logic to change user data
  }

  uploadRecipe(): void {
    // Logic to upload a new recipe
  }
}
