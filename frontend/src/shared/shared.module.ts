import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SearchableSelectComponent } from './components/searchable-select/searchable-select.component';
import { ClickOutsideDirective } from './directives/click-outside.directive';
import { RatingStarsComponent } from './components/rating-stars/rating-stars.component';
import { FormatEnumPipe } from './pipes/format-enum.pipe';
import { SortByAlphaPipe } from './pipes/sort-by-alpha.pipe';
import {DietaryTagsComponent} from "../app/dietary-tags/dietary-tags.component";

@NgModule({
  declarations: [
    SearchableSelectComponent,
    ClickOutsideDirective,
    RatingStarsComponent,
    FormatEnumPipe,
    SortByAlphaPipe,
    DietaryTagsComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule
  ],
  exports: [
    SearchableSelectComponent,
    ClickOutsideDirective,
    RatingStarsComponent,
    FormatEnumPipe,
    SortByAlphaPipe,
    DietaryTagsComponent,
    FormsModule,
    ReactiveFormsModule
  ]
})
export class SharedModule { }
