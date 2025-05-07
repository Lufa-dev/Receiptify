import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {IngredientSeasonality} from "../../shared/models/seasonality.model";

@Component({
  selector: 'app-ingredient-seasonality',
  templateUrl: './ingredient-seasonality.component.html',
  styleUrl: './ingredient-seasonality.component.scss'
})
export class IngredientSeasonalityComponent {
  @Input() seasonality: IngredientSeasonality | null = null;
  @Input() showIcon = true;
  @Input() showLabel = false;
  @Input() showTooltip = true;

  // Debug properties
  debugMessage: string = '';

  getStatusIcon(): string {
    if (!this.seasonality) return 'bi-question-circle text-secondary';

    // Primary check using status string for reliability
    if (this.seasonality.status === 'In Season') {
      return 'bi-check-circle-fill text-success';
    } else if (this.seasonality.status === 'Coming Soon') {
      return 'bi-clock-history text-warning';
    } else if (this.seasonality.status === 'Out of Season') {
      return 'bi-x-circle-fill text-danger';
    }

    // Fallback to boolean properties if status string doesn't match expected values
    if (this.seasonality.isInSeason) {
      return 'bi-check-circle-fill text-success';
    } else if (this.seasonality.isComingSoon) {
      return 'bi-clock-history text-warning';
    } else {
      return 'bi-x-circle-fill text-danger';
    }
  }

  getStatusLabel(): string {
    if (!this.seasonality) return 'Unknown';
    return this.seasonality.status;
  }

  getTooltip(): string {
    if (!this.seasonality) return 'Seasonality information not available';

    let tooltip = `Status: ${this.seasonality.status}\n`;
    tooltip += `Seasonality: ${this.seasonality.seasonality}`;

    return tooltip;
  }
}



