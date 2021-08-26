import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OneToManyDialogComponent } from './one-to-many-dialog.component';

describe('OneToManyDialogComponent', () => {
  let component: OneToManyDialogComponent;
  let fixture: ComponentFixture<OneToManyDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OneToManyDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OneToManyDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
