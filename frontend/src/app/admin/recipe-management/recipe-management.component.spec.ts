import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RecipeManagementComponent } from './recipe-management.component';

describe('RecipeManagementComponent', () => {
  let component: RecipeManagementComponent;
  let fixture: ComponentFixture<RecipeManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RecipeManagementComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RecipeManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
