import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NormalizationRuleDetailComponent } from '@src/app/company-settings/normalization-rule/normalization-rule-detail/normalization-rule-detail.component';

describe('NormalizationRuleDetailComponent', () => {
  let component: NormalizationRuleDetailComponent;
  let fixture: ComponentFixture<NormalizationRuleDetailComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ NormalizationRuleDetailComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NormalizationRuleDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
