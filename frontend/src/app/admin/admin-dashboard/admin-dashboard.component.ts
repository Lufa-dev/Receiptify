import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../shared/services/admin.service';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit {
  stats: any = {};
  isLoading = false;
  error = '';

  constructor(private adminService: AdminService) { }

  ngOnInit(): void {
    this.loadDashboardStats();
  }

  loadDashboardStats(): void {
    this.isLoading = true;
    this.error = '';

    this.adminService.getDashboardStats()
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (stats) => {
          this.stats = stats;
        },
        error: (error) => {
          this.error = 'Failed to load dashboard statistics. Please try again.';
        }
      });
  }
}
