import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { UserManagementComponent } from './user-management/user-management.component';
import { RecipeManagementComponent } from './recipe-management/recipe-management.component';
import { CommentManagementComponent } from './comment-management/comment-management.component';
import { AdminGuard } from '../../shared/services/admin-guard';
import { UserDetailComponent } from './user-detail/user-detail.component';
import { RecipeDetailAdminComponent } from './recipe-detail-admin/recipe-detail-admin.component';
import { CommentDetailComponent } from './comment-detail/comment-detail.component';

const routes: Routes = [
  {
    path: 'admin',
    component: AdminDashboardComponent,
    canActivate: [AdminGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: AdminDashboardComponent },
      { path: 'users', component: UserManagementComponent },
      { path: 'users/:id', component: UserDetailComponent },
      { path: 'recipes', component: RecipeManagementComponent },
      { path: 'recipes/:id', component: RecipeDetailAdminComponent },
      { path: 'comments', component: CommentManagementComponent },
      { path: 'comments/:id', component: CommentDetailComponent }
    ]
  }
];

@NgModule({
  declarations: [
    AdminDashboardComponent,
    UserManagementComponent,
    RecipeManagementComponent,
    CommentManagementComponent,
    UserDetailComponent,
    RecipeDetailAdminComponent,
    CommentDetailComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes)
  ],
  providers: [AdminGuard]
})
export class AdminModule { }
