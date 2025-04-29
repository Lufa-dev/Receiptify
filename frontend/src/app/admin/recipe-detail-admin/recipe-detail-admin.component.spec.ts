import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RecipeDetailAdminComponent } from './recipe-detail-admin.component';

describe('RecipeDetailAdminComponent', () => {
  let component: RecipeDetailAdminComponent;
  let fixture: ComponentFixture<RecipeDetailAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RecipeDetailAdminComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RecipeDetailAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
