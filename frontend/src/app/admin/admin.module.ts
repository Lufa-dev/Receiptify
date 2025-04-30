import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { UserManagementComponent } from './user-management/user-management.component';
import { RecipeManagementComponent } from './recipe-management/recipe-management.component';
import { CommentManagementComponent } from './comment-management/comment-management.component';
import { AdminGuard } from '../../shared/services/admin-guard';
import { UserDetailComponent } from './user-detail/user-detail.component';
import { RecipeDetailAdminComponent } from './recipe-detail-admin/recipe-detail-admin.component';
import { CommentDetailComponent } from './comment-detail/comment-detail.component';
import {AdminRoutingModule} from "./admin-routing.module";



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
    AdminRoutingModule
  ],
  providers: [AdminGuard]
})
export class AdminModule { }
