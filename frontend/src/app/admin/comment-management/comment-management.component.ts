import {finalize} from "rxjs";
import {OnInit} from "@angular/core";
import {AdminService} from "../../../shared/services/admin.service";
import {Router} from "@angular/router";

export class CommentManagementComponent implements OnInit {
  comments: Comment[] = [];
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
          this.comments = response.content;
          this.totalComments = response.totalElements;
        },
        error: (error) => {
          console.error('Error loading comments:', error);
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
            console.error('Error deleting comment:', error);
            this.error = 'Failed to delete comment. Please try again.';
          }
        });
    }
  }

  viewRecipe(recipeId: number, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/recipe', recipeId]);
  }

  viewUser(userId: number, event: Event): void {
    event.stopPropagation();
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
