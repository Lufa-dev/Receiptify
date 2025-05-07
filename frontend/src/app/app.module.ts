import { BrowserModule } from '@angular/platform-browser';
import { APP_INITIALIZER, NgModule } from '@angular/core';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { RouterModule } from '@angular/router';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppComponent } from './app.component';
import { RegisterComponent } from './register/register.component';
import { LoginComponent } from './login/login.component';
import { HomeComponent } from './home/home.component';
import { NavbarComponent } from '../shared/components/navbar/navbar.component';
import { AuthService } from '../shared/services/auth.service';
import { ProfileComponent } from './profile/profile.component';
import { RecipeFormComponent } from './recipe-form/recipe-form.component';
import { RecipeDetailComponent } from './recipe-detail/recipe-detail.component';
import { MyRecipesComponent } from './my-recipes/my-recipes.component';
import { CollectionsComponent } from './collections/collections.component';
import { CollectionDetailComponent } from './collection-detail/collection-detail.component';
import { AddToCollectionComponent } from './add-to-collection/add-to-collection.component';
import { RecipeRatingComponent } from './recipe-rating/recipe-rating.component';
import { RecipeCommentsComponent } from './recipe-comments/recipe-comments.component';
import { PortionCalculatorService } from "../shared/services/portion-calculator.service";
import { AdvancedSearchComponent } from './advanced-search/advanced-search.component';
import { AddToShoppingListComponent } from './add-to-shopping-list/add-to-shopping-list.component';
import { ShoppingListComponent } from './shopping-list/shopping-list.component';
import { SeasonalityIndicatorComponent } from './seasonality-indicator/seasonality-indicator.component';
import { IngredientSeasonalityComponent } from './ingredient-seasonality/ingredient-seasonality.component';
import { NutritionDisplayComponent } from './nutrition-display/nutrition-display.component';
import { RecommendedRecipesComponent } from './recommended-recipes/recommended-recipes.component';
import { SimilarRecipesComponent } from './similar-recipes/similar-recipes.component';
import { AdminModule } from "./admin/admin.module";
import { SharedModule } from '../shared/shared.module';
import {ApiUrlProvider} from "../shared/services/api-url.provider";
import {ApiUrlInterceptor} from "../shared/interceptors/api-url.interceptor";
import {ActivityTrackerService} from "../shared/services/activity-tracker.service";

export function clearStorageInitializer() {
  return () => {
    sessionStorage.clear();
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
    RecipeRatingComponent,
    RecipeCommentsComponent,
    AdvancedSearchComponent,
    AddToShoppingListComponent,
    ShoppingListComponent,
    SeasonalityIndicatorComponent,
    IngredientSeasonalityComponent,
    NutritionDisplayComponent,
    RecommendedRecipesComponent,
    SimilarRecipesComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    RouterModule,
    BrowserAnimationsModule,
    SharedModule,
    AdminModule
  ],
  providers: [AuthService, PortionCalculatorService, ApiUrlProvider, ActivityTrackerService,
    {
      provide: APP_INITIALIZER,
      useFactory: clearStorageInitializer,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ApiUrlInterceptor,
      multi: true
    }],
  bootstrap: [AppComponent]
})
export class AppModule { }
