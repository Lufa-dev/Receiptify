import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RegisterComponent } from './register/register.component';
import { LoginComponent } from './login/login.component';
import { HomeComponent } from './home/home.component';
import {ProfileComponent} from "./profile/profile.component";
import {RecipeFormComponent} from "./recipe-form/recipe-form.component";
import {AuthGuard} from "../shared/services/auth-guard";
import {RecipeDetailComponent} from "./recipe-detail/recipe-detail.component";
import {MyRecipesComponent} from "./my-recipes/my-recipes.component";
import {CollectionsComponent} from "./collections/collections.component";
import {CollectionDetailComponent} from "./collection-detail/collection-detail.component";
import {AdvancedSearchComponent} from "./advanced-search/advanced-search.component";


const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'submit-recipe', component: RecipeFormComponent, canActivate: [AuthGuard] },
  { path: 'my-recipes', component: MyRecipesComponent, canActivate: [AuthGuard] },
  { path: 'edit-recipe/:id', component: RecipeFormComponent, canActivate: [AuthGuard] },
  { path: 'recipe/:id', component: RecipeDetailComponent },
  { path: 'collections', component: CollectionsComponent, canActivate: [AuthGuard] },
  { path: 'collections/:id', component: CollectionDetailComponent, canActivate: [AuthGuard] },
  { path: 'advanced-search', component: AdvancedSearchComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
