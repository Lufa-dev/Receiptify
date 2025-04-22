import { BrowserModule } from '@angular/platform-browser';
import {APP_INITIALIZER, NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import { AppRoutingModule } from './app-routing.module';
import { RouterModule } from '@angular/router';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppComponent } from './app.component';
import { RegisterComponent } from './register/register.component';
import { LoginComponent } from './login/login.component';
import { HomeComponent } from './home/home.component';
import { NavbarComponent } from '../shared/components/navbar/navbar.component';
import { AuthService } from '../shared/services/auth.service';
import { HttpClientModule } from '@angular/common/http';
import { ProfileComponent } from './profile/profile.component';
import { RecipeFormComponent } from './recipe-form/recipe-form.component';
import { RecipeDetailComponent } from './recipe-detail/recipe-detail.component';
import { MyRecipesComponent } from './my-recipes/my-recipes.component';
import { CollectionsComponent } from './collections/collections.component';
import { CollectionDetailComponent } from './collection-detail/collection-detail.component';
import { AddToCollectionComponent } from './add-to-collection/add-to-collection.component';
import {ClickOutsideDirective} from "../shared/directives/click-outside.directive";
import {RatingStarsComponent} from "../shared/components/rating-stars/rating-stars.component";
import { RecipeRatingComponent } from './recipe-rating/recipe-rating.component';
import { RecipeCommentsComponent } from './recipe-comments/recipe-comments.component';
import {PortionCalculatorService} from "../shared/services/portion-calculator.service";

export function clearStorageInitializer() {
  return () => {
    console.log('Clearing storage for fresh start');
    sessionStorage.clear();
    localStorage.clear();
    return Promise.resolve();
  };
}

@NgModule({
  declarations: [
    AppComponent,
    RegisterComponent,
    LoginComponent,
    HomeComponent,
    NavbarComponent,
    ProfileComponent,
    RecipeFormComponent,
    RecipeDetailComponent,
    MyRecipesComponent,
    CollectionsComponent,
    CollectionDetailComponent,
    AddToCollectionComponent,
    ClickOutsideDirective,
    RatingStarsComponent,
    RecipeRatingComponent,
    RecipeCommentsComponent
  ],
    imports: [
        BrowserModule,
        FormsModule,
        HttpClientModule,
        AppRoutingModule,
        RouterModule,
        BrowserAnimationsModule,
        ReactiveFormsModule
    ],
  providers: [AuthService, PortionCalculatorService,
    {
      provide: APP_INITIALIZER,
      useFactory: clearStorageInitializer,
      multi: true
    }],
  bootstrap: [AppComponent]
})
export class AppModule { }
