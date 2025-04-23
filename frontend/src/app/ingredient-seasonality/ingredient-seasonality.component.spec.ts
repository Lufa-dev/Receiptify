import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IngredientSeasonalityComponent } from './ingredient-seasonality.component';

describe('IngredientSeasonalityComponent', () => {
  let component: IngredientSeasonalityComponent;
  let fixture: ComponentFixture<IngredientSeasonalityComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [IngredientSeasonalityComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(IngredientSeasonalityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
