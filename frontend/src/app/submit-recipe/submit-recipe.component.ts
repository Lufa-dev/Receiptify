import {finalize} from "rxjs";
import {RecipeService} from "../../shared/services/recipe.service";
import {AngularFireStorage} from "@angular/fire/compat/storage";
import {AuthService} from "../../shared/services/auth.service";
import {Recipe} from "../../shared/models/recipe.model";
import {Component} from "@angular/core";

@Component({
  selector: 'app-submit-recipe',
  templateUrl: './submit-recipe.component.html',
  styleUrls: ['./submit-recipe.component.scss']
})
export class SubmitRecipeComponent {
  recipe: Recipe = {
    title: '',
    description: '',
    imageUrl: '',
    userId: '',
    ingredients: [],
    steps: []
  };
  ingredients: string[] = [];
  steps: string[] = [];
  selectedImageFile: File | null = null;

  constructor(private recipeService: RecipeService, private storage: AngularFireStorage, private authService: AuthService) {}

  addIngredient() {
    this.ingredients.push('');
  }

  removeIngredient(index: number) {
    this.ingredients.splice(index, 1);
  }

  addStep() {
    this.steps.push('');
  }

  removeStep(index: number) {
    this.steps.splice(index, 1);
  }

  onImageChange(event: any) {
    this.selectedImageFile = event.target.files[0];
  }

  onSubmit() {
    this.recipe.ingredients = this.ingredients;
    this.recipe.steps = this.steps;
    this.authService.getCurrentUserIdObservable().subscribe((userId: any) => {
      if (userId) {
        this.recipe.userId = userId;
        if (this.selectedImageFile) {
          const filePath = `recipes/${Date.now()}_${this.selectedImageFile.name}`;
          const fileRef = this.storage.ref(filePath);
          const task = this.storage.upload(filePath, this.selectedImageFile);

          task.snapshotChanges().pipe(
            finalize(() => {
              fileRef.getDownloadURL().subscribe((url: any) => {
                this.recipe.imageUrl = url;
                this.saveRecipe();
              });
            })
          ).subscribe();
        } else {
          this.saveRecipe();
        }
      }
    });
  }

  saveRecipe() {
    this.recipeService.addRecipe(this.recipe).subscribe(
        (response: any) => {
        console.log('Recipe submitted successfully', response);
        // Reset form or navigate to another page
      },
        (error: any) => {
        console.error('Error submitting recipe', error);
      }
    );
  }
}
