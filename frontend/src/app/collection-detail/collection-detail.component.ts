import {Component, OnDestroy, OnInit} from '@angular/core';
import {Collection} from "../../shared/models/collection.model";
import {Recipe} from "../../shared/models/recipe.model";
import {ActivatedRoute, Router} from "@angular/router";
import {CollectionService} from "../../shared/services/collection.service";
import {RecipeService} from "../../shared/services/recipe.service";
import {catchError, finalize, forkJoin, of, Subscription} from "rxjs";
import {map} from "rxjs/operators";

@Component({
  selector: 'app-collection-detail',
  templateUrl: './collection-detail.component.html',
  styleUrl: './collection-detail.component.scss'
})
export class CollectionDetailComponent implements OnInit, OnDestroy {
  collection: Collection | null = null;
  recipes: Recipe[] = [];
  isLoading = true;
  error = '';
  successMessage = '';
  private subscriptions: Subscription[] = [];


  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private collectionService: CollectionService,
    private recipeService: RecipeService
  ) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.loadCollection(+id);
      } else {
        this.error = 'Collection not found';
        this.isLoading = false;
      }
    });
  }

  loadCollection(id: number): void {
    this.isLoading = true;
    this.error = '';

    const sub = this.collectionService.getCollectionById(id).subscribe({
      next: (collection) => {
        this.collection = collection;

        // If the collection has recipes, fetch them
        if (collection.recipeIds && collection.recipeIds.length > 0) {
          this.loadRecipes(collection.recipeIds);
        } else {
          this.recipes = [];
          this.isLoading = false;
        }
      },
      error: (error) => {
        this.error = 'Failed to load collection details. Please try again.';
        this.isLoading = false;

        if (error.status === 401) {
          setTimeout(() => this.router.navigate(['/login']), 1500);
        }
      }
    });
    this.subscriptions.push(sub);
  }

  loadRecipes(recipeIds: number[]): void {
    if (!recipeIds || recipeIds.length === 0) {
      this.recipes = [];
      this.isLoading = false;
      return;
    }

    // Create an array of observables for each recipe to fetch
    const recipeObservables = recipeIds.map(id =>
      this.recipeService.getRecipeById(id).pipe(
        // Handle errors for individual recipes without failing the whole operation
        map(recipe => {
          return recipe;
        }),
        catchError(error => {
          return of(null);
        })
      )
    );

    // Execute all requests in parallel and combine results
    forkJoin(recipeObservables)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (results) => {
          // Filter out null results (failed requests)
          this.recipes = results.filter(recipe => recipe !== null) as Recipe[];
        },
        error: (error) => {
          this.error = 'Failed to load some recipes from this collection.';
        }
      });
  }

  removeRecipeFromCollection(recipeId: number): void {
    if (!this.collection) return;

    if (confirm('Are you sure you want to remove this recipe from the collection?')) {
      const sub = this.collectionService.removeRecipeFromCollection(this.collection.id, recipeId)
        .subscribe({
          next: (updatedCollection) => {
            this.successMessage = 'Recipe removed from collection!';
            // Update the local collection object
            this.collection = updatedCollection;
            // Remove the recipe from the displayed list
            this.recipes = this.recipes.filter(recipe => recipe.id !== recipeId);
          },
          error: (error) => {
            this.error = 'Failed to remove recipe from collection. Please try again.';

            if (error.status === 401) {
              setTimeout(() => this.router.navigate(['/login']), 1500);
            }
          }
        });
      this.subscriptions.push(sub);
    }
  }

  navigateToRecipe(recipeId: number): void {
    this.router.navigate(['/recipe', recipeId]);
  }

  isMyRecipesCollection(): boolean {
    if (!this.collection) return false;

    return this.collection.name.toLowerCase() === 'my recipes';
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }
}




