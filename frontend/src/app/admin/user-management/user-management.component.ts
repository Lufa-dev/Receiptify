import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../shared/services/admin.service';
import { finalize } from 'rxjs/operators';
import { Router } from '@angular/router';
import { User } from '../../../shared/models/user.model';

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss']
})
export class UserManagementComponent implements OnInit {
  users: User[] = [];
  totalUsers = 0;
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
    this.loadUsers();
  }

  loadUsers(page: number = 0): void {
    this.isLoading = true;
    this.error = '';
    this.currentPage = page;

    this.adminService.getAllUsers(page, this.pageSize)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (response) => {
          this.users = response.content;
          this.totalUsers = response.totalElements;
        },
        error: (error) => {
          this.error = 'Failed to load users. Please try again.';
        }
      });
  }

  onPageChange(page: number): void {
    if (this.isSearching && this.searchQuery) {
      this.searchUsers(this.searchQuery, page);
    } else {
      this.loadUsers(page);
    }
  }

  onSearch(): void {
    if (!this.searchQuery.trim()) {
      this.isSearching = false;
      this.loadUsers(0);
      return;
    }

    this.isSearching = true;
    this.searchUsers(this.searchQuery, 0);
  }

  searchUsers(query: string, page: number = 0): void {
    this.isLoading = true;
    this.error = '';
    this.currentPage = page;

    this.adminService.searchUsers(query, page, this.pageSize)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (response) => {
          this.users = response.content;
          this.totalUsers = response.totalElements;
        },
        error: (error) => {
          this.error = 'Failed to search users. Please try again.';
        }
      });
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.isSearching = false;
    this.loadUsers(0);
  }

  viewUserDetails(id: number): void {
    this.router.navigate(['/admin/users', id]);
  }

  deleteUser(id: number, event: Event): void {
    event.stopPropagation();

    if (confirm('Are you sure you want to delete this user? This cannot be undone.')) {
      this.isLoading = true;
      this.error = '';
      this.successMessage = '';

      this.adminService.deleteUser(id)
        .pipe(finalize(() => this.isLoading = false))
        .subscribe({
          next: () => {
            this.successMessage = 'User deleted successfully';
            // Refresh the user list
            this.loadUsers(this.currentPage);
          },
          error: (error) => {
            this.error = 'Failed to delete user. Please try again.';
          }
        });
    }
  }

  toggleAdminRole(user: User, event: Event): void {
    event.stopPropagation();

    const makeAdmin = user.roles !== 'ADMIN';
    const confirmMessage = makeAdmin ?
      `Are you sure you want to promote ${user.username} to Admin?` :
      `Are you sure you want to remove Admin privileges from ${user.username}?`;

    if (confirm(confirmMessage)) {
      this.isLoading = true;
      this.error = '';
      this.successMessage = '';

      this.adminService.toggleAdminRole(user.id!, makeAdmin)
        .pipe(finalize(() => this.isLoading = false))
        .subscribe({
          next: (updatedUser) => {
            // Update the user in the list
            const index = this.users.findIndex(u => u.id === user.id);
            if (index !== -1) {
              this.users[index] = updatedUser;
            }

            this.successMessage = makeAdmin
              ? `${user.username} has been promoted to Admin`
              : `Admin privileges removed from ${user.username}`;
          },
          error: (error) => {
            this.error = 'Failed to update user role. Please try again.';
          }
        });
    }
  }

  get totalPages(): number {
    return Math.ceil(this.totalUsers / this.pageSize);
  }

  get pages(): number[] {
    const pagesArray = [];
    for (let i = 0; i < this.totalPages; i++) {
      pagesArray.push(i);
    }
    return pagesArray;
  }
}
