import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminGuard } from '../../shared/services/admin-guard';


import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { CommentDetailComponent } from './comment-detail/comment-detail.component';
import { CommentManagementComponent } from './comment-management/comment-management.component';
import { UserManagementComponent } from './user-management/user-management.component';
import { RecipeManagementComponent } from './recipe-management/recipe-management.component';
import { UserDetailComponent } from './user-detail/user-detail.component';
import { RecipeDetailAdminComponent } from './recipe-detail-admin/recipe-detail-admin.component';

const routes: Routes = [
  {
    path: '',
    canActivate: [AdminGuard],
    children: [
      {
        path: '',
        component: AdminDashboardComponent
      },
      {
        path: 'users',
        component: UserManagementComponent
      },
      {
        path: 'users/:id',
        component: UserDetailComponent
      },
      {
        path: 'recipes',
        component: RecipeManagementComponent
      },
      {
        path: 'recipes/:id',
        component: RecipeDetailAdminComponent
      },
      {
        path: 'comments',
        component: CommentManagementComponent
      },
      {
        path: 'comments/:id',
        component: CommentDetailComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
