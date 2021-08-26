import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NodeCustomizationComponent } from '@src/app/modules/modules-detail/triggers/triggers-detail-new/node-customization/node-customization.component';

describe('NodeCustomizationComponent', () => {
  let component: NodeCustomizationComponent;
  let fixture: ComponentFixture<NodeCustomizationComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ NodeCustomizationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NodeCustomizationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
