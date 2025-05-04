import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SearchableSelectComponent } from './components/searchable-select/searchable-select.component';
import { ClickOutsideDirective } from './directives/click-outside.directive';
import { RatingStarsComponent } from './components/rating-stars/rating-stars.component';
import { FormatEnumPipe } from './pipes/format-enum.pipe';
import { SortByAlphaPipe } from './pipes/sort-by-alpha.pipe';

@NgModule({
  declarations: [
    SearchableSelectComponent,
    ClickOutsideDirective,
    RatingStarsComponent,
    FormatEnumPipe,
    SortByAlphaPipe
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
    FormsModule,
    ReactiveFormsModule
  ]
})
export class SharedModule { }
