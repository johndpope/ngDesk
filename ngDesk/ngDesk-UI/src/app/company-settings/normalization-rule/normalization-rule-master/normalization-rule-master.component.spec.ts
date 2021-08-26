import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NormalizationRuleMasterComponent } from '@src/app/company-settings/normalization-rule/normalization-rule-master/normalization-rule-master.component';

describe('NormalizationRuleMasterComponent', () => {
  let component: NormalizationRuleMasterComponent;
  let fixture: ComponentFixture<NormalizationRuleMasterComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ NormalizationRuleMasterComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NormalizationRuleMasterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
