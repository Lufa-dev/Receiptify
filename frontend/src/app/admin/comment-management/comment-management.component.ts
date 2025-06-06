import { Component, OnInit } from '@angular/core';
import { finalize } from "rxjs";
import { AdminService } from "../../../shared/services/admin.service";
import { Router } from "@angular/router";
import { Comment } from "../../../shared/models/comment.model";

@Component({
  selector: 'app-comment-management',
  templateUrl: './comment-management.component.html',
  styleUrls: ['./comment-management.component.scss']
})
export class CommentManagementComponent implements OnInit {
  comments: any[] = []; // Using any[] to avoid type conflicts
  totalComments = 0;
  currentPage = 0;
  pageSize = 10;
  isLoading = false;
  error = '';
  successMessage = '';

  constructor(
    private adminService: AdminService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadComments();
  }

  loadComments(page: number = 0): void {
    this.isLoading = true;
    this.error = '';
    this.currentPage = page;

    this.adminService.getAllComments(page, this.pageSize)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (response) => {
          // Using any type to avoid type conflicts
          this.comments = response.content;
          this.totalComments = response.totalElements;
        },
        error: (error) => {
          this.error = 'Failed to load comments. Please try again.';
        }
      });
  }

  onPageChange(page: number): void {
    this.loadComments(page);
  }

  viewCommentDetails(id: number): void {
    this.router.navigate(['/admin/comments', id]);
  }

  deleteComment(id: number, event: Event): void {
    event.stopPropagation();

    if (confirm('Are you sure you want to delete this comment? This action cannot be undone.')) {
      this.isLoading = true;
      this.error = '';
      this.successMessage = '';

      this.adminService.deleteComment(id)
        .pipe(finalize(() => this.isLoading = false))
        .subscribe({
          next: () => {
            this.successMessage = 'Comment deleted successfully';
            // Refresh the comment list
            this.loadComments(this.currentPage);
          },
          error: (error) => {
            this.error = 'Failed to delete comment. Please try again.';
          }
        });
    }
  }

  viewRecipe(recipeId: number, event: Event): void {
    // Prevent the event from bubbling up to the row click handler
    event.preventDefault();
    event.stopPropagation();

    // Navigate to the recipe detail page in admin panel
    this.router.navigate(['/admin/recipes', recipeId]);
  }

  viewUser(userId: number, event: Event): void {
    // Prevent the event from bubbling up to the row click handler
    event.preventDefault();
    event.stopPropagation();

    // Navigate to the user detail page in admin panel
    this.router.navigate(['/admin/users', userId]);
  }

  get totalPages(): number {
    return Math.ceil(this.totalComments / this.pageSize);
  }

  get pages(): number[] {
    const pagesArray = [];
    for (let i = 0; i < this.totalPages; i++) {
      pagesArray.push(i);
    }
    return pagesArray;
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleString();
  }

  getContentPreview(content: string, maxLength: number = 100): string {
    if (!content) return '';

    if (content.length <= maxLength) {
      return content;
    }

    return content.substring(0, maxLength) + '...';
  }
}

