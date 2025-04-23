import {Component, Input} from '@angular/core';
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

  getStatusIcon(): string {
    if (!this.seasonality) return 'bi-question-circle text-secondary';

    if (this.seasonality.isInSeason) return 'bi-check-circle-fill text-success';
    if (this.seasonality.isComingSoon) return 'bi-clock-history text-warning';
    return 'bi-x-circle-fill text-danger';
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

