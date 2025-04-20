import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-rating-stars',
  templateUrl: './rating-stars.component.html',
  styleUrls: ['./rating-stars.component.scss']
})
export class RatingStarsComponent {
  @Input() rating: number = 0;
  @Input() readonly: boolean = true;
  @Input() size: 'small' | 'medium' | 'large' = 'medium';
  @Output() ratingChange = new EventEmitter<number>();

  hoverRating: number = 0;
  stars: number[] = [1, 2, 3, 4, 5];

  constructor() { }

  onStarHover(star: number): void {
    if (!this.readonly) {
      this.hoverRating = star;
    }
  }

  onStarLeave(): void {
    if (!this.readonly) {
      this.hoverRating = 0;
    }
  }

  onStarClick(star: number): void {
    if (!this.readonly) {
      this.rating = star;
      this.ratingChange.emit(star);
    }
  }

  getStarClass(star: number): string {
    const displayRating = this.hoverRating || this.rating;

    if (displayRating >= star) {
      return 'bi-star-fill'; // Filled star
    } else if (displayRating >= star - 0.5) {
      return 'bi-star-half'; // Half star
    } else {
      return 'bi-star'; // Empty star
    }
  }
}
