import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AdminService } from '../../../shared/services/admin.service';
import { RecipeService } from '../../../shared/services/recipe.service';
import { Comment } from '../../../shared/models/comment.model';
import { RecipeDTO } from '../../../shared/models/recipe.model';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-comment-detail',
  templateUrl: './comment-detail.component.html',
  styleUrls: ['./comment-detail.component.scss']
})
export class CommentDetailComponent implements OnInit {
  commentId: number = 0;
  comment: Comment | null = null;
  recipe: RecipeDTO | null = null;
  commentForm: FormGroup;
  isLoading = false;
  isSubmitting = false;
  error = '';
  successMessage = '';
  isEditing = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private adminService: AdminService,
    private recipeService: RecipeService,
    private fb: FormBuilder
  ) {
    this.commentForm = this.createCommentForm();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.commentId = +params['id'];
      if (this.commentId) {
        this.loadCommentDetails();
      } else {
        this.error = 'Invalid comment ID';
      }
    });
  }

  createCommentForm(): FormGroup {
    // All form controls start disabled since we're in view mode by default
    return this.fb.group({
      content: [{value: '', disabled: true}, [Validators.required, Validators.maxLength(1000)]],
      moderationStatus: [{value: 'active', disabled: true}],
      adminNotes: [{value: '', disabled: true}]
    });
  }

  loadCommentDetails(): void {
    this.isLoading = true;
    this.error = '';

    this.adminService.getCommentById(this.commentId)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (comment) => {
          this.comment = comment;
          this.updateFormWithComment(comment);

          // Load the associated recipe
          if (comment.recipeId) {
            this.loadRecipeDetails(comment.recipeId);
          }
        },
        error: (error) => {
          console.error('Error loading comment details:', error);
          this.error = 'Failed to load comment details. Please try again.';
        }
      });
  }

  updateFormWithComment(comment: Comment): void {
    // Update form values while maintaining the enabled/disabled state
    // Map status to moderationStatus for the backend
    this.commentForm.patchValue({
      content: comment.content,
      moderationStatus: comment.moderationStatus || 'active',
      adminNotes: comment.adminNotes || ''
    });
  }

  loadRecipeDetails(recipeId: number): void {
    this.recipeService.getRecipeById(recipeId)
      .subscribe({
        next: (recipe) => {
          this.recipe = recipe;
        },
        error: (error) => {
          console.error('Error loading recipe details:', error);
          // Don't set an error message, just log it as this is secondary info
        }
      });
  }

  toggleEditMode(): void {
    this.isEditing = !this.isEditing;

    if (this.isEditing) {
      // Enable form controls for editing mode
      this.commentForm.get('content')?.enable();
      this.commentForm.get('moderationStatus')?.enable();
      this.commentForm.get('adminNotes')?.enable();
    } else {
      // Disable form controls when exiting edit mode
      this.commentForm.get('content')?.disable();
      this.commentForm.get('moderationStatus')?.disable();
      this.commentForm.get('adminNotes')?.disable();

      // Reset form to original values when canceling edit
      if (this.comment) {
        this.updateFormWithComment(this.comment);
      }
    }
  }

  onSubmit(): void {
    if (this.commentForm.invalid) {
      Object.keys(this.commentForm.controls).forEach(key => {
        const control = this.commentForm.get(key);
        control?.markAsTouched();
      });
      return;
    }

    this.isSubmitting = true;
    this.error = '';
    this.successMessage = '';

    // Need to get raw value since controls might be disabled
    const formValue = this.commentForm.getRawValue();

    // Use correct field names for backend
    const commentData = {
      content: formValue.content,
      moderationStatus: formValue.moderationStatus, // Match backend field name
      adminNotes: formValue.adminNotes
    };

    console.log('Sending comment data to backend:', commentData);

    this.adminService.updateComment(this.commentId, commentData)
      .pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: (updatedComment) => {
          this.comment = updatedComment;
          this.successMessage = 'Comment updated successfully';
          this.isEditing = false;
          this.toggleEditMode(); // This will disable the form controls
        },
        error: (error) => {
          console.error('Error updating comment:', error);
          this.error = 'Failed to update comment. Please try again.';
        }
      });
  }

  deleteComment(): void {
    if (confirm('Are you sure you want to delete this comment? This action cannot be undone.')) {
      this.isLoading = true;
      this.error = '';
      this.successMessage = '';

      this.adminService.deleteComment(this.commentId)
        .pipe(finalize(() => this.isLoading = false))
        .subscribe({
          next: () => {
            this.successMessage = 'Comment deleted successfully';
            // Navigate back to comment list after short delay
            setTimeout(() => {
              this.router.navigate(['/admin/comments']);
            }, 1500);
          },
          error: (error) => {
            console.error('Error deleting comment:', error);
            this.error = 'Failed to delete comment. Please try again.';
          }
        });
    }
  }

  viewRecipe(): void {
    if (this.comment && this.comment.recipeId) {
      this.router.navigate(['/recipe', this.comment.recipeId]);
    }
  }

  viewUser(): void {
    if (this.comment && this.comment.user && this.comment.user.id) {
      this.router.navigate(['/admin/users', this.comment.user.id]);
    }
  }

  getUserDisplayName(): string {
    if (!this.comment || !this.comment.user) {
      return 'Unknown User';
    }

    if (this.comment.user.firstName || this.comment.user.lastName) {
      return `${this.comment.user.firstName || ''} ${this.comment.user.lastName || ''} (${this.comment.user.username})`;
    }

    return this.comment.user.username;
  }

  goBack(): void {
    this.router.navigate(['/admin/comments']);
  }

  formatDate(date: string): string {
    if (!date) return '';
    return new Date(date).toLocaleString();
  }
}

