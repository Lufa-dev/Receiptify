import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../shared/services/admin.service';
import { finalize } from 'rxjs/operators';
import { Router } from '@angular/router';
import { RecipeDTO } from '../../../shared/models/recipe.model';

@Component({
  selector: 'app-recipe-management',
  templateUrl: './recipe-management.component.html',
  styleUrls: ['./recipe-management.component.scss']
})
export class RecipeManagementComponent implements OnInit {
  recipes: RecipeDTO[] = [];
  totalRecipes = 0;
  currentPage = 0;
  pageSize = 10;
  isLoading = false;
  error = '';
  successMessage = '';
  searchQuery = '';
  isSearching = false;

  constructor(
    private adminService: AdminService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadRecipes();
  }

  loadRecipes(page: number = 0): void {
    this.isLoading = true;
    this.error = '';
    this.currentPage = page;

    this.adminService.getAllRecipes(page, this.pageSize)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (response) => {
          this.recipes = response.content;
          this.totalRecipes = response.totalElements;
        },
        error: (error) => {
          this.error = 'Failed to load recipes. Please try again.';
        }
      });
  }

  onPageChange(page: number): void {
    if (this.isSearching && this.searchQuery) {
      this.searchRecipes(this.searchQuery, page);
    } else {
      this.loadRecipes(page);
    }
  }

  onSearch(): void {
    if (!this.searchQuery.trim()) {
      this.isSearching = false;
      this.loadRecipes(0);
      return;
    }

    this.isSearching = true;
    this.searchRecipes(this.searchQuery, 0);
  }

  searchRecipes(query: string, page: number = 0): void {
    this.isLoading = true;
    this.error = '';
    this.currentPage = page;

    this.adminService.searchRecipes(query, page, this.pageSize)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (response) => {
          this.recipes = response.content;
          this.totalRecipes = response.totalElements;
        },
        error: (error) => {
          this.error = 'Failed to search recipes. Please try again.';
        }
      });
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.isSearching = false;
    this.loadRecipes(0);
  }

  viewRecipeDetails(id: number): void {
    this.router.navigate(['/admin/recipes', id]);
  }

  deleteRecipe(id: number, event: Event): void {
    event.stopPropagation();

    if (confirm('Are you sure you want to delete this recipe? This action cannot be undone.')) {
      this.isLoading = true;
      this.error = '';
      this.successMessage = '';

      this.adminService.deleteRecipe(id)
        .pipe(finalize(() => this.isLoading = false))
        .subscribe({
          next: () => {
            this.successMessage = 'Recipe deleted successfully';
            // Refresh the recipe list
            this.loadRecipes(this.currentPage);
          },
          error: (error) => {
            this.error = 'Failed to delete recipe. Please try again.';
          }
        });
    }
  }

  viewRecipeOnSite(id: number, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/recipe', id]);
  }

  get totalPages(): number {
    return Math.ceil(this.totalRecipes / this.pageSize);
  }

  get pages(): number[] {
    const pagesArray = [];
    for (let i = 0; i < this.totalPages; i++) {
      pagesArray.push(i);
    }
    return pagesArray;
  }
}
