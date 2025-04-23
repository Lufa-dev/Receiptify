import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SeasonalityIndicatorComponent } from './seasonality-indicator.component';

describe('SeasonalityIndicatorComponent', () => {
  let component: SeasonalityIndicatorComponent;
  let fixture: ComponentFixture<SeasonalityIndicatorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SeasonalityIndicatorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SeasonalityIndicatorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
