import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {NutritionService} from "../../shared/services/nutrition.service";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-nutrition-display',
  templateUrl: './nutrition-display.component.html',
  styleUrl: './nutrition-display.component.scss'
})
export class NutritionDisplayComponent implements OnInit, OnDestroy {
  @Input() recipeId: number | undefined;

  nutrition: any = {};
  dailyValues: any = {};
  macroDistribution: any = {}; // New property for normalized macronutrient distribution
  isLoading = false;
  error = '';
  private subscriptions: Subscription[] = [];

  constructor(private nutritionService: NutritionService) {}

  ngOnInit(): void {
    this.loadNutritionData();
  }

  loadNutritionData(): void {
    if (!this.recipeId) return;

    this.isLoading = true;
    const nutritionSub = this.nutritionService.getRecipeNutrition(this.recipeId)
      .subscribe({
        next: (data) => {
          this.nutrition = data.nutrition;
          this.dailyValues = data.dailyValues;
          this.macroDistribution = data.macroDistribution; // Get the normalized distribution
          this.isLoading = false;
        },
        error: (err) => {
          this.error = 'Failed to load nutrition information.';
          this.isLoading = false;
        }
      });
    this.subscriptions.push(nutritionSub);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }
}


