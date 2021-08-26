import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FileRuleDetailComponent } from '@src/app/company-settings/file-rule/file-rule-detail/file-rule-detail.component';

describe('FileRuleDetailComponent', () => {
  let component: FileRuleDetailComponent;
  let fixture: ComponentFixture<FileRuleDetailComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ FileRuleDetailComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FileRuleDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
