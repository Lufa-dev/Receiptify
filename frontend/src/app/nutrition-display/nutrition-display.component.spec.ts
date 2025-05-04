import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NutritionDisplayComponent } from './nutrition-display.component';

describe('NutritionDisplayComponent', () => {
  let component: NutritionDisplayComponent;
  let fixture: ComponentFixture<NutritionDisplayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NutritionDisplayComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NutritionDisplayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
