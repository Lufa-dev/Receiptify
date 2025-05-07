import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Comment } from '../../shared/models/comment.model';
import { CommentService } from '../../shared/services/comment.service';
import { AuthService } from '../../shared/services/auth.service';
import { finalize } from 'rxjs/operators';
import { Router } from '@angular/router';
import {Subscription} from "rxjs";

@Component({
  selector: 'app-recipe-comments',
  templateUrl: './recipe-comments.component.html',
  styleUrls: ['./recipe-comments.component.scss']
})
export class RecipeCommentsComponent implements OnInit, OnDestroy {
  @Input() recipeId!: number | undefined;
  @Input() isOwner: boolean = false;
  @Output() commentCountChanged = new EventEmitter<number>();

  comments: Comment[] = [];
  totalComments: number = 0;
  currentPage: number = 0;
  pageSize: number = 10;
  lastPage: boolean = false;
  isLoading: boolean = false;
  isSubmitting: boolean = false;
  error: string = '';
  successMessage: string = '';
  private subscriptions: Subscription[] = [];

  commentForm: FormGroup;
  editingCommentId: number | null = null;

  constructor(
    private commentService: CommentService,
    public authService: AuthService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.commentForm = this.createCommentForm();
  }

  ngOnInit(): void {
    this.loadComments();
  }

  createCommentForm(): FormGroup {
    return this.fb.group({
      content: ['', [Validators.required, Validators.maxLength(1000)]]
    });
  }

  loadComments(): void {
    if (!this.recipeId) return;

    this.isLoading = true;
    this.error = '';

    const sub = this.commentService.getRecipeComments(this.recipeId, this.currentPage, this.pageSize)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (response: any) => {
          this.comments = response.content;

          if (response.page) {
            this.totalComments = response.page.totalElements;
            this.lastPage = (response.page.number >= response.page.totalPages - 1);
          } else {
            this.totalComments = response.totalElements || 0;
            this.lastPage = response.last || false;
          }

        },
        error: (error) => {
          this.error = 'Failed to load comments. Please try again.';
        }
      });
    this.subscriptions.push(sub);
  }

  loadMoreComments(): void {
    if (!this.recipeId || this.lastPage || this.isLoading) {
      return;
    }

    this.currentPage++;
    this.isLoading = true;

    this.commentService.getRecipeComments(this.recipeId, this.currentPage, this.pageSize)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (response) => {
          this.comments = [...this.comments, ...response.content];
          this.lastPage = response.last || false;
        },
        error: (error) => {
          this.currentPage--; // Revert page increment on error
        }
      });
  }

  submitComment(): void {
    if (!this.recipeId) return;

    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login'], {
        queryParams: { returnUrl: `/recipe/${this.recipeId}` }
      });
      return;
    }

    if (this.commentForm.invalid) {
      return;
    }

    this.isSubmitting = true;
    this.error = '';
    this.successMessage = '';

    const comment: Comment = {
      content: this.commentForm.get('content')?.value,
      recipeId: this.recipeId
    };

    if (this.editingCommentId) {
      // Update existing comment
      this.commentService.updateComment(this.editingCommentId, comment)
        .pipe(finalize(() => this.isSubmitting = false))
        .subscribe({
          next: (updatedComment) => {
            const index = this.comments.findIndex(c => c.id === this.editingCommentId);
            if (index !== -1) {
              this.comments[index] = updatedComment;
            }
            this.successMessage = 'Comment updated successfully!';
            this.resetForm();
          },
          error: (error) => this.handleCommentError(error)
        });
    } else {
      // Add new comment
      this.commentService.addComment(comment)
        .pipe(finalize(() => this.isSubmitting = false))
        .subscribe({
          next: (newComment) => {
            this.comments.unshift(newComment); // Add to beginning of array
            this.totalComments++;
            this.commentCountChanged.emit(this.totalComments);
            this.successMessage = 'Comment added successfully!';
            this.resetForm();
          },
          error: (error) => this.handleCommentError(error)
        });
    }
  }

  editComment(comment: Comment): void {
    this.editingCommentId = comment.id ?? null;
    this.commentForm.patchValue({
      content: comment.content
    });
    // Scroll to form and focus
    document.getElementById('comment-form')?.scrollIntoView({ behavior: 'smooth' });
    document.getElementById('comment-textarea')?.focus();
  }

  deleteComment(id: number): void {
    if (id === undefined) {
      this.error = "Cannot delete comment with undefined ID";
      return;
    }

    if (confirm('Are you sure you want to delete this comment?')) {
      this.commentService.deleteComment(id)
        .subscribe({
          next: () => {
            this.comments = this.comments.filter(c => c.id !== id);
            this.totalComments--;
            this.commentCountChanged.emit(this.totalComments);
            this.successMessage = 'Comment deleted successfully!';

            setTimeout(() => {
              this.successMessage = '';
            }, 3000);
          },
          error: (error) => {

            if (error.status === 401) {
              this.router.navigate(['/login'], {
                queryParams: { returnUrl: `/recipe/${this.recipeId}` }
              });
            } else if (error.error?.error) {
              this.error = error.error.error;
            } else {
              this.error = 'Failed to delete comment. Please try again.';
            }
          }
        });
    }
  }

  cancelEdit(): void {
    this.resetForm();
  }

  resetForm(): void {
    this.commentForm.reset();
    this.editingCommentId = null;
  }

  canEditComment(comment: Comment): boolean {
    if (!this.authService.isLoggedIn()) {
      return false;
    }

    const username = sessionStorage.getItem('profileName');
    return username === comment.user?.username;
  }

  canModifyComment(comment: Comment): boolean {
    if (!this.authService.isLoggedIn()) {
      return false;
    }

    const username = sessionStorage.getItem('profileName');
    return username === comment.user?.username || this.isOwner;
  }

  private handleCommentError(error: any): void {

    if (error.status === 401) {
      this.router.navigate(['/login'], {
        queryParams: { returnUrl: `/recipe/${this.recipeId}` }
      });
    } else if (error.error?.error) {
      this.error = error.error.error;
    } else {
      this.error = 'Failed to process comment. Please try again.';
    }
  }

  formatDate(date: string): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }
}
