import {Component, Input, OnInit} from '@angular/core';
import {finalize} from "rxjs";
import {CollectionService} from "../../shared/services/collection.service";
import {Collection} from "../../shared/models/collection.model";

@Component({
  selector: 'app-add-to-collection',
  templateUrl: './add-to-collection.component.html',
  styleUrl: './add-to-collection.component.scss'
})
export class AddToCollectionComponent implements OnInit {
  @Input() recipeId!: number; // Using definite assignment assertion

  collections: Collection[] = [];
  isLoading = false;
  error = '';
  successMessage = '';
  showDropdown = false;

  constructor(private collectionService: CollectionService) {
    // No need for default assignment with definite assignment assertion
  }

  ngOnInit(): void {
    this.loadCollections();
  }

  toggleDropdown(): void {
    this.showDropdown = !this.showDropdown;
  }

  loadCollections(): void {
    this.isLoading = true;
    this.error = '';

    this.collectionService.getUserCollections()
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (collections) => {
          // Filter out the "My Recipes" collection as it's automatically populated
          this.collections = collections.filter(collection =>
            !(collection.isDefault && collection.name === 'My Recipes')
          );
        },
        error: (error) => {
          this.error = 'Failed to load collections';
        }
      });
  }

  addToCollection(collectionId: number, event: Event): void {
    event.stopPropagation(); // Prevent event bubbling

    if (!this.recipeId) {
      this.error = 'No recipe selected';
      return;
    }

    this.isLoading = true;
    this.error = '';
    this.successMessage = '';

    this.collectionService.addRecipeToCollection(collectionId, this.recipeId)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (collection) => {
          this.successMessage = `Added to "${collection.name}" collection!`;
          this.showDropdown = false;

          // Update the collection in our local array
          const index = this.collections.findIndex(c => c.id === collection.id);
          if (index !== -1) {
            this.collections[index] = collection;
          }
        },
        error: (error) => {

          if (error.status === 400 && error.error?.error) {
            this.error = error.error.error;
          } else {
            this.error = 'Failed to add to collection';
          }
        }
      });
  }

  isRecipeInCollection(collectionId: number): boolean {
    const collection = this.collections.find(c => c.id === collectionId);
    return collection ? collection.recipeIds.includes(this.recipeId) : false;
  }


}


