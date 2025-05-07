import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DietaryTagsComponent } from './dietary-tags.component';

describe('DietaryTagsComponent', () => {
  let component: DietaryTagsComponent;
  let fixture: ComponentFixture<DietaryTagsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DietaryTagsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DietaryTagsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
