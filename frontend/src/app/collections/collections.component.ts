import {Component, OnInit} from '@angular/core';
import {Collection} from "../../shared/models/collection.model";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {CollectionService} from "../../shared/services/collection.service";
import {AuthService} from "../../shared/services/auth.service";
import {Router} from "@angular/router";
import {finalize} from "rxjs";

@Component({
  selector: 'app-collections',
  templateUrl: './collections.component.html',
  styleUrl: './collections.component.scss'
})
export class CollectionsComponent implements OnInit {
  collections: Collection[] = [];
  isLoading = false;
  error = '';
  successMessage = '';

  // Form for creating and editing collections
  collectionForm: FormGroup;
  isEditing = false;
  editingCollectionId: number | null = null;
  showForm = false;
  isSubmitting = false;

  constructor(
    private collectionService: CollectionService,
    private authService: AuthService,
    private router: Router,
    private fb: FormBuilder
  ) {
    this.collectionForm = this.createForm();
  }

  ngOnInit(): void {
    // Check if user is logged in
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadCollections();
  }

  loadCollections(): void {
    this.isLoading = true;
    this.error = '';

    this.collectionService.getUserCollections()
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (collections) => {
          this.collections = collections;
        },
        error: (error) => {
          this.error = 'Failed to load collections. Please try again.';

          if (error.status === 401) {
            setTimeout(() => this.router.navigate(['/login']), 1500);
          }
        }
      });
  }

  createForm(): FormGroup {
    return this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(50)]],
      description: ['', Validators.maxLength(200)]
    });
  }

  toggleForm(): void {
    if (this.showForm && this.isEditing) {
      // Reset form if canceling edit
      this.resetForm();
    }
    this.showForm = !this.showForm;
  }

  resetForm(): void {
    this.collectionForm.reset();
    this.isEditing = false;
    this.editingCollectionId = null;
  }

  onSubmit(): void {
    if (this.collectionForm.invalid) {
      return;
    }

    this.isSubmitting = true;
    this.error = '';
    this.successMessage = '';

    const collectionData = {
      name: this.collectionForm.value.name,
      description: this.collectionForm.value.description || ''
    };

    const request = this.isEditing && this.editingCollectionId
      ? this.collectionService.updateCollection(this.editingCollectionId, collectionData)
      : this.collectionService.createCollection(collectionData);

    request.pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: (collection) => {
          this.successMessage = this.isEditing
            ? 'Collection updated successfully!'
            : 'Collection created successfully!';

          this.resetForm();
          this.showForm = false;
          this.loadCollections();
        },
        error: (error) => {

          if (error.status === 401) {
            this.error = 'Your session has expired. Please log in again.';
            setTimeout(() => this.router.navigate(['/login']), 1500);
          } else if (error.status === 400 && error.error?.error) {
            this.error = error.error.error;
          } else {
            this.error = 'Failed to save collection. Please try again.';
          }
        }
      });
  }

  editCollection(collection: Collection): void {
    this.collectionForm.patchValue({
      name: collection.name,
      description: collection.description || ''
    });

    this.isEditing = true;
    this.editingCollectionId = collection.id;
    this.showForm = true;
  }

  deleteCollection(id: number): void {
    if (confirm('Are you sure you want to delete this collection? This action cannot be undone.')) {
      this.isLoading = true;

      this.collectionService.deleteCollection(id)
        .pipe(finalize(() => this.isLoading = false))
        .subscribe({
          next: () => {
            this.successMessage = 'Collection deleted successfully!';
            this.loadCollections();
          },
          error: (error) => {

            if (error.status === 401) {
              this.error = 'Your session has expired. Please log in again.';
              setTimeout(() => this.router.navigate(['/login']), 1500);
            } else if (error.status === 403 && error.error?.error) {
              this.error = error.error.error;
            } else {
              this.error = 'Failed to delete collection. Please try again.';
            }
          }
        });
    }
  }

  viewCollection(id: number): void {
    this.router.navigate(['/collections', id]);
  }

  isDefaultCollection(collection: Collection): boolean {
    return collection.isDefault ||
      collection.name === 'My Recipes' ||
      collection.name === 'Favorites';
  }
}
