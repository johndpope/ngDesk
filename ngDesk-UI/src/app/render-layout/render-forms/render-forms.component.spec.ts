import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RenderFormsComponent } from './render-forms.component';

describe('RenderFormsComponent', () => {
  let component: RenderFormsComponent;
  let fixture: ComponentFixture<RenderFormsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RenderFormsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RenderFormsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
