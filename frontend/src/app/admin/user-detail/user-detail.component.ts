import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AdminService } from '../../../shared/services/admin.service';
import { User } from '../../../shared/models/user.model';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-user-detail',
  templateUrl: './user-detail.component.html',
  styleUrls: ['./user-detail.component.scss']
})
export class UserDetailComponent implements OnInit {
  userId: number = 0;
  user: User | null = null;
  userForm: FormGroup;
  isLoading = false;
  isSubmitting = false;
  error = '';
  successMessage = '';
  isEditing = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private adminService: AdminService,
    private fb: FormBuilder
  ) {
    this.userForm = this.createUserForm();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.userId = +params['id'];
      if (this.userId) {
        this.loadUserDetails();
      } else {
        this.error = 'Invalid user ID';
      }
    });
  }

  createUserForm(): FormGroup {
    return this.fb.group({
      username: [{value: '', disabled: true}],
      email: [{value: '', disabled: !this.isEditing}, [Validators.required, Validators.email]],
      firstName: [{value: '', disabled: !this.isEditing}, Validators.required],
      lastName: [{value: '', disabled: !this.isEditing}, Validators.required],
      roles: [{value: 'USER', disabled: !this.isEditing}]
    });
  }

  loadUserDetails(): void {
    this.isLoading = true;
    this.error = '';

    this.adminService.getUserById(this.userId)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (user) => {
          this.user = user;
          this.updateFormWithUser(user);
        },
        error: (error) => {
          console.error('Error loading user details:', error);
          this.error = 'Failed to load user details. Please try again.';
        }
      });
  }

  updateFormWithUser(user: User): void {
    this.userForm.patchValue({
      username: user.username,
      email: user.email || '',
      firstName: user.firstName || '',
      lastName: user.lastName || '',
      roles: user.roles || 'USER'
    });
  }

  toggleEditMode(): void {
    this.isEditing = !this.isEditing;

    if (this.isEditing) {
      // Enable form controls for editing
      this.userForm.get('email')?.enable();
      this.userForm.get('firstName')?.enable();
      this.userForm.get('lastName')?.enable();
      this.userForm.get('roles')?.enable();
    } else {
      // Disable form controls when not editing and reset to original values
      this.userForm.get('email')?.disable();
      this.userForm.get('firstName')?.disable();
      this.userForm.get('lastName')?.disable();
      this.userForm.get('roles')?.disable();

      if (this.user) {
        this.updateFormWithUser(this.user);
      }
    }
  }

  onSubmit(): void {
    if (this.userForm.invalid) {
      return;
    }

    this.isSubmitting = true;
    this.error = '';
    this.successMessage = '';

    const userData = {
      email: this.userForm.get('email')?.value,
      firstName: this.userForm.get('firstName')?.value,
      lastName: this.userForm.get('lastName')?.value,
      roles: this.userForm.get('roles')?.value
    };

    this.adminService.updateUser(this.userId, userData)
      .pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: (updatedUser) => {
          this.user = updatedUser;
          this.successMessage = 'User updated successfully';
          this.isEditing = false;
          this.toggleEditMode(); // This will disable the form
        },
        error: (error) => {
          console.error('Error updating user:', error);
          this.error = 'Failed to update user. Please try again.';
        }
      });
  }

  deleteUser(): void {
    if (confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
      this.isLoading = true;
      this.error = '';
      this.successMessage = '';

      this.adminService.deleteUser(this.userId)
        .pipe(finalize(() => this.isLoading = false))
        .subscribe({
          next: () => {
            this.successMessage = 'User deleted successfully';
            // Navigate back to user list after short delay
            setTimeout(() => {
              this.router.navigate(['/admin/users']);
            }, 1500);
          },
          error: (error) => {
            console.error('Error deleting user:', error);
            this.error = 'Failed to delete user. Please try again.';
          }
        });
    }
  }

  goBack(): void {
    this.router.navigate(['/admin/users']);
  }
}

