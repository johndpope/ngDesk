import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TriggersDetailNewComponent } from '@src/app/modules/modules-detail/triggers/triggers-detail-new/triggers-detail-new.component';

describe('TriggersDetailNewComponent', () => {
  let component: TriggersDetailNewComponent;
  let fixture: ComponentFixture<TriggersDetailNewComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ TriggersDetailNewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TriggersDetailNewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
