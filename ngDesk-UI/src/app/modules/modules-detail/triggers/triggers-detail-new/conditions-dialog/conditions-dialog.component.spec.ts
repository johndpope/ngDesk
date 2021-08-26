import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ConditionsDialogComponent } from '@src/app/modules/modules-detail/triggers/triggers-detail-new/conditions-dialog/conditions-dialog.component';

describe('ConditionsDialogComponent', () => {
  let component: ConditionsDialogComponent;
  let fixture: ComponentFixture<ConditionsDialogComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ConditionsDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConditionsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
